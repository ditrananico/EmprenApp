<#
.SYNOPSIS
  Compara las entidades JPA con los .sql del proyecto y reporta que falta o no coincide
  para que Hibernate (ddl-auto=validate) y MySQL puedan levantar el esquema.
  SOLO LECTURA: no modifica ningun archivo.

.PARAMETER Root
  Raiz del proyecto (donde estan scripts.sql y src/).

.PARAMETER SqlFiles
  Archivos .sql en orden de ejecucion (relativos a Root). Por defecto: scripts.sql y, si no esta
  vacio, data.sql.

.PARAMETER SourceDir
  Carpeta con las clases Java (relativa a Root).

NOTE: este archivo es ASCII a proposito (Windows PowerShell 5.1 lee los .ps1 sin BOM como ANSI).
#>
[CmdletBinding()]
param(
    [string]$Root = (Get-Location).Path,
    [string[]]$SqlFiles,
    [string]$SourceDir = 'src/main/java'
)

$ErrorActionPreference = 'Stop'
# Un fallo interno del verificador NO debe confundirse con "hay bloqueantes" (exit 1).
trap {
    Write-Output "ERROR interno del verificador: $($_.Exception.Message) (linea $($_.InvocationInfo.ScriptLineNumber))"
    exit 2
}
$Root = (Resolve-Path -LiteralPath $Root).Path

$Findings = New-Object System.Collections.Generic.List[object]
$Fixes    = New-Object System.Collections.Generic.List[object]
$script:fkChecks = $true                                   # SET FOREIGN_KEY_CHECKS
$script:insertRecords = New-Object System.Collections.Generic.List[object]
$script:collations = New-Object System.Collections.Generic.List[string]
$script:namingMode = 'snake'                               # snake | standard

function Add-Finding([string]$Level, [string]$Where, [string]$Msg) {
    $Findings.Add([pscustomobject]@{ Level = $Level; Where = $Where; Msg = $Msg })
}

# ---------------------------------------------------------------- utilidades
# Replica CamelCaseToUnderscoresNamingStrategy (default de Spring Boot): '_' solo entre
# minuscula + MAYUSCULA + minuscula ("userID" -> "userid", "fechaCreacion" -> "fecha_creacion").
function ConvertTo-Snake([string]$s) {
    $sb = New-Object System.Text.StringBuilder
    for ($i = 0; $i -lt $s.Length; $i++) {
        $c = $s[$i]
        if ($i -gt 0 -and $i -lt ($s.Length - 1) -and [char]::IsLower($s[$i - 1]) -and [char]::IsUpper($c) -and [char]::IsLower($s[$i + 1])) {
            [void]$sb.Append('_')
        }
        [void]$sb.Append([char]::ToLower($c))
    }
    return $sb.ToString()
}

# Nombre fisico segun la naming strategy: aplica a nombres implicitos Y explicitos.
function Get-PhysName([string]$n) {
    if ($script:namingMode -eq 'standard') { return $n }
    return (ConvertTo-Snake $n)
}

function Get-NormName([string]$s) {
    $x = $s.Trim().Trim('`')
    if ($x.Contains('.')) { $x = $x.Substring($x.LastIndexOf('.') + 1).Trim('`') }
    return $x.ToLower()
}

function Get-NameList([string]$s) {
    $r = @()
    foreach ($p in $s.Split(',')) {
        $n = ($p -replace '\(\s*\d+\s*\)', '').Trim()
        if ($n) { $r += (Get-NormName $n) }
    }
    return , $r
}

function Split-Top([string]$s) {
    $parts = New-Object System.Collections.Generic.List[string]
    $depth = 0
    $q = [char]0
    $sb = New-Object System.Text.StringBuilder
    foreach ($c in $s.ToCharArray()) {
        if ($q -ne [char]0) {
            [void]$sb.Append($c)
            if ($c -eq $q) { $q = [char]0 }
            continue
        }
        if ($c -eq "'" -or $c -eq '"' -or $c -eq '`') { $q = $c; [void]$sb.Append($c); continue }
        if ($c -eq '(') { $depth++ } elseif ($c -eq ')') { $depth-- }
        if ($c -eq ',' -and $depth -eq 0) {
            $parts.Add($sb.ToString().Trim())
            [void]$sb.Clear()
            continue
        }
        [void]$sb.Append($c)
    }
    $last = $sb.ToString().Trim()
    if ($last.Length -gt 0) { $parts.Add($last) }
    return , $parts.ToArray()
}

# Devuelve el contenido de cada tupla (...) de un VALUES (..),(..) ignorando ON DUPLICATE KEY UPDATE.
function Get-ValueTuples([string]$s) {
    $cut = [regex]::Match($s, '(?is)\bON\s+DUPLICATE\s+KEY\s+UPDATE\b')
    if ($cut.Success) { $s = $s.Substring(0, $cut.Index) }
    $tuples = New-Object System.Collections.Generic.List[string]
    $depth = 0; $q = [char]0
    $sb = New-Object System.Text.StringBuilder
    foreach ($c in $s.ToCharArray()) {
        if ($q -ne [char]0) {
            if ($depth -gt 0) { [void]$sb.Append($c) }
            if ($c -eq $q) { $q = [char]0 }
            continue
        }
        if ($c -eq "'" -or $c -eq '"' -or $c -eq '`') { $q = $c; if ($depth -gt 0) { [void]$sb.Append($c) }; continue }
        if ($c -eq '(') {
            $depth++
            if ($depth -eq 1) { [void]$sb.Clear(); continue }
        }
        elseif ($c -eq ')') {
            $depth--
            if ($depth -eq 0) { $tuples.Add($sb.ToString()); [void]$sb.Clear(); continue }
        }
        if ($depth -gt 0) { [void]$sb.Append($c) }
    }
    return , $tuples.ToArray()
}

# En MySQL las columnas de la PRIMARY KEY son NOT NULL aunque no lo digan.
function Set-PkNotNull($tbl) {
    foreach ($pc in $tbl.Pk) {
        if ($tbl.Columns.Contains($pc)) { $tbl.Columns[$pc].NotNull = $true }
    }
}

# Divide el texto SQL en sentencias, ignorando comentarios y respetando comillas.
function Split-SqlStatements([string]$Text, [string]$File) {
    $out = New-Object System.Collections.Generic.List[object]
    $sb = New-Object System.Text.StringBuilder
    $line = 1; $startLine = 0
    $q = [char]0
    $i = 0; $n = $Text.Length
    while ($i -lt $n) {
        $c = $Text[$i]
        if ($c -eq "`n") { $line++ }
        if ($q -ne [char]0) {
            [void]$sb.Append($c)
            if ($c -eq '\' -and ($i + 1) -lt $n) { $i++; [void]$sb.Append($Text[$i]) }
            elseif ($c -eq $q) { $q = [char]0 }
            $i++; continue
        }
        if ($c -eq '-' -and ($i + 1) -lt $n -and $Text[$i + 1] -eq '-') {
            while ($i -lt $n -and $Text[$i] -ne "`n") { $i++ }
            continue
        }
        if ($c -eq '#') {
            while ($i -lt $n -and $Text[$i] -ne "`n") { $i++ }
            continue
        }
        if ($c -eq '/' -and ($i + 1) -lt $n -and $Text[$i + 1] -eq '*') {
            $i += 2
            while ($i -lt $n -and -not ($Text[$i] -eq '*' -and ($i + 1) -lt $n -and $Text[$i + 1] -eq '/')) {
                if ($Text[$i] -eq "`n") { $line++ }
                $i++
            }
            $i += 2
            continue
        }
        if ($c -eq "'" -or $c -eq '"' -or $c -eq '`') {
            $q = $c
            if ($startLine -eq 0) { $startLine = $line }
            [void]$sb.Append($c); $i++; continue
        }
        if ($c -eq ';') {
            $t = $sb.ToString().Trim()
            if ($t.Length -gt 0) { $out.Add([pscustomobject]@{ Text = $t; Line = $startLine; File = $File }) }
            [void]$sb.Clear(); $startLine = 0
            $i++; continue
        }
        if ($startLine -eq 0 -and -not [char]::IsWhiteSpace($c)) { $startLine = $line }
        [void]$sb.Append($c)
        $i++
    }
    $t = $sb.ToString().Trim()
    if ($t.Length -gt 0) { $out.Add([pscustomobject]@{ Text = $t; Line = $startLine; File = $File }) }
    return , $out.ToArray()
}

# ---------------------------------------------------------------- modelo SQL
function New-Col([string]$name, [string]$type, [string]$targs, [string]$rest) {
    $t = $type.ToUpper()
    switch ($t) {
        'BOOLEAN' { $t = 'TINYINT'; $targs = '1' }
        'BOOL'    { $t = 'TINYINT'; $targs = '1' }
        'INTEGER' { $t = 'INT' }
        'NUMERIC' { $t = 'DECIMAL' }
        'DEC'     { $t = 'DECIMAL' }
        'FIXED'   { $t = 'DECIMAL' }
    }
    $len = $null; $prec = $null; $scale = $null
    if ($targs) {
        $p = $targs.Split(',')
        if ($t -in 'VARCHAR', 'CHAR', 'VARBINARY', 'BINARY') { $len = [int]$p[0].Trim() }
        elseif ($t -eq 'DECIMAL') {
            $prec = [int]$p[0].Trim()
            if ($p.Count -gt 1) { $scale = [int]$p[1].Trim() } else { $scale = 0 }
        }
    }
    elseif ($t -eq 'DECIMAL') { $prec = 10; $scale = 0 }
    return [pscustomobject]@{
        Name         = $name.ToLower()
        Type         = $t
        Len          = $len
        Prec         = $prec
        Scale        = $scale
        Unsigned     = ($rest -match '(?i)\bUNSIGNED\b')
        NotNull      = ($rest -match '(?i)\bNOT\s+NULL\b')
        AutoInc      = ($rest -match '(?i)\bAUTO_INCREMENT\b')
        HasDefault   = ($rest -match '(?i)\bDEFAULT\b')
        InlinePk     = ($rest -match '(?i)\bPRIMARY\s+KEY\b')
        InlineUnique = ($rest -match '(?i)\bUNIQUE\b')
    }
}

function Format-Col($c) {
    $s = $c.Type
    if ($null -ne $c.Len) { $s += "($($c.Len))" }
    elseif ($null -ne $c.Prec) { $s += "($($c.Prec),$($c.Scale))" }
    if ($c.Unsigned) { $s += ' UNSIGNED' }
    return $s
}

# Interpreta un item del cuerpo de CREATE TABLE o una clausula de ALTER TABLE ... ADD
function Read-Item([string]$item) {
    $t = $item.Trim()
    if ($t -match '(?is)^PRIMARY\s+KEY\s*\(([^)]*)\)') {
        return [pscustomobject]@{ Kind = 'pk'; Cols = (Get-NameList $Matches[1]) }
    }
    if ($t -match '(?is)^(?:CONSTRAINT\s+(\S+)\s+)?FOREIGN\s+KEY\s*(?:\w+\s*)?\(([^)]*)\)\s*REFERENCES\s+([\w`.]+)\s*\(([^)]*)\)') {
        $fkName = $Matches[1]
        return [pscustomobject]@{
            Kind    = 'fk'
            Name    = $(if ($fkName) { $fkName.Trim('`').ToLower() } else { '' })
            Cols    = (Get-NameList $Matches[2])
            RefTable = (Get-NormName $Matches[3])
            RefCols = (Get-NameList $Matches[4])
        }
    }
    if ($t -match '(?is)^(?:CONSTRAINT\s+\S+\s+)?UNIQUE(?:\s+(?:KEY|INDEX))?\s*(?:\w+\s*)?\(([^)]*)\)') {
        return [pscustomobject]@{ Kind = 'unique'; Cols = (Get-NameList $Matches[1]) }
    }
    if ($t -match '(?is)^(?:INDEX|KEY|FULLTEXT|SPATIAL)\b') { return [pscustomobject]@{ Kind = 'index' } }
    if ($t -match '(?is)^(?:CONSTRAINT\s+\S+\s+)?CHECK\b') { return [pscustomobject]@{ Kind = 'index' } }
    if ($t -match '(?is)^`?(\w+)`?\s+(\w+)(?:\s*\(([^)]*)\))?(.*)$') {
        $col = New-Col $Matches[1] $Matches[2] $Matches[3] $Matches[4]
        return [pscustomobject]@{ Kind = 'column'; Col = $col }
    }
    return [pscustomobject]@{ Kind = 'unknown'; Text = $t }
}

$schema = [ordered]@{}                       # tabla(lower) -> objeto tabla
$insertedTables = @{}
$sqlStmtCount = 0
$sqlSummary = New-Object System.Collections.Generic.List[string]

function Add-ItemToTable($tbl, $it, [string]$loc) {
    switch ($it.Kind) {
        'column' {
            $c = $it.Col
            if ($tbl.Columns.Contains($c.Name)) {
                Add-Finding 'BLOQUEA' $loc "$($tbl.Name): la columna '$($c.Name)' ya existe (MySQL error 1060)."
                return
            }
            $tbl.Columns[$c.Name] = $c
            if ($c.InlinePk) { $tbl.Pk = @($c.Name) }
            if ($c.InlineUnique) { $tbl.Uniques.Add(@($c.Name)) }
        }
        'pk' { $tbl.Pk = $it.Cols }
        'unique' { $tbl.Uniques.Add($it.Cols) }
        'fk' {
            if ($it.RefTable -ne $tbl.Name -and -not $schema.Contains($it.RefTable) -and $script:fkChecks) {
                Add-Finding 'BLOQUEA' $loc "$($tbl.Name): la FK '$($it.Name)' referencia a '$($it.RefTable)' que todavia no existe en este punto del script (MySQL error 1824). Mover el CREATE de '$($it.RefTable)' antes, o diferir la FK con ALTER TABLE."
            }
            $tbl.Fks.Add($it)
        }
    }
}

function Read-SqlFile([string]$path, [string]$display) {
    $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
    $stmts = Split-SqlStatements $text $display
    $creates = 0; $alters = 0; $inserts = 0; $others = 0
    foreach ($st in $stmts) {
        $script:sqlStmtCount++
        $txt = $st.Text
        $loc = "$($st.File):$($st.Line)"

        if ($txt -match '(?is)^SET\b') {
            # SET FOREIGN_KEY_CHECKS=0/1 (tambien en formato mysqldump). Se toma la ultima asignacion.
            $fkm = [regex]::Matches($txt, '(?i)(?<![\w@])(?:@@(?:session\.|global\.)?)?FOREIGN_KEY_CHECKS\s*=\s*(@?\w+)')
            if ($fkm.Count -gt 0) {
                $v = $fkm[$fkm.Count - 1].Groups[1].Value
                $script:fkChecks = ($v -notmatch '^(0|OFF|FALSE)$')
            }
            $others++; continue
        }
        if ($txt -match '(?is)^(CREATE\s+DATABASE|CREATE\s+SCHEMA|USE|DROP\s+DATABASE)\b') { $others++; continue }

        if ($txt -match '(?is)^CREATE\s+TABLE\s+(IF\s+NOT\s+EXISTS\s+)?([\w`.]+)\s*\((.*)\)([^)]*)$') {
            $creates++
            $ifNotExists = [bool]$Matches[1]
            $orig = $Matches[2].Trim('`')
            if ($orig.Contains('.')) { $orig = $orig.Substring($orig.LastIndexOf('.') + 1).Trim('`') }
            $name = $orig.ToLower()
            $body = $Matches[3]
            $opts = $Matches[4]
            $cm0 = [regex]::Match($opts, '(?i)\bCOLLATE\s*=?\s*(\w+)')
            if ($cm0.Success) { $script:collations.Add($cm0.Groups[1].Value) }
            if ($schema.Contains($name)) {
                if ($ifNotExists) {
                    Add-Finding 'AVISO' $loc "CREATE TABLE IF NOT EXISTS '$name': la tabla ya existe (definida en $($schema[$name].Loc)); MySQL solo avisa y esta segunda definicion se ignora."
                }
                else {
                    Add-Finding 'BLOQUEA' $loc "CREATE TABLE '$name' pero la tabla ya existe (MySQL error 1050, definida en $($schema[$name].Loc)). Usar IF NOT EXISTS o quitar el duplicado."
                }
                continue
            }
            $tbl = [pscustomobject]@{
                Name = $name; OrigName = $orig; Loc = $loc
                Columns = (New-Object System.Collections.Specialized.OrderedDictionary)
                Pk = @()
                Fks = (New-Object System.Collections.Generic.List[object])
                Uniques = (New-Object System.Collections.Generic.List[object])
            }
            foreach ($item in (Split-Top $body)) {
                $it = Read-Item $item
                if ($it.Kind -eq 'unknown') {
                    Add-Finding 'INFO' $loc "${name}: item de CREATE no reconocido por el parser: '$($it.Text)'."
                    continue
                }
                Add-ItemToTable $tbl $it $loc
            }
            Set-PkNotNull $tbl
            $schema[$name] = $tbl
            continue
        }

        if ($txt -match '(?is)^ALTER\s+TABLE\s+([\w`.]+)\s+(.*)$') {
            $alters++
            $name = Get-NormName $Matches[1]
            $clauses = Split-Top $Matches[2]
            if (-not $schema.Contains($name)) {
                Add-Finding 'BLOQUEA' $loc "ALTER TABLE sobre '$name' que no existe todavia en este punto del script."
                continue
            }
            $tbl = $schema[$name]
            foreach ($cl in $clauses) {
                if ($cl -match '(?is)^ADD\s+(?:COLUMN\s+)?(.*)$') {
                    $it = Read-Item $Matches[1]
                    if ($it.Kind -eq 'unknown') { Add-Finding 'INFO' $loc "${name}: clausula ALTER no reconocida: '$cl'."; continue }
                    Add-ItemToTable $tbl $it $loc
                }
                elseif ($cl -match '(?is)^DROP\s+(?:COLUMN\s+)?`?(\w+)`?\s*$' -and $Matches[1] -notin 'PRIMARY', 'INDEX', 'KEY', 'FOREIGN', 'CONSTRAINT') {
                    $cn = $Matches[1].ToLower()
                    if ($tbl.Columns.Contains($cn)) { $tbl.Columns.Remove($cn) }
                    else { Add-Finding 'BLOQUEA' $loc "${name}: DROP COLUMN '$cn' pero la columna no existe." }
                }
                elseif ($cl -match '(?is)^MODIFY\s+(?:COLUMN\s+)?(.*)$') {
                    $it = Read-Item $Matches[1]
                    if ($it.Kind -eq 'column') { $tbl.Columns[$it.Col.Name] = $it.Col }
                }
                elseif ($cl -match '(?is)^CHANGE\s+(?:COLUMN\s+)?`?(\w+)`?\s+(.*)$') {
                    $old = $Matches[1].ToLower()
                    $it = Read-Item $Matches[2]
                    if ($it.Kind -eq 'column') {
                        if ($tbl.Columns.Contains($old)) { $tbl.Columns.Remove($old) }
                        $tbl.Columns[$it.Col.Name] = $it.Col
                    }
                }
                elseif ($cl -match '(?is)^DROP\s+(?:FOREIGN\s+KEY|CONSTRAINT)\s+`?(\w+)`?') {
                    $fk = $Matches[1].ToLower()
                    for ($k = $tbl.Fks.Count - 1; $k -ge 0; $k--) { if ($tbl.Fks[$k].Name -eq $fk) { $tbl.Fks.RemoveAt($k) } }
                }
                elseif ($cl -match '(?is)^(DROP\s+(INDEX|KEY)|RENAME\s+(INDEX|KEY)|ADD\s+INDEX|ENGINE|AUTO_INCREMENT|DEFAULT|CONVERT)\b') { }
                else { Add-Finding 'INFO' $loc "${name}: clausula ALTER no soportada por el parser (revisar a mano): '$cl'." }
            }
            Set-PkNotNull $tbl
            continue
        }

        if ($txt -match '(?is)^DROP\s+TABLE\s+(?:IF\s+EXISTS\s+)?([\w`.,\s]+)$') {
            $others++
            foreach ($tn in $Matches[1].Split(',')) {
                $n = Get-NormName $tn
                if ($schema.Contains($n)) { $schema.Remove($n) }
            }
            continue
        }

        if ($txt -match '(?is)^(?:INSERT|REPLACE)\s+(?:IGNORE\s+)?(?:INTO\s+)?([\w`.]+)\s*(?:\(([^)]*)\))?') {
            $inserts++
            $name = Get-NormName $Matches[1]
            $cols = $null
            if ($Matches[2]) { $cols = Get-NameList $Matches[2] }
            if (-not $schema.Contains($name)) {
                Add-Finding 'BLOQUEA' $loc "INSERT en '$name' que no existe en este punto del script."
                continue
            }
            $tbl = $schema[$name]
            if ($null -eq $cols) {
                Add-Finding 'AVISO' $loc "INSERT en '$name' sin lista de columnas: no se puede validar contra el esquema."
            }
            else {
                foreach ($cn in $cols) {
                    if (-not $tbl.Columns.Contains($cn)) { Add-Finding 'BLOQUEA' $loc "INSERT en '$name': la columna '$cn' no existe." }
                }
                foreach ($c in $tbl.Columns.Values) {
                    if ($c.NotNull -and -not $c.HasDefault -and -not $c.AutoInc -and ($cols -notcontains $c.Name)) {
                        Add-Finding 'BLOQUEA' $loc "INSERT en '$name': falta la columna NOT NULL sin default '$($c.Name)' (falla en modo estricto)."
                    }
                }
                foreach ($fk in $tbl.Fks) {
                    if ($fk.RefTable -ne $name -and ($cols | Where-Object { $fk.Cols -contains $_ }) -and -not $insertedTables.ContainsKey($fk.RefTable)) {
                        Add-Finding 'AVISO' $loc "INSERT en '$name' completa '$($fk.Cols -join ',')' (FK a '$($fk.RefTable)') pero antes no hay INSERT en '$($fk.RefTable)': verificar que la fila referenciada exista."
                    }
                }
            }
            # cantidad de valores por fila vs columnas ("Column count doesn't match value count")
            $vm = [regex]::Match($txt, '(?is)^[^()]*(?:\([^)]*\))?\s*VALUES?\s*(.*)$')
            if ($vm.Success) {
                $rows = New-Object System.Collections.Generic.List[object]
                $expected = $tbl.Columns.Count
                if ($null -ne $cols) { $expected = $cols.Count }
                $rn = 0
                foreach ($tuple in (Get-ValueTuples $vm.Groups[1].Value)) {
                    $rn++
                    $vals = Split-Top $tuple
                    if ($vals.Count -ne $expected) {
                        Add-Finding 'BLOQUEA' $loc "INSERT en '$name', fila ${rn}: $($vals.Count) valor(es) para $expected columna(s) (MySQL error 1136)."
                    }
                    elseif ($null -ne $cols) { $rows.Add($vals) }
                }
                if ($null -ne $cols -and $rows.Count -gt 0) {
                    $script:insertRecords.Add([pscustomobject]@{ Table = $name; Cols = $cols; Rows = $rows; Loc = $loc })
                }
            }
            $insertedTables[$name] = $true
            continue
        }

        $others++
    }
    $sqlSummary.Add("$display : $($stmts.Count) sentencias ($creates CREATE, $alters ALTER, $inserts INSERT, $others otras)")
}

# archivos SQL en orden
if (-not $SqlFiles -or $SqlFiles.Count -eq 0) {
    $SqlFiles = @('scripts.sql')
    $dataPath = Join-Path $Root 'data.sql'
    if ((Test-Path -LiteralPath $dataPath) -and ((Get-Item -LiteralPath $dataPath).Length -gt 0)) { $SqlFiles += 'data.sql' }
}
foreach ($f in $SqlFiles) {
    if ([System.IO.Path]::IsPathRooted($f)) { $p = $f } else { $p = Join-Path $Root $f }
    if (-not (Test-Path -LiteralPath $p)) { Write-Output "ERROR: no existe el archivo SQL '$f' (Root=$Root)"; exit 2 }
    Read-SqlFile $p $f
}

# ---------------------------------------------------------------- configuracion de Spring (solo claves puntuales)
# Se leen unicamente naming strategy, ddl-auto y sql.init: el resto del archivo (credenciales) no se toca ni se imprime.
$cfgText = ''
foreach ($cf in 'src/main/resources/application.yml', 'src/main/resources/application.yaml', 'src/main/resources/application.properties') {
    $cp = Join-Path $Root $cf
    if (Test-Path -LiteralPath $cp) { $cfgText += "`n" + [System.IO.File]::ReadAllText($cp, [System.Text.Encoding]::UTF8) }
}
$ddlAuto = $null
$m = [regex]::Match($cfgText, '(?im)^\s*(?:spring\.jpa\.hibernate\.)?ddl-auto\s*[:=]\s*([\w-]+)')
if ($m.Success) { $ddlAuto = $m.Groups[1].Value }
$m = [regex]::Match($cfgText, '(?im)^\s*(?:spring\.jpa\.hibernate\.naming\.)?physical[-_](?:naming[-_])?strategy\s*[:=]\s*([\w.$]+)')
if ($m.Success) {
    $ns = $m.Groups[1].Value
    if ($ns -match 'PhysicalNamingStrategyStandardImpl') { $script:namingMode = 'standard' }
    elseif ($ns -match 'CamelCaseToUnderscores|SpringPhysicalNamingStrategy') { $script:namingMode = 'snake' }
    else {
        $script:namingMode = 'snake'
        Add-Finding 'INFO' 'naming' "physical-strategy custom ($ns): el verificador asume snake_case de Spring; los nombres pueden no coincidir."
    }
}
$initMode = 'embedded'
$m = [regex]::Match($cfgText, '(?im)^\s*spring\.sql\.init\.mode\s*[:=]\s*(\w+)')
if (-not $m.Success) { $m = [regex]::Match($cfgText, '(?ims)^\s*sql:\s*\r?\n\s+init:\s*\r?\n(?:[ \t]+[^\r\n]*\r?\n)*?[ \t]+mode:\s*(\w+)') }
if ($m.Success) { $initMode = $m.Groups[1].Value }
$hasInitLocations = [regex]::IsMatch($cfgText, '(?i)(schema|data)-locations')
$dockerInit = @()
foreach ($dc in (Get-ChildItem -LiteralPath $Root -File -ErrorAction SilentlyContinue | Where-Object { $_.Name -match '^(docker-)?compose.*\.ya?ml$' })) {
    if (Select-String -LiteralPath $dc.FullName -Pattern 'docker-entrypoint-initdb\.d' -Quiet) { $dockerInit += $dc.Name }
}

# ---------------------------------------------------------------- modelo Java
$srcPath = Join-Path $Root $SourceDir
if (-not (Test-Path -LiteralPath $srcPath)) { Write-Output "ERROR: no existe '$SourceDir' (Root=$Root)"; exit 2 }
$javaFiles = Get-ChildItem -LiteralPath $srcPath -Recurse -Filter *.java

$enums = @{}      # nombre -> constantes
$classes = @{}    # nombre -> clase (entidad o mapped superclass)
$externalCols = New-Object System.Collections.Generic.List[object]   # FKs de @OneToMany+@JoinColumn (viven en la tabla hija)

function Get-Ann([string]$ann, [string]$name) {
    $m = [regex]::Match($ann, '@' + $name + '\b(?:\s*\(((?:[^()]|\([^()]*\))*)\))?')
    if (-not $m.Success) { return $null }
    return [string]$m.Groups[1].Value
}
function Get-AttrStr([string]$a, [string]$k) {
    $m = [regex]::Match($a, '(?<![\w.])' + $k + '\s*=\s*"([^"]*)"')
    if ($m.Success) { return $m.Groups[1].Value }
    return $null
}
function Get-AttrInt([string]$a, [string]$k) {
    $m = [regex]::Match($a, '(?<![\w.])' + $k + '\s*=\s*(\d+)')
    if ($m.Success) { return [int]$m.Groups[1].Value }
    return $null
}
function Get-AttrBool([string]$a, [string]$k) {
    $m = [regex]::Match($a, '(?<![\w.])' + $k + '\s*=\s*(true|false)')
    if ($m.Success) { return ($m.Groups[1].Value -eq 'true') }
    return $null
}

$fieldRx = [regex]'(?s)((?:@\w+(?:\s*\((?:[^()]|\([^()]*\))*\))?\s*)*)(?:private|protected|public)\s+(?!static\b)((?:final\s+|transient\s+|volatile\s+)*)([\w.]+(?:\s*<[^;=]*?>)?(?:\[\])?)\s+(\w+)\s*(?:=[^;]*)?;'

foreach ($jf in $javaFiles) {
    $raw = [System.IO.File]::ReadAllText($jf.FullName, [System.Text.Encoding]::UTF8)
    $txt = [regex]::Replace($raw, '(?s)/\*.*?\*/', '')
    $txt = [regex]::Replace($txt, '//[^\r\n]*', '')

    foreach ($em in [regex]::Matches($txt, '(?s)\benum\s+(\w+)[^{]*\{(.*?)(?:;|\})')) {
        $consts = @()
        foreach ($part in (Split-Top $em.Groups[2].Value)) {
            $cm = [regex]::Match($part, '^\s*(\w+)')
            if ($cm.Success) { $consts += $cm.Groups[1].Value }
        }
        $enums[$em.Groups[1].Value] = $consts
    }

    $isEntity = $txt -match '@Entity\b'
    $isMapped = $txt -match '@MappedSuperclass\b'
    if (-not ($isEntity -or $isMapped)) { continue }
    # (?<![.\w"]) evita tomar "Foo.class" (literal de clase) como declaracion
    $cm = [regex]::Match($txt, '(?s)(?<![.\w"])class\s+(\w+)(?:\s+extends\s+(\w+))?')
    if (-not $cm.Success) { continue }
    $cname = $cm.Groups[1].Value
    $header = $txt.Substring(0, $cm.Index)
    $tableName = $null
    if ($isEntity) {
        $tann = Get-Ann $header 'Table'
        if ($null -ne $tann) { $tableName = Get-AttrStr $tann 'name' }
        if (-not $tableName) { $tableName = $cname }
        $tableName = Get-PhysName $tableName
    }
    # generadores declarados a nivel clase (@SequenceGenerator suele ir ahi)
    $seqGens = @{}
    foreach ($sg in [regex]::Matches($txt, '@SequenceGenerator\s*\(((?:[^()]|\([^()]*\))*)\)')) {
        $gn = Get-AttrStr $sg.Groups[1].Value 'name'
        if ($gn) { $seqGens[$gn] = (Get-AttrStr $sg.Groups[1].Value 'sequenceName') }
    }
    $customGens = [regex]::IsMatch($txt, '@(GenericGenerator|TableGenerator|UuidGenerator)\b')
    $fields = New-Object System.Collections.Generic.List[object]
    $notes = New-Object System.Collections.Generic.List[string]
    $body = $txt.Substring($cm.Index)
    foreach ($fm in $fieldRx.Matches($body)) {
        $ann = $fm.Groups[1].Value
        $mods = $fm.Groups[2].Value
        $jtRaw = ($fm.Groups[3].Value -replace '\s+', '')
        $fname = $fm.Groups[4].Value
        if ($mods -match 'transient' -or $null -ne (Get-Ann $ann 'Transient')) { continue }
        $baseType = ($jtRaw -replace '<.*$', '')
        $baseType = $baseType.Substring($baseType.LastIndexOf('.') + 1)
        $isArray = $jtRaw.EndsWith('[]')

        $f = [ordered]@{
            Name = $fname; JavaType = $baseType; IsArray = $isArray; Column = $null
            Nullable = $true; Length = $null; Precision = $null; Scale = $null; Unique = $false
            IsId = $false; Gen = $null; EnumMode = $null; Relation = $null; Target = $null
            ColDef = $null; Skip = $null; SkipKind = $null
            ColumnRaw = $null; ImplicitJoin = $false
            GenName = $null; SeqName = $null; GenCustom = $false
        }
        $f.IsId = ($null -ne (Get-Ann $ann 'Id')) -or ($null -ne (Get-Ann $ann 'EmbeddedId'))
        $gv = Get-Ann $ann 'GeneratedValue'
        if ($null -ne $gv) {
            $sm = [regex]::Match($gv, 'strategy\s*=\s*(?:GenerationType\.)?(\w+)')
            if ($sm.Success) { $f.Gen = $sm.Groups[1].Value } else { $f.Gen = 'AUTO' }
            $f.GenName = Get-AttrStr $gv 'generator'
            if ($f.GenName -and $seqGens.ContainsKey($f.GenName)) { $f.SeqName = $seqGens[$f.GenName] }
            $f.GenCustom = $customGens -or ($null -ne (Get-Ann $ann 'GenericGenerator')) -or ($null -ne (Get-Ann $ann 'UuidGenerator')) -or ($null -ne (Get-Ann $ann 'TableGenerator'))
            $fsg = Get-Ann $ann 'SequenceGenerator'
            if ($null -ne $fsg) {
                $sqn = Get-AttrStr $fsg 'sequenceName'
                if ($sqn) { $f.SeqName = $sqn }
                if (-not $f.GenName) { $f.GenName = Get-AttrStr $fsg 'name' }
            }
        }

        $col = Get-Ann $ann 'Column'
        $jc = Get-Ann $ann 'JoinColumn'
        $rel = $null
        foreach ($r in 'ManyToOne', 'OneToOne', 'OneToMany', 'ManyToMany') {
            $ra = Get-Ann $ann $r
            if ($null -ne $ra) { $rel = $r; $relArgs = $ra }
        }

        if ($null -ne (Get-Ann $ann 'Embedded') -or $null -ne (Get-Ann $ann 'EmbeddedId')) {
            $f.Skip = "usa @Embedded/@EmbeddedId: el parser no lo expande, revisar a mano"
            $f.SkipKind = 'embedded'
        }
        elseif ($null -ne (Get-Ann $ann 'ElementCollection')) {
            $f.Skip = "@ElementCollection: requiere tabla aparte, revisar a mano"
        }
        elseif ($rel -eq 'OneToMany' -or $rel -eq 'ManyToMany') {
            if ($null -ne (Get-AttrStr $relArgs 'mappedBy')) { continue }
            # @OneToMany + @JoinColumn unidireccional: la FK vive en la tabla del hijo y la entidad hija no la mapea
            if ($rel -eq 'OneToMany' -and $null -ne $jc) {
                $jcn = Get-AttrStr $jc 'name'
                $tm = [regex]::Match($jtRaw, '<(?:\w+\.)*(\w+)>')
                if ($jcn -and $tm.Success) {
                    $externalCols.Add([pscustomobject]@{ Target = $tm.Groups[1].Value; Column = (Get-PhysName $jcn).ToLower(); Owner = "$cname.$fname" })
                    continue
                }
            }
            $jt = Get-Ann $ann 'JoinTable'
            $jtName = $null
            if ($null -ne $jt) { $jtName = Get-AttrStr $jt 'name' }
            if ($jtName) { $f.Skip = "$rel con tabla intermedia '$jtName': verificar que exista en el SQL" }
            else { $f.Skip = "$rel sin mappedBy ni @JoinTable: tabla intermedia por defecto, revisar a mano" }
        }
        elseif ($rel -eq 'ManyToOne' -or $rel -eq 'OneToOne') {
            if ($null -ne (Get-AttrStr $relArgs 'mappedBy')) { continue }
            $f.Relation = $rel
            $f.Target = $baseType
            $cn = $null
            if ($null -ne $jc) { $cn = Get-AttrStr $jc 'name' }
            if ($cn) { $f.Column = (Get-PhysName $cn).ToLower() }
            else {
                # sin @JoinColumn Hibernate usa <campo>_<columna PK referenciada>; se resuelve al final
                $f.ImplicitJoin = $true
                $f.Column = $null
            }
            $nb = $null
            if ($null -ne $jc) { $nb = Get-AttrBool $jc 'nullable' }
            if ($null -ne $nb) { $f.Nullable = $nb }
            $opt = Get-AttrBool $relArgs 'optional'
            if ($opt -eq $false) { $f.Nullable = $false }
        }
        else {
            $cn = $null
            if ($null -ne $col) {
                $cn = Get-AttrStr $col 'name'
                $nb = Get-AttrBool $col 'nullable'; if ($null -ne $nb) { $f.Nullable = $nb }
                $f.Length = Get-AttrInt $col 'length'
                $f.Precision = Get-AttrInt $col 'precision'
                $f.Scale = Get-AttrInt $col 'scale'
                $ub = Get-AttrBool $col 'unique'; if ($null -ne $ub) { $f.Unique = $ub }
                $f.ColDef = Get-AttrStr $col 'columnDefinition'
            }
            if (-not $cn) { $cn = $fname }
            $f.ColumnRaw = $cn
            $f.Column = (Get-PhysName $cn).ToLower()
            if ($f.IsId) { $f.Nullable = $false }
            if ($baseType -cmatch '^(int|long|boolean|double|float|short|byte)$') { $f.Nullable = $false }
            $en = Get-Ann $ann 'Enumerated'
            if ($null -ne $en) {
                if ($en -match 'STRING') { $f.EnumMode = 'STRING' } else { $f.EnumMode = 'ORDINAL' }
            }
            elseif ($enums.ContainsKey($baseType)) { $f.EnumMode = 'ORDINAL' }
            if ($null -ne (Get-Ann $ann 'Convert')) { $f.Skip = '@Convert: tipo no verificable, revisar a mano' }
            if ($null -ne (Get-Ann $ann 'Formula')) { continue }
        }
        $fields.Add([pscustomobject]$f)
    }
    $classes[$cname] = [pscustomobject]@{
        Name = $cname; File = $jf.FullName.Substring($Root.Length).TrimStart('\', '/')
        IsEntity = $isEntity; Table = $tableName; Extends = $cm.Groups[2].Value
        Fields = $fields; Inheritance = ($null -ne (Get-Ann $header 'Inheritance'))
    }
}

# ya con todos los enums (pueden aparecer despues de la entidad), re-etiquetar ORDINAL implicito
foreach ($c in $classes.Values) {
    foreach ($f in $c.Fields) {
        if ($null -eq $f.EnumMode -and $null -eq $f.Relation -and $enums.ContainsKey($f.JavaType)) { $f.EnumMode = 'ORDINAL' }
    }
}

# @ManyToOne/@OneToOne sin @JoinColumn: Hibernate usa <campo>_<columna de la PK referenciada>
foreach ($c in $classes.Values) {
    foreach ($f in $c.Fields) {
        if (-not $f.ImplicitJoin) { continue }
        $pkRaw = 'id'
        if ($classes.ContainsKey($f.Target)) {
            foreach ($tf in $classes[$f.Target].Fields) { if ($tf.IsId -and $tf.ColumnRaw) { $pkRaw = $tf.ColumnRaw } }
        }
        $f.Column = (Get-PhysName ($f.Name + '_' + $pkRaw)).ToLower()
    }
}

function Get-AllFields($cls) {
    $res = New-Object System.Collections.Generic.List[object]
    $chain = New-Object System.Collections.Generic.List[object]
    $cur = $cls; $guard = 0
    while ($null -ne $cur -and $guard -lt 10) {
        $chain.Insert(0, $cur)
        if ($cur.Extends -and $classes.ContainsKey($cur.Extends)) { $cur = $classes[$cur.Extends] } else { $cur = $null }
        $guard++
    }
    foreach ($c in $chain) { foreach ($f in $c.Fields) { $res.Add($f) } }
    return , $res.ToArray()
}

# ---------------------------------------------------------------- tipos Java -> SQL
function Get-SqlSpec($f) {
    $jt = $f.JavaType
    if ($f.IsArray -and $jt -eq 'byte') { return @{ A = @('BLOB', 'LONGBLOB', 'MEDIUMBLOB', 'VARBINARY', 'BINARY'); P = 'BLOB' } }
    if ($null -ne $f.EnumMode) {
        if ($f.EnumMode -eq 'STRING') {
            $l = 255; if ($null -ne $f.Length) { $l = $f.Length }
            return @{ A = @('VARCHAR'); P = "VARCHAR($l)" }
        }
        return @{ A = @('TINYINT', 'SMALLINT', 'INT'); P = 'TINYINT' }
    }
    switch -CaseSensitive ($jt) {
        { $_ -in 'Long', 'long', 'BigInteger' } { return @{ A = @('BIGINT'); P = 'BIGINT' } }
        { $_ -in 'Integer', 'int' } { return @{ A = @('INT', 'MEDIUMINT'); P = 'INT' } }
        { $_ -in 'Short', 'short' } { return @{ A = @('SMALLINT'); P = 'SMALLINT' } }
        { $_ -in 'Byte', 'byte' } { return @{ A = @('TINYINT'); P = 'TINYINT' } }
        { $_ -in 'Boolean', 'boolean' } { return @{ A = @('TINYINT', 'BIT'); P = 'BOOLEAN' } }
        'String' {
            $l = 255; if ($null -ne $f.Length) { $l = $f.Length }
            return @{ A = @('VARCHAR', 'TEXT', 'MEDIUMTEXT', 'LONGTEXT'); P = "VARCHAR($l)" }
        }
        'BigDecimal' {
            $p = 19; $s = 2
            if ($null -ne $f.Precision) { $p = $f.Precision }
            if ($null -ne $f.Scale) { $s = $f.Scale }
            return @{ A = @('DECIMAL'); P = "DECIMAL($p,$s)" }
        }
        { $_ -in 'Double', 'double' } { return @{ A = @('DOUBLE'); P = 'DOUBLE' } }
        { $_ -in 'Float', 'float' } { return @{ A = @('FLOAT'); P = 'FLOAT' } }
        'Timestamp' { return @{ A = @('DATETIME', 'TIMESTAMP'); P = 'TIMESTAMP' } }
        { $_ -in 'LocalDateTime', 'Instant', 'OffsetDateTime', 'ZonedDateTime', 'Date', 'Calendar' } { return @{ A = @('DATETIME', 'TIMESTAMP'); P = 'DATETIME' } }
        'LocalDate' { return @{ A = @('DATE'); P = 'DATE' } }
        'LocalTime' { return @{ A = @('TIME'); P = 'TIME' } }
        'UUID' { return @{ A = @('BINARY', 'CHAR', 'VARCHAR'); P = 'BINARY(16)' } }
    }
    return $null
}

# ---------------------------------------------------------------- comparacion
$entities = $classes.Values | Where-Object { $_.IsEntity } | Sort-Object Name
$entityByName = @{}
foreach ($e in $entities) { $entityByName[$e.Name] = $e }
$entityTables = @{}

function Get-PkField($ent) {
    foreach ($f in (Get-AllFields $ent)) { if ($f.IsId) { return $f } }
    return $null
}
function Get-NullSql($nullable) { if ($nullable) { return 'NULL' } else { return 'NOT NULL' } }

foreach ($ent in $entities) {
    $tname = $ent.Table.ToLower()
    $entityTables[$tname] = $ent
    $fields = Get-AllFields $ent
    $tbl = $null
    if ($schema.Contains($tname)) { $tbl = $schema[$tname] }
    $ewhere = "$($ent.Name)"

    if ($ent.Inheritance) { Add-Finding 'INFO' $ewhere "usa @Inheritance: el parser no modela la estrategia, revisar a mano." }
    if ($ent.Extends -and -not $classes.ContainsKey($ent.Extends) -and $ent.Extends -ne 'Object') {
        Add-Finding 'INFO' $ewhere "extiende '$($ent.Extends)' que no es una @MappedSuperclass del proyecto: columnas heredadas no verificadas."
    }

    # --- tabla
    if ($null -eq $tbl) {
        Add-Finding 'BLOQUEA' $ewhere "falta la tabla '$tname' (entidad en $($ent.File))."
        $fixCols = New-Object System.Collections.Generic.List[string]
        $fixFks = New-Object System.Collections.Generic.List[object]
        $pkCols = @()
        foreach ($f in $fields) {
            if ($null -ne $f.Skip -or [string]::IsNullOrEmpty($f.Column)) { continue }
            $sqlT = $null
            if ($f.Relation) {
                $sqlT = 'BIGINT'
                $refCol = 'id'
                if ($entityByName.ContainsKey($f.Target)) {
                    $tent = $entityByName[$f.Target]
                    $tpk = Get-PkField $tent
                    if ($null -ne $tpk) {
                        $ts = Get-SqlSpec $tpk
                        if ($ts) { $sqlT = $ts.P }
                        $refCol = $tpk.Column
                    }
                    $fixFks.Add([pscustomobject]@{ Col = $f.Column; RefTable = $tent.Table.ToLower(); RefCol = $refCol })
                }
            }
            elseif ($f.ColDef) { $sqlT = $f.ColDef }
            else {
                $sp = Get-SqlSpec $f
                if ($sp) { $sqlT = $sp.P } else { $sqlT = 'VARCHAR(255) /* TIPO ' + $f.JavaType + ' NO RECONOCIDO: revisar */' }
            }
            $line = "$($f.Column) $sqlT $(Get-NullSql (-not (-not $f.Nullable)))"
            if ($f.Gen -eq 'IDENTITY') { $line += ' AUTO_INCREMENT' }
            if ($f.IsId) { $pkCols += $f.Column }
            $fixCols.Add($line)
        }
        $Fixes.Add([pscustomobject]@{ Kind = 'create'; Table = $tname; Cols = $fixCols; Pk = $pkCols; Fks = $fixFks })
        continue
    }

    if ($tbl.OrigName -cne $ent.Table) {
        Add-Finding 'AVISO' $ewhere "Hibernate consultara la tabla '$($ent.Table)' (naming strategy: $($script:namingMode)) pero el SQL crea '$($tbl.OrigName)': en MySQL sobre Linux/Docker (case-sensitive) no coincide y falla. Unificar mayusculas."
    }

    $mapped = @{}
    foreach ($ex in $externalCols) {
        if ($ex.Target -ne $ent.Name) { continue }
        $mapped[$ex.Column] = $true
        if (-not $tbl.Columns.Contains($ex.Column)) {
            Add-Finding 'BLOQUEA' "$($ex.Owner) -> $tname.$($ex.Column)" "falta la columna '$($ex.Column)' en '$tname' (FK de la relacion @OneToMany + @JoinColumn de $($ex.Owner))."
            $Fixes.Add([pscustomobject]@{ Kind = 'addcol'; Table = $tname; Sql = "ALTER TABLE $tname ADD COLUMN $($ex.Column) BIGINT NULL;"; NotNull = $false })
        }
    }
    $hasUnmodeled = $ent.Inheritance -or (@($fields | Where-Object { $_.SkipKind -eq 'embedded' }).Count -gt 0) -or
        ($ent.Extends -and $ent.Extends -ne 'Object' -and -not $classes.ContainsKey($ent.Extends))
    foreach ($f in $fields) {
        if ($null -ne $f.Skip) {
            Add-Finding 'INFO' "$ewhere.$($f.Name)" $f.Skip
            if ([string]::IsNullOrEmpty($f.Column)) { continue }
        }
        if ([string]::IsNullOrEmpty($f.Column)) { continue }
        $mapped[$f.Column] = $true
        $w = "$ewhere.$($f.Name) -> $tname.$($f.Column)"

        # secuencia emulada (AUTO/SEQUENCE en MySQL)
        if ($f.IsId -and ($f.Gen -eq 'AUTO' -or $f.Gen -eq 'SEQUENCE' -or $f.Gen -eq 'TABLE')) {
            if ($f.JavaType -eq 'UUID') {
                # AUTO + UUID: Hibernate usa el generador UUID, sin tabla de secuencia
            }
            elseif ($f.SeqName) {
                if (-not $schema.Contains($f.SeqName.ToLower())) {
                    Add-Finding 'BLOQUEA' $w "@SequenceGenerator(sequenceName='$($f.SeqName)'): en MySQL Hibernate emula la secuencia con una tabla '$($f.SeqName)' que no existe."
                }
            }
            elseif ($f.GenCustom -or $f.GenName -or $f.Gen -eq 'TABLE') {
                Add-Finding 'INFO' $w "generador personalizado ($(if ($f.GenName) { $f.GenName } else { $f.Gen })): el verificador no deduce su tabla/secuencia, revisar a mano."
            }
            else {
                $seq = ($ent.Name + '_seq').ToLower()
                if (-not $schema.Contains($seq)) {
                    Add-Finding 'BLOQUEA' $w "@GeneratedValue($($f.Gen)) sobre $($f.JavaType) en MySQL usa una tabla de secuencia '$($ent.Name)_SEQ' (por defecto de Hibernate 6) que no existe. Usar GenerationType.IDENTITY o crear la tabla."
                }
            }
        }

        $col = $null
        if ($tbl.Columns.Contains($f.Column)) { $col = $tbl.Columns[$f.Column] }
        if ($null -eq $col) {
            Add-Finding 'BLOQUEA' $w "falta la columna '$($f.Column)' en la tabla '$tname'."
            $sqlT = 'BIGINT'
            if ($f.Relation) {
                if ($entityByName.ContainsKey($f.Target)) {
                    $tpk = Get-PkField $entityByName[$f.Target]
                    if ($null -ne $tpk) { $ts = Get-SqlSpec $tpk; if ($ts) { $sqlT = $ts.P } }
                }
            }
            elseif ($f.ColDef) { $sqlT = $f.ColDef }
            else { $sp = Get-SqlSpec $f; if ($sp) { $sqlT = $sp.P } else { $sqlT = 'VARCHAR(255)' } }
            $Fixes.Add([pscustomobject]@{
                Kind = 'addcol'; Table = $tname
                Sql = "ALTER TABLE $tname ADD COLUMN $($f.Column) $sqlT $(Get-NullSql (-not (-not $f.Nullable)));"
                NotNull = (-not $f.Nullable)
            })
        }
        else {
            # tipo
            $spec = $null
            if ($f.Relation) {
                if ($entityByName.ContainsKey($f.Target)) {
                    $tpk = Get-PkField $entityByName[$f.Target]
                    if ($null -ne $tpk) { $spec = Get-SqlSpec $tpk }
                }
            }
            elseif (-not $f.ColDef -and -not $f.Skip) {
                $spec = Get-SqlSpec $f
                if ($null -eq $spec) {
                    Add-Finding 'INFO' $w "tipo Java '$($f.JavaType)' no reconocido por el verificador: tipo SQL no comparado."
                }
            }
            if ($null -ne $spec -and $f.EnumMode -eq 'STRING' -and $col.Type -eq 'ENUM') {
                Add-Finding 'AVISO' $w "la columna es ENUM nativo de MySQL: desde Hibernate 6.2 cambio el mapeo de @Enumerated(STRING) en MySQL y validate podria rechazarlo (no verificable en forma estatica). Confirmarlo con un arranque real o usar VARCHAR."
            }
            elseif ($null -ne $spec) {
                if ($spec.A -notcontains $col.Type) {
                    Add-Finding 'BLOQUEA' $w "tipo incompatible: el SQL declara $(Format-Col $col) y Hibernate espera $($spec.A -join '/') (Java $($f.JavaType)). Hibernate validate lo rechaza."
                    $Fixes.Add([pscustomobject]@{
                        Kind = 'modify'; Table = $tname
                        Sql = "ALTER TABLE $tname MODIFY COLUMN $($f.Column) $($spec.P) $(Get-NullSql (-not $col.NotNull));  -- MODIFY redefine toda la columna: revisar DEFAULT / AUTO_INCREMENT"
                    })
                }
                else {
                    # longitud
                    if ($col.Type -eq 'VARCHAR' -and $null -ne $col.Len -and -not $f.Relation) {
                        $el = 255; if ($null -ne $f.Length) { $el = $f.Length }
                        if ($col.Len -lt $el) {
                            Add-Finding 'AVISO' $w "VARCHAR($($col.Len)) en SQL < length=$el en la entidad: valores largos se truncan o fallan al insertar."
                        }
                        if ($null -ne $f.EnumMode -and $enums.ContainsKey($f.JavaType)) {
                            $max = 0; $maxName = ''
                            foreach ($k in $enums[$f.JavaType]) { if ($k.Length -gt $max) { $max = $k.Length; $maxName = $k } }
                            if ($max -gt $col.Len) {
                                Add-Finding 'RUNTIME' $w "el enum $($f.JavaType) tiene la constante '$maxName' ($max car.) que no entra en VARCHAR($($col.Len))."
                            }
                        }
                    }
                    if ($col.Type -eq 'DECIMAL' -and ($null -ne $f.Precision -or $null -ne $f.Scale) -and -not $f.Relation) {
                        $ep = 19; $es = 2
                        if ($null -ne $f.Precision) { $ep = $f.Precision }
                        if ($null -ne $f.Scale) { $es = $f.Scale }
                        if ($col.Prec -ne $ep -or $col.Scale -ne $es) {
                            Add-Finding 'AVISO' $w "DECIMAL($($col.Prec),$($col.Scale)) en SQL vs precision/scale ($ep,$es) en la entidad."
                        }
                    }
                }
            }
            # nullabilidad
            if (-not $f.Nullable -and -not $col.NotNull -and -not $f.IsId) {
                Add-Finding 'AVISO' $w "la entidad exige no-null pero la columna SQL permite NULL."
            }
            elseif ($f.Nullable -and $col.NotNull -and -not $col.AutoInc) {
                Add-Finding 'AVISO' $w "la entidad acepta null pero la columna es NOT NULL: un null falla en el INSERT."
            }
            # unique
            if ($f.Unique) {
                $hasU = $false
                foreach ($u in $tbl.Uniques) { if ($u.Count -eq 1 -and $u[0] -eq $f.Column) { $hasU = $true } }
                if ($tbl.Pk.Count -eq 1 -and $tbl.Pk[0] -eq $f.Column) { $hasU = $true }
                if (-not $hasU) {
                    Add-Finding 'AVISO' $w "la entidad declara unique=true pero el SQL no tiene UNIQUE sobre '$($f.Column)'."
                    $Fixes.Add([pscustomobject]@{ Kind = 'unique'; Table = $tname; Sql = "ALTER TABLE $tname ADD CONSTRAINT uk_${tname}_$($f.Column) UNIQUE ($($f.Column));" })
                }
            }
        }

        # FK de la relacion
        if ($f.Relation) {
            if (-not $entityByName.ContainsKey($f.Target)) {
                Add-Finding 'INFO' $w "la entidad destino '$($f.Target)' no se encontro entre las entidades del proyecto: FK no verificada."
            }
            else {
                $tent = $entityByName[$f.Target]
                $rt = $tent.Table.ToLower()
                $tpk = Get-PkField $tent
                $rc = 'id'; if ($null -ne $tpk) { $rc = $tpk.Column }
                if (-not $schema.Contains($rt)) {
                    # ya se reporta como tabla faltante al procesar la entidad destino
                }
                $hasFk = $false; $otherRef = $null
                foreach ($fk in $tbl.Fks) {
                    if ($fk.Cols.Count -eq 1 -and $fk.Cols[0] -eq $f.Column) {
                        if ($fk.RefTable -eq $rt) { $hasFk = $true } else { $otherRef = $fk.RefTable }
                    }
                }
                if ($null -ne $otherRef) {
                    Add-Finding 'AVISO' $w "la FK de '$($f.Column)' apunta a '$otherRef' pero la entidad referencia a '$rt'."
                }
                elseif (-not $hasFk) {
                    Add-Finding 'AVISO' $w "la columna '$($f.Column)' no tiene FOREIGN KEY hacia '$rt' (Hibernate validate no lo exige, pero se pierde integridad referencial)."
                    $Fixes.Add([pscustomobject]@{
                        Kind = 'fk'; Table = $tname
                        Sql = "ALTER TABLE $tname ADD CONSTRAINT fk_${tname}_$($f.Column -replace '_id$','') FOREIGN KEY ($($f.Column)) REFERENCES $rt ($rc) ON DELETE RESTRICT ON UPDATE CASCADE;"
                    })
                }
            }
        }
    }

    # --- columnas del SQL que la entidad no mapea
    foreach ($c in $tbl.Columns.Values) {
        if ($mapped.ContainsKey($c.Name)) { continue }
        $w = "$tname.$($c.Name)"
        if ($hasUnmodeled) {
            Add-Finding 'INFO' $w "columna del SQL sin campo en $($ent.Name); la entidad usa @Embedded/@Inheritance/superclase no modelada, asi que puede pertenecer a ese mapeo (no se puede afirmar que falte)."
        }
        elseif ($c.NotNull -and -not $c.HasDefault -and -not $c.AutoInc) {
            Add-Finding 'RUNTIME' $w "columna NOT NULL sin default que la entidad $($ent.Name) no mapea: el arranque pasa, pero todo INSERT desde JPA falla. Mapearla en la entidad, o hacerla NULL / darle DEFAULT en el SQL."
        }
        else {
            Add-Finding 'INFO' $w "columna del SQL sin campo en $($ent.Name) (inocua para validate)."
        }
    }
}

# tablas del SQL sin entidad
foreach ($tn in $schema.Keys) {
    if (-not $entityTables.ContainsKey($tn)) {
        Add-Finding 'INFO' $tn "tabla del SQL sin entidad JPA asociada."
    }
}

# FK del SQL: tipos compatibles y columna referenciada existente
foreach ($tn in $schema.Keys) {
    $tbl = $schema[$tn]
    foreach ($fk in $tbl.Fks) {
        if (-not $schema.Contains($fk.RefTable)) {
            Add-Finding 'BLOQUEA' "$tn.$($fk.Cols -join ',')" "la FK '$($fk.Name)' referencia a la tabla '$($fk.RefTable)' que no existe en el esquema."
            continue
        }
        $rt = $schema[$fk.RefTable]
        for ($i = 0; $i -lt $fk.Cols.Count; $i++) {
            $cc = $null; $rc = $null
            if ($tbl.Columns.Contains($fk.Cols[$i])) { $cc = $tbl.Columns[$fk.Cols[$i]] }
            if ($i -lt $fk.RefCols.Count -and $rt.Columns.Contains($fk.RefCols[$i])) { $rc = $rt.Columns[$fk.RefCols[$i]] }
            if ($null -eq $cc) { Add-Finding 'BLOQUEA' "$tn.$($fk.Cols[$i])" "la FK '$($fk.Name)' usa una columna que no existe."; continue }
            if ($null -eq $rc) { Add-Finding 'BLOQUEA' "$tn.$($fk.Cols[$i])" "la FK '$($fk.Name)' referencia '$($fk.RefTable).$($fk.RefCols[$i])' que no existe."; continue }
            if ($cc.Type -ne $rc.Type -or $cc.Unsigned -ne $rc.Unsigned) {
                Add-Finding 'BLOQUEA' "$tn.$($cc.Name)" "FK '$($fk.Name)': tipo $(Format-Col $cc) incompatible con $($fk.RefTable).$($rc.Name) $(Format-Col $rc) (MySQL error 3780)."
            }
        }
    }
}

# valores de enum insertados vs constantes del enum Java (arranca bien y revienta al leer)
foreach ($ir in $script:insertRecords) {
    if (-not $entityTables.ContainsKey($ir.Table)) { continue }
    $ent = $entityTables[$ir.Table]
    $byCol = @{}
    foreach ($f in (Get-AllFields $ent)) { if ($f.Column -and $null -ne $f.EnumMode -and $enums.ContainsKey($f.JavaType)) { $byCol[$f.Column] = $f } }
    for ($ci = 0; $ci -lt $ir.Cols.Count; $ci++) {
        $cn = $ir.Cols[$ci]
        if (-not $byCol.ContainsKey($cn)) { continue }
        $ef = $byCol[$cn]
        $consts = $enums[$ef.JavaType]
        $rn = 0
        foreach ($row in $ir.Rows) {
            $rn++
            $raw = $row[$ci].Trim()
            if ($raw -match '^(?i)NULL$') { continue }
            if ($ef.EnumMode -eq 'STRING') {
                $sm = [regex]::Match($raw, "^'(.*)'$")
                if (-not $sm.Success) { $sm = [regex]::Match($raw, '^"(.*)"$') }
                if ($sm.Success -and ($consts -cnotcontains $sm.Groups[1].Value)) {
                    Add-Finding 'RUNTIME' "$($ir.Loc) $($ir.Table).$cn" "fila ${rn}: el valor '$($sm.Groups[1].Value)' no es una constante de $($ef.JavaType) ($($consts -join ', ')): la app revienta al leer esa fila."
                }
            }
            elseif ($raw -match '^\d+$' -and [int]$raw -ge $consts.Count) {
                Add-Finding 'RUNTIME' "$($ir.Loc) $($ir.Table).$cn" "fila ${rn}: el ordinal $raw no existe en $($ef.JavaType) ($($consts.Count) constantes)."
            }
        }
    }
}

# contexto de ejecucion de los .sql (lo que se asume y lo que se detecto en la configuracion)
if ($ddlAuto -and $ddlAuto -ne 'validate') {
    Add-Finding 'INFO' 'ejecucion' "ddl-auto=$ddlAuto en la configuracion: este verificador modela 'validate'; con '$ddlAuto' Hibernate puede crear/alterar el esquema por su cuenta."
}
if ($SqlFiles -contains 'data.sql' -and $initMode -ne 'always') {
    Add-Finding 'INFO' 'ejecucion' "data.sql esta en la lista, pero spring.sql.init.mode='$initMode': Spring no lo ejecuta contra MySQL salvo que sea 'always'. Si lo corres a mano, ignorar."
}
if ($hasInitLocations) {
    Add-Finding 'INFO' 'ejecucion' "la configuracion define schema-locations/data-locations: esos archivos los ejecuta Spring y no se analizan salvo que los pases en -SqlFiles."
}
if ($dockerInit.Count -gt 0) {
    Add-Finding 'INFO' 'ejecucion' "docker-entrypoint-initdb.d en $($dockerInit -join ', '): MySQL ejecuta esos .sql en orden alfabetico solo al crear el volumen; verificar que sean los de -SqlFiles."
}

# ---------------------------------------------------------------- orden topologico
function Get-TopoOrder([string[]]$tables, [hashtable]$deps) {
    $done = New-Object System.Collections.Generic.List[string]
    $left = New-Object System.Collections.Generic.List[string]
    foreach ($t in $tables) { $left.Add($t) }
    $progress = $true
    while ($left.Count -gt 0 -and $progress) {
        $progress = $false
        foreach ($t in @($left)) {
            $ok = $true
            foreach ($d in $deps[$t]) { if ($d -ne $t -and $done -notcontains $d -and $left -contains $d) { $ok = $false } }
            if ($ok) { $done.Add($t); [void]$left.Remove($t); $progress = $true }
        }
    }
    return [pscustomobject]@{ Order = $done.ToArray(); Cycle = $left.ToArray() }
}

$deps = @{}
foreach ($tn in $schema.Keys) {
    $d = @()
    foreach ($fk in $schema[$tn].Fks) { if ($fk.RefTable -ne $tn -and $schema.Contains($fk.RefTable)) { $d += $fk.RefTable } }
    $deps[$tn] = $d | Select-Object -Unique
}
$topo = Get-TopoOrder @($schema.Keys) $deps
if ($topo.Cycle.Count -gt 0) {
    Add-Finding 'AVISO' ($topo.Cycle -join ', ') "dependencia circular entre FKs: no hay orden de CREATE valido; hay que crear una de las FKs con ALTER TABLE posterior."
}

# ---------------------------------------------------------------- reporte
$levels = 'BLOQUEA', 'RUNTIME', 'AVISO', 'INFO'
$titles = @{
    'BLOQUEA' = 'BLOQUEA EL ARRANQUE (Hibernate validate o la ejecucion del script fallan)'
    'RUNTIME' = 'ROMPE EN RUNTIME (la app arranca pero fallan INSERTs / operaciones)'
    'AVISO'   = 'AVISOS (desalineaciones que no impiden arrancar)'
    'INFO'    = 'INFO'
}
$count = @{}
foreach ($l in $levels) { $count[$l] = @($Findings | Where-Object { $_.Level -eq $l }).Count }

Write-Output '# Verificacion de esquema: entidades JPA vs SQL'
Write-Output ''
Write-Output "- Entidades analizadas: $(@($entities).Count) | Tablas en el esquema efectivo: $($schema.Count)"
foreach ($s in $sqlSummary) { Write-Output "- SQL: $s" }
$ddlShow = $ddlAuto; if (-not $ddlShow) { $ddlShow = '(no detectado)' }
Write-Output "- Naming strategy: $($script:namingMode) | ddl-auto: $ddlShow | spring.sql.init.mode: $initMode"
Write-Output "- Alcance: compara las entidades con los .sql, NO con la base de datos viva."
Write-Output ''
if ($count['BLOQUEA'] -gt 0) {
    Write-Output "**VEREDICTO: NO ARRANCA** - $($count['BLOQUEA']) problema(s) bloqueante(s)."
}
elseif ($count['RUNTIME'] -gt 0) {
    Write-Output "**VEREDICTO: ARRANCA, PERO ROMPE EN RUNTIME** - $($count['RUNTIME']) problema(s) que fallan al operar."
}
else {
    Write-Output '**VEREDICTO: OK** - el esquema deberia pasar ddl-auto=validate (analisis estatico, no se ejecuto contra MySQL).'
}
Write-Output ''
Write-Output "| Nivel | Cantidad |"
Write-Output "|---|---|"
foreach ($l in $levels) { Write-Output "| $l | $($count[$l]) |" }

foreach ($l in $levels) {
    $items = @($Findings | Where-Object { $_.Level -eq $l })
    if ($items.Count -eq 0) { continue }
    Write-Output ''
    Write-Output "## [$l] $($titles[$l])"
    foreach ($it in $items) { Write-Output "- ``$($it.Where)``: $($it.Msg)" }
}

Write-Output ''
Write-Output '## Orden de ejecucion sugerido (segun FKs)'
$n = 1
foreach ($t in $topo.Order) { Write-Output "$n. $t"; $n++ }
if ($topo.Cycle.Count -gt 0) { Write-Output "- CICLO sin resolver: $($topo.Cycle -join ', ')" }

# ---- parche propuesto
Write-Output ''
if ($Fixes.Count -eq 0) {
    Write-Output '## Parche propuesto'
    Write-Output 'No hay nada que agregar.'
}
else {
    Write-Output '## Parche propuesto (NO aplicado; revisar antes de ejecutar)'
    Write-Output '```sql'
    # misma collation que las tablas existentes (si no, FKs sobre VARCHAR fallan)
    $patchCollate = 'utf8mb4_unicode_ci'
    if ($script:collations.Count -gt 0) {
        $patchCollate = ($script:collations | Group-Object | Sort-Object Count -Descending | Select-Object -First 1).Name
    }
    $patchCharset = ($patchCollate -split '_')[0]
    $known = New-Object System.Collections.Generic.List[string]
    foreach ($k in $schema.Keys) { $known.Add($k) }
    $creates = @($Fixes | Where-Object { $_.Kind -eq 'create' })
    $pending = New-Object System.Collections.Generic.List[object]
    foreach ($c in $creates) { $pending.Add($c) }
    $deferred = New-Object System.Collections.Generic.List[string]
    $progress = $true
    while ($pending.Count -gt 0) {
        $pick = $null
        foreach ($c in $pending) {
            $ok = $true
            foreach ($fk in $c.Fks) { if ($fk.RefTable -ne $c.Table -and $known -notcontains $fk.RefTable) { $ok = $false } }
            if ($ok) { $pick = $c; break }
        }
        $cyclic = $false
        if ($null -eq $pick) { $pick = $pending[0]; $cyclic = $true }
        [void]$pending.Remove($pick)
        $lines = New-Object System.Collections.Generic.List[string]
        foreach ($cl in $pick.Cols) { $lines.Add("    $cl") }
        if ($pick.Pk.Count -gt 0) { $lines.Add("    PRIMARY KEY ($($pick.Pk -join ', '))") }
        foreach ($fk in $pick.Fks) {
            $inline = ($fk.RefTable -eq $pick.Table) -or ($known -contains $fk.RefTable)
            if ($inline) {
                $lines.Add("    CONSTRAINT fk_$($pick.Table)_$($fk.Col -replace '_id$','') FOREIGN KEY ($($fk.Col)) REFERENCES $($fk.RefTable) ($($fk.RefCol)) ON DELETE RESTRICT ON UPDATE CASCADE")
            }
            else {
                $deferred.Add("ALTER TABLE $($pick.Table) ADD CONSTRAINT fk_$($pick.Table)_$($fk.Col -replace '_id$','') FOREIGN KEY ($($fk.Col)) REFERENCES $($fk.RefTable) ($($fk.RefCol)) ON DELETE RESTRICT ON UPDATE CASCADE;")
            }
        }
        Write-Output "CREATE TABLE $($pick.Table) ("
        Write-Output ($lines -join ",`n")
        Write-Output ") ENGINE=InnoDB DEFAULT CHARSET=$patchCharset COLLATE=$patchCollate;"
        Write-Output ''
        $known.Add($pick.Table)
    }
    foreach ($d in $deferred) { Write-Output $d }
    foreach ($kind in 'addcol', 'modify', 'unique', 'fk') {
        foreach ($fx in ($Fixes | Where-Object { $_.Kind -eq $kind })) {
            if ($kind -eq 'addcol' -and $fx.NotNull) {
                Write-Output "-- OJO: NOT NULL sin DEFAULT falla si la tabla ya tiene filas; agregar DEFAULT o cargar datos antes."
            }
            Write-Output $fx.Sql
        }
    }
    Write-Output '```'
}

if ($count['BLOQUEA'] -gt 0) { exit 1 } else { exit 0 }
