# Builds KompleteKontrolMK1.bwextension using the portable JDK and Maven in .tools
# (run scripts\setup-tools.ps1 once first). Maven's dependency cache is kept in .tools\m2
# so nothing is written to the user profile.
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts\build.ps1            # build only
#   powershell -ExecutionPolicy Bypass -File scripts\build.ps1 -Install   # build and copy to Bitwig's Extensions folder

param ([switch] $Install)

$ErrorActionPreference = 'Stop'
$root  = Split-Path -Parent $PSScriptRoot
$tools = Join-Path $root '.tools'

if (-not (Test-Path (Join-Path $tools 'jdk\bin\javac.exe')) -or -not (Test-Path (Join-Path $tools 'maven\bin\mvn.cmd')))
{
    throw 'JDK or Maven missing. Run scripts\setup-tools.ps1 first.'
}

$env:JAVA_HOME = Join-Path $tools 'jdk'
$env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:PATH
$mvn = Join-Path $tools 'maven\bin\mvn.cmd'
$repo = Join-Path $tools 'm2'

Push-Location $root
try
{
    & $mvn -B -q "-Dmaven.repo.local=$repo" clean package
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed with exit code $LASTEXITCODE" }
}
finally { Pop-Location }

$jar = Join-Path $root 'target\KompleteKontrolMK1.jar'
$ext = Join-Path $root 'target\KompleteKontrolMK1.bwextension'
Copy-Item $jar $ext -Force
Write-Host "Built $ext"

if ($Install)
{
    $docs = [Environment]::GetFolderPath('MyDocuments')
    $dest = Join-Path $docs 'Bitwig Studio\Extensions'
    if (-not (Test-Path $dest)) { throw "Bitwig Extensions folder not found: $dest" }
    Copy-Item $ext $dest -Force
    Write-Host "Installed to $dest"
}
