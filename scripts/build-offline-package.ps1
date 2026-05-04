[CmdletBinding()]
param(
    [string]$TychoTargetFile
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$distRoot = Join-Path $root "dist"
$packageRoot = Join-Path $distRoot "dbeaver-jinja-templater-offline"
$zipPath = Join-Path $distRoot "dbeaver-jinja-templater-offline.zip"
$pluginTarget = Join-Path $root "dbeaver-jinja-templater-plugin\target"

function Require-Command {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command not found: $Name"
    }
}

Require-Command "mvn"

if (Test-Path $distRoot) {
    Remove-Item -LiteralPath $distRoot -Recurse -Force
}

New-Item -ItemType Directory -Path $packageRoot | Out-Null

Push-Location $root
try {
    $mavenArgs = @("clean", "package")
    if ($TychoTargetFile) {
        $mavenArgs = @("-Dtycho.target.file=$TychoTargetFile") + $mavenArgs
    }
    & mvn @mavenArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed with exit code $LASTEXITCODE"
    }
} finally {
    Pop-Location
}

$pluginJar = Get-ChildItem -Path $pluginTarget -Filter "*.jar" -File |
    Where-Object { $_.Name -notmatch "sources|javadoc" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $pluginJar) {
    throw "Plugin jar was not found in $pluginTarget"
}

Copy-Item -LiteralPath $pluginJar.FullName -Destination $packageRoot

Copy-Item -LiteralPath (Join-Path $root "README.md") -Destination $packageRoot
Copy-Item -LiteralPath (Join-Path $root "DEPLOYMENT.md") -Destination $packageRoot

$offlineTarget = Join-Path $packageRoot "offline"
New-Item -ItemType Directory -Path $offlineTarget | Out-Null
Copy-Item -LiteralPath (Join-Path $root "offline\README_OFFLINE_TRANSFER.md") -Destination $offlineTarget

$examplesTarget = Join-Path $packageRoot "examples"
Copy-Item -LiteralPath (Join-Path $root "examples") -Destination $examplesTarget -Recurse

$checksumFile = Join-Path $packageRoot "checksums.sha256"
$hashLines = Get-ChildItem -Path $packageRoot -Recurse -File | ForEach-Object {
    $hash = Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256
    $relative = $_.FullName.Substring($packageRoot.Length).TrimStart('\')
    "{0} *{1}" -f $hash.Hash.ToLowerInvariant(), $relative.Replace('\', '/')
}
$hashLines | Set-Content -LiteralPath $checksumFile -Encoding UTF8

if (Test-Path $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}
Compress-Archive -Path (Join-Path $packageRoot "*") -DestinationPath $zipPath

Write-Host "Offline package created:"
Write-Host "  Directory: $packageRoot"
Write-Host "  Zip:       $zipPath"
