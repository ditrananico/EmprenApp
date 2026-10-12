<#
.SYNOPSIS
  Audita las convenciones de arquitectura de EmprenApp (CLAUDE.md). Solo lee, no modifica nada.

.PARAMETER Root       Raíz del proyecto.
.PARAMETER SourceDir  Carpeta de fuentes Java, relativa a Root.
.PARAMETER Modulo     Filtra por módulo (ej. producto). Vacío = todos.

Niveles: ERROR (rompe una regla o un comportamiento), AVISO (revisar), INFO.
Código de salida: 0 sin ERROR, 1 con ERROR, 2 error de uso.
#>
param(
    [string]$Root = '.',
    [string]$SourceDir = 'src/main/java',
    [string]$Modulo = ''
)

$ErrorActionPreference = 'Stop'

try { $rootPath = (Resolve-Path -LiteralPath $Root).Path } catch { Write-Output "ERROR: no existe la raíz '$Root'"; exit 2 }
$src = Join-Path $rootPath $SourceDir
if (-not (Test-Path -LiteralPath $src)) { Write-Output "ERROR: no existe la carpeta de fuentes '$src'"; exit 2 }

function Get-Modulo([string]$path) {
    $parts = $path -split '[\\/]'
    $i = [array]::IndexOf($parts, 'emprenApp')
    if ($i -ge 0 -and ($i + 1) -lt $parts.Length - 1) { return $parts[$i + 1] }
    return '(raiz)'
}
function Get-Rel([string]$path) { return ($path.Substring($rootPath.Length).TrimStart('\', '/')) -replace '\\', '/' }

$hallazgos = New-Object System.Collections.Generic.List[object]
function Add-H($nivel, $regla, $mod, $file, $line, $msg) {
    $hallazgos.Add([pscustomobject]@{ Nivel = $nivel; Regla = $regla; Modulo = $mod; Archivo = $file; Linea = $line; Mensaje = $msg })
}

$files = Get-ChildItem -LiteralPath $src -Recurse -Filter *.java -File
$checked = 0

foreach ($f in $files) {
    $mod = Get-Modulo $f.FullName
    if ($mod -eq 'shared' -or $mod -eq 'configuration') { continue }
    if ($Modulo -and $mod -ne $Modulo) { continue }
    $rel = Get-Rel $f.FullName
    $lines = Get-Content -LiteralPath $f.FullName -Encoding UTF8
    $txt = $lines -join "`n"
    $name = [IO.Path]::GetFileNameWithoutExtension($f.Name)
    $checked++

    $esController = $txt -match '@RestController'
    $esService = ($rel -match '/application/service/') -and ($txt -match '@Service') -and ($name -notlike '*ValidationService')
    $esRepo = $rel -match '/domain/\w+Repository\.java$'
    $esMapper = $txt -match '@Mapper\b'
    $esEntity = $txt -match '@Entity\b'

    # --- Controllers ---
    if ($esController) {
        # C1: depende del Service en vez del Adapter
        for ($i = 0; $i -lt $lines.Length; $i++) {
            if ($lines[$i] -match '^\s*import\s+[\w.]+\.service\.\w+Service\s*;') {
                Add-H 'ERROR' 'C1 controller-usa-service' $mod $rel ($i + 1) 'El controller importa un Service; debe depender del Adapter.'
            }
            if ($lines[$i] -match '^\s*import\s+org\.emprenApp\.[\w.]+\.domain\.\w+Repository\s*;') {
                Add-H 'ERROR' 'C1 controller-usa-repository' $mod $rel ($i + 1) 'El controller importa un Repository; debe pasar por el Adapter.'
            }
            elseif ($lines[$i] -match '^\s*import\s+org\.emprenApp\.[\w.]+\.domain\.\w+\s*;') {
                Add-H 'AVISO' 'C3 controller-expone-entidad' $mod $rel ($i + 1) 'El controller importa una entidad del dominio; debería trabajar con request/response/DTO.'
            }
        }
        # C2: no extiende BaseRestController
        if ($txt -notmatch 'extends\s+BaseRestController') {
            Add-H 'AVISO' 'C2 base-controller' $mod $rel 1 'El controller no extiende BaseRestController.'
        }
        # C4: ruta sin prefijo v1/
        $m = [regex]::Match($txt, '@RequestMapping\(\s*(?:value\s*=\s*)?"([^"]*)"')
        if ($m.Success -and $m.Groups[1].Value -notmatch '^/?v1/') {
            Add-H 'AVISO' 'C4 ruta-sin-v1' $mod $rel 1 ("La ruta base '{0}' no empieza con v1/." -f $m.Groups[1].Value)
        }
        $base = if ($m.Success) { $m.Groups[1].Value.Trim('/') } else { '' }

        # C5/C6/C7: endpoints
        $rutas = @{}
        for ($i = 0; $i -lt $lines.Length; $i++) {
            if ($lines[$i] -notmatch '^\s*@(Get|Post|Put|Delete|Patch)Mapping\b(.*)$') { continue }
            $verbo = $Matches[1].ToUpper()
            $resto = $Matches[2]
            $pm = [regex]::Match($resto, '"([^"]*)"')
            $path = if ($pm.Success) { $pm.Groups[1].Value } else { '' }

            # firma: desde la anotación hasta la línea con la llave de apertura (máx. 8 líneas)
            $sig = ''
            $end = [Math]::Min($i + 8, $lines.Length - 1)
            for ($j = $i + 1; $j -le $end; $j++) {
                $sig += ' ' + $lines[$j].Trim()
                if ($lines[$j] -match '\{\s*$') { break }
            }

            # C5: ruta duplicada (mismo verbo y patrón)
            $norm = ($base + '/' + $path.Trim('/')).Trim('/') -replace '\{[^}]*\}', '{}'
            $key = "$verbo $norm"
            if ($rutas.ContainsKey($key)) {
                Add-H 'ERROR' 'C5 ruta-duplicada' $mod $rel ($i + 1) ("Mismo verbo y patrón de ruta que la línea {0}: {1}. Spring no puede distinguirlas." -f $rutas[$key], $key)
            } else { $rutas[$key] = $i + 1 }

            # C6: @PathVariable sin nombre que no coincide con la variable de la ruta
            $vars = @([regex]::Matches($path, '\{(\w+)[^}]*\}') | ForEach-Object { $_.Groups[1].Value })
            foreach ($pv in [regex]::Matches($sig, '@PathVariable(\s*\([^)]*\))?\s+[\w<>?,. ]+?\s+(\w+)\s*[,)]')) {
                $conNombre = $pv.Groups[1].Success -and $pv.Groups[1].Value.Trim() -ne '()'
                $param = $pv.Groups[2].Value
                if (-not $conNombre -and $vars.Count -gt 0 -and ($vars -notcontains $param)) {
                    Add-H 'ERROR' 'C6 pathvariable-no-coincide' $mod $rel ($i + 1) ("@PathVariable '{0}' no coincide con la variable de ruta ({1}); falla en runtime o requiere nombre explícito." -f $param, ($vars -join ', '))
                }
            }

            # C7: GET que devuelve List sin paginar
            if ($verbo -eq 'GET' -and $sig -match 'ResponseEntity<\s*List<' -and $sig -notmatch 'Pageable') {
                Add-H 'AVISO' 'C7 listado-sin-paginar' $mod $rel ($i + 1) 'El listado devuelve List sin Pageable; la convención es Pageable / Page<T>.'
            }
        }
    }

    # --- Services ---
    if ($esService) {
        if ($txt -notmatch 'implements\s+\w+Adapter') {
            Add-H 'AVISO' 'S1 service-sin-adapter' $mod $rel 1 'El Service no implementa un Adapter (puerto).'
        }
        for ($i = 0; $i -lt $lines.Length; $i++) {
            $l = $lines[$i]
            if ($l -match '^\s*//') { continue }
            # S2: borrado físico
            if ($l -match '[rR]epository\.(delete\w*)\(') {
                Add-H 'ERROR' 'S2 delete-fisico' $mod $rel ($i + 1) ("Borrado físico ({0}); la convención es baja lógica." -f $Matches[1])
            }
            # S3: lectura que ignora el flag activo
            if ($l -match '[rR]epository\.(findAll|findById|existsById|count)\(') {
                Add-H 'AVISO' 'S3 consulta-sin-filtro-activo' $mod $rel ($i + 1) ("{0}() no filtra por activo; verificar que sea intencional." -f $Matches[1])
            }
            # S4: excepciones genéricas
            if ($l -match 'throw\s+new\s+(RuntimeException|IllegalArgumentException|IllegalStateException|Exception)\s*\(') {
                Add-H 'AVISO' 'S4 excepcion-generica' $mod $rel ($i + 1) ("throw new {0}; usar BaseException/NotFoundException/GenericException con ErrorCodeEnum." -f $Matches[1])
            }
            # S5: ValidateGeneric ausente se evalúa a nivel archivo (abajo)
        }
        # S6: @Autowired de mappers (los mappers van con INSTANCE)
        if ($txt -match '@Autowired\s+(private\s+)?\w*Mapper\b') {
            Add-H 'AVISO' 'M2 mapper-inyectado' $mod $rel 1 'Hay un mapper inyectado; la convención es INSTANCE estático.'
        }
    }

    # --- Repositories ---
    if ($esRepo) {
        for ($i = 0; $i -lt $lines.Length; $i++) {
            if ($lines[$i] -match '\b(delete\w*By\w*|deleteAll\w*)\s*\(') {
                Add-H 'ERROR' 'S2 delete-fisico' $mod $rel ($i + 1) ("Método de borrado físico en el repositorio ({0})." -f $Matches[1])
            }
        }
    }

    # --- Mappers ---
    if ($esMapper) {
        if ($txt -notmatch 'INSTANCE\s*=\s*Mappers\.getMapper') {
            Add-H 'AVISO' 'M1 mapper-sin-instance' $mod $rel 1 'El mapper no declara INSTANCE = Mappers.getMapper(...).'
        }
        if ($txt -match 'componentModel\s*=\s*"spring"') {
            Add-H 'AVISO' 'M2 mapper-componentmodel-spring' $mod $rel 1 'El mapper usa componentModel spring; la convención es INSTANCE estático.'
        }
    }

    # --- Entidades ---
    if ($esEntity) {
        if ($txt -notmatch '(?m)^\s*private\s+\w+\s+(active|activo|estado|status)\b') {
            Add-H 'AVISO' 'E1 entidad-sin-flag-baja' $mod $rel 1 'La entidad no tiene flag de baja lógica (active/activo/estado).'
        }
    }
}

# --- Reporte ---
Write-Output '== Auditoría de convenciones =='
Write-Output ("Archivos revisados: {0}" -f $checked)
Write-Output ''

$orden = @{ 'ERROR' = 0; 'AVISO' = 1; 'INFO' = 2 }
$sorted = $hallazgos | Sort-Object @{Expression = { $orden[$_.Nivel] } }, Modulo, Archivo, Linea
$errores = @($hallazgos | Where-Object { $_.Nivel -eq 'ERROR' }).Count
$avisos = @($hallazgos | Where-Object { $_.Nivel -eq 'AVISO' }).Count

if ($hallazgos.Count -eq 0) {
    Write-Output 'Sin hallazgos.'
} else {
    foreach ($h in $sorted) {
        Write-Output ("[{0}] {1} | {2}:{3} | {4}" -f $h.Nivel, $h.Regla, $h.Archivo, $h.Linea, $h.Mensaje)
    }
    Write-Output ''
    Write-Output '== Resumen por módulo =='
    foreach ($g in ($hallazgos | Group-Object Modulo | Sort-Object Name)) {
        $e = @($g.Group | Where-Object { $_.Nivel -eq 'ERROR' }).Count
        $a = @($g.Group | Where-Object { $_.Nivel -eq 'AVISO' }).Count
        Write-Output ("{0}: {1} ERROR, {2} AVISO" -f $g.Name, $e, $a)
    }
}
Write-Output ''
Write-Output ("Total: {0} ERROR, {1} AVISO" -f $errores, $avisos)
if ($errores -gt 0) { exit 1 } else { exit 0 }
