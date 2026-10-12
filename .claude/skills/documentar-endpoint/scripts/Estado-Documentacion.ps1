<#
.SYNOPSIS
  Estado de la documentación OpenAPI (springdoc) de EmprenApp. Solo lee, no modifica nada.

.DESCRIPTION
  Recorre src/main/java y, por módulo, informa:
    - Controllers: @Tag a nivel clase, y por endpoint @Operation y @ApiResponse(s).
    - Request/Response (y DTOs expuestos por un controller): campos con @Schema.
  Con -Detalle lista cada elemento faltante con archivo:línea.

.PARAMETER Root       Raíz del proyecto.
.PARAMETER SourceDir  Carpeta de fuentes Java, relativa a Root.
.PARAMETER Modulo     Filtra por módulo (ej. categoria). Vacío = todos.
.PARAMETER Detalle    Lista cada faltante.

Código de salida: 0 ok (haya o no faltantes), 2 error de uso.
#>
param(
    [string]$Root = '.',
    [string]$SourceDir = 'src/main/java',
    [string]$Modulo = '',
    [switch]$Detalle
)

$ErrorActionPreference = 'Stop'

try {
    $rootPath = (Resolve-Path -LiteralPath $Root).Path
} catch {
    Write-Output "ERROR: no existe la raíz '$Root'"
    exit 2
}
$src = Join-Path $rootPath $SourceDir
if (-not (Test-Path -LiteralPath $src)) {
    Write-Output "ERROR: no existe la carpeta de fuentes '$src'"
    exit 2
}

function Get-Modulo([string]$path) {
    $parts = $path -split '[\\/]'
    $i = [array]::IndexOf($parts, 'emprenApp')
    if ($i -ge 0 -and ($i + 1) -lt $parts.Length - 1) { return $parts[$i + 1] }
    return '(raiz)'
}

function Get-Rel([string]$path) {
    return $path.Substring($rootPath.Length).TrimStart('\', '/') -replace '\\', '/'
}

$javaFiles = Get-ChildItem -LiteralPath $src -Recurse -Filter *.java -File
$allText = @{}
foreach ($f in $javaFiles) { $allText[$f.FullName] = Get-Content -LiteralPath $f.FullName -Encoding UTF8 }

# DTOs que algún controller expone (aparecen por nombre en un archivo @RestController).
$dtosExpuestos = New-Object System.Collections.Generic.HashSet[string]
foreach ($f in $javaFiles) {
    $txt = ($allText[$f.FullName] -join "`n")
    if ($txt -match '@RestController') {
        foreach ($m in [regex]::Matches($txt, '\b(\w+DTO)\b')) { [void]$dtosExpuestos.Add($m.Groups[1].Value) }
    }
}

$controllers = New-Object System.Collections.Generic.List[object]
$modelos = New-Object System.Collections.Generic.List[object]
$faltantes = New-Object System.Collections.Generic.List[object]

function Add-Faltante($mod, $file, $line, $elem, $falta) {
    $faltantes.Add([pscustomobject]@{ Modulo = $mod; Archivo = $file; Linea = $line; Elemento = $elem; Falta = $falta })
}

$mapRx = '@(Get|Post|Put|Delete|Patch|Request)Mapping\b'

foreach ($f in $javaFiles) {
    $lines = $allText[$f.FullName]
    $txt = $lines -join "`n"
    $mod = Get-Modulo $f.FullName
    if ($Modulo -and $mod -ne $Modulo) { continue }
    $rel = Get-Rel $f.FullName
    $name = [IO.Path]::GetFileNameWithoutExtension($f.Name)

    $esController = $txt -match '@RestController'
    $esModelo = ($rel -match '/infrastructure/(request|response)/') -or ($name -like '*DTO' -and $dtosExpuestos.Contains($name))
    if ($esController -and $rel -match '/shared/') { $esController = $false }
    if (-not $esController -and -not $esModelo) { continue }

    $buf = ''
    $bufStart = 0
    $info = [pscustomobject]@{ Modulo = $mod; Archivo = $rel; Clase = $name; Tag = $false; Endpoints = 0; ConOperation = 0; ConApiResponse = 0 }
    $campos = 0; $conSchema = 0
    $dentroClase = $false

    for ($n = 0; $n -lt $lines.Length; $n++) {
        $line = $lines[$n]
        $t = $line.Trim()
        if ($t -eq '' -or $t.StartsWith('//') -or $t.StartsWith('*') -or $t.StartsWith('/*')) { continue }
        if ($buf -eq '') { $bufStart = $n + 1 }

        if ($t -match '^(public\s+)?(final\s+)?(abstract\s+)?(class|record|interface|enum)\s+\w+') {
            if ($esController -and -not $dentroClase) {
                $info.Tag = ($buf -match '@Tag\b')
                if (-not $info.Tag) { Add-Faltante $mod $rel ($n + 1) $name '@Tag' }
            }
            $dentroClase = $true
            $buf = ''
            continue
        }

        if ($esController -and $dentroClase -and $t -match '^public\s+[^=;]*\(') {
            if ($buf -match $mapRx) {
                $info.Endpoints++
                $metodo = ([regex]::Match($t, '(\w+)\s*\(')).Groups[1].Value
                if ($buf -match '@Operation\b') { $info.ConOperation++ } else { Add-Faltante $mod $rel $bufStart "$name.$metodo" '@Operation' }
                if ($buf -match '@ApiResponses?\b') { $info.ConApiResponse++ } else { Add-Faltante $mod $rel $bufStart "$name.$metodo" '@ApiResponse' }
            }
            $buf = ''
            continue
        }

        if ($esModelo -and $dentroClase -and $t -match '^private\s+(?!static)[\w<>\[\], ?.]+\s+(\w+)\s*(=[^;]*)?;') {
            $campo = $Matches[1]
            $campos++
            if ($buf -match '@Schema\b') { $conSchema++ } else { Add-Faltante $mod $rel ($n + 1) "$name.$campo" '@Schema' }
            $buf = ''
            continue
        }

        if ($t.StartsWith('@') -or $buf -ne '') {
            $buf += ' ' + $t
            if ($t.EndsWith(';') -or $t.EndsWith('{') -or $t.EndsWith('}')) { $buf = '' }
        }
    }

    if ($esController) { $controllers.Add($info) }
    if ($esModelo) {
        $tipo = if ($rel -match '/request/') { 'request' } elseif ($rel -match '/response/') { 'response' } else { 'dto' }
        $modelos.Add([pscustomobject]@{ Modulo = $mod; Archivo = $rel; Clase = $name; Tipo = $tipo; Campos = $campos; ConSchema = $conSchema })
    }
}

# ---- Reporte ----
$mods = @($controllers | ForEach-Object { $_.Modulo }) + @($modelos | ForEach-Object { $_.Modulo }) | Sort-Object -Unique
if (-not $mods) { Write-Output 'No se encontraron controllers ni request/response.'; exit 0 }

Write-Output '== Estado de documentación OpenAPI =='
Write-Output ''
foreach ($m in $mods) {
    Write-Output "[$m]"
    foreach ($c in ($controllers | Where-Object { $_.Modulo -eq $m })) {
        $tag = if ($c.Tag) { 'OK' } else { 'FALTA' }
        Write-Output ("  controller {0}: @Tag {1} | endpoints {2} | @Operation {3}/{2} | @ApiResponse {4}/{2}" -f $c.Clase, $tag, $c.Endpoints, $c.ConOperation, $c.ConApiResponse)
    }
    foreach ($d in ($modelos | Where-Object { $_.Modulo -eq $m } | Sort-Object Tipo, Clase)) {
        Write-Output ("  {0} {1}: @Schema {2}/{3} campos" -f $d.Tipo, $d.Clase, $d.ConSchema, $d.Campos)
    }
    Write-Output ''
}

$totEnd = ($controllers | Measure-Object Endpoints -Sum).Sum
$totOp = ($controllers | Measure-Object ConOperation -Sum).Sum
$totAr = ($controllers | Measure-Object ConApiResponse -Sum).Sum
$totCam = ($modelos | Measure-Object Campos -Sum).Sum
$totSch = ($modelos | Measure-Object ConSchema -Sum).Sum
$tagsOk = @($controllers | Where-Object { $_.Tag }).Count
Write-Output '== Resumen =='
Write-Output ("Controllers con @Tag: {0}/{1}" -f $tagsOk, $controllers.Count)
Write-Output ("Endpoints con @Operation: {0}/{1}" -f $totOp, $totEnd)
Write-Output ("Endpoints con @ApiResponse: {0}/{1}" -f $totAr, $totEnd)
Write-Output ("Campos con @Schema: {0}/{1}" -f $totSch, $totCam)
Write-Output ("Elementos sin documentar: {0}" -f $faltantes.Count)

if ($Detalle) {
    Write-Output ''
    Write-Output '== Faltantes =='
    foreach ($x in $faltantes) {
        Write-Output ("{0}:{1} | {2} | falta {3}" -f $x.Archivo, $x.Linea, $x.Elemento, $x.Falta)
    }
}
exit 0
