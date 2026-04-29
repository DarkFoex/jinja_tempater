[CmdletBinding()]
param(
    [string]$PackageRoot = (Join-Path (Split-Path -Parent $PSScriptRoot) "dist\dbeaver-jinja-templater-offline")
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $PackageRoot)) {
    throw "Package root not found: $PackageRoot"
}

$required = @(
    "README.md",
    "offline\README_OFFLINE_TRANSFER.md",
    "examples\sample_template.sql",
    "examples\sample_vars.json",
    "checksums.sha256"
)

$pluginJar = Get-ChildItem -Path $PackageRoot -Filter "*.jar" -File | Select-Object -First 1
if (-not $pluginJar) {
    throw "Plugin jar not found in package root"
}

$missing = @()
foreach ($relative in $required) {
    $path = Join-Path $PackageRoot $relative
    if (-not (Test-Path $path)) {
        $missing += $relative
    }
}

if ($missing.Count -gt 0) {
    throw ("Missing required files:`n - " + ($missing -join "`n - "))
}

$checksumPath = Join-Path $PackageRoot "checksums.sha256"
$expected = @{}
Get-Content -LiteralPath $checksumPath | ForEach-Object {
    if ([string]::IsNullOrWhiteSpace($_)) {
        return
    }
    $parts = $_ -split "\s+\*", 2
    if ($parts.Count -eq 2) {
        $expected[$parts[1]] = $parts[0]
    }
}

$failed = @()
Get-ChildItem -Path $PackageRoot -Recurse -File | Where-Object { $_.Name -ne "checksums.sha256" } | ForEach-Object {
    $relative = $_.FullName.Substring($PackageRoot.Length).TrimStart('\').Replace('\', '/')
    $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    if (-not $expected.ContainsKey($relative)) {
        $failed += "Missing checksum entry for $relative"
    } elseif ($expected[$relative] -ne $hash) {
        $failed += "Checksum mismatch for $relative"
    }
}

if ($failed.Count -gt 0) {
    throw ("Package validation failed:`n - " + ($failed -join "`n - "))
}

Write-Host "Package validation passed."
Write-Host "Plugin jar: $($pluginJar.Name)"

