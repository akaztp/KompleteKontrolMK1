# Downloads a portable JDK (Eclipse Temurin 21) and Apache Maven into the repository's
# .tools folder (git-ignored), so nothing is installed system-wide.
#
# Usage:  powershell -ExecutionPolicy Bypass -File scripts\setup-tools.ps1

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$root  = Split-Path -Parent $PSScriptRoot
$tools = Join-Path $root '.tools'
$dl    = Join-Path $tools 'downloads'
New-Item -ItemType Directory -Force -Path $dl | Out-Null

function Get-Sha256([string] $path) { (Get-FileHash -Algorithm SHA256 -Path $path).Hash.ToLowerInvariant() }
function Get-Sha512([string] $path) { (Get-FileHash -Algorithm SHA512 -Path $path).Hash.ToLowerInvariant() }

# ---------------------------------------------------------------------------
# JDK 21 (Eclipse Temurin, from adoptium.net)

$jdkDir = Join-Path $tools 'jdk'
if (-not (Test-Path (Join-Path $jdkDir 'bin\javac.exe')))
{
    Write-Host 'Resolving latest Temurin JDK 21 for Windows x64...'
    $api = 'https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse'
    $asset = (Invoke-RestMethod -Uri $api) | Select-Object -First 1
    $pkg = $asset.binary.package
    $zip = Join-Path $dl $pkg.name
    Write-Host "Downloading $($pkg.name) ($([math]::Round($pkg.size / 1MB)) MB)..."
    Invoke-WebRequest -Uri $pkg.link -OutFile $zip
    if ((Get-Sha256 $zip) -ne $pkg.checksum.ToLowerInvariant()) { throw "Checksum mismatch for $zip" }

    $tmp = Join-Path $tools 'jdk-extract'
    if (Test-Path $tmp) { Remove-Item -Recurse -Force $tmp }
    Expand-Archive -Path $zip -DestinationPath $tmp
    $inner = Get-ChildItem -Directory $tmp | Select-Object -First 1
    if (Test-Path $jdkDir) { Remove-Item -Recurse -Force $jdkDir }
    Move-Item $inner.FullName $jdkDir
    Remove-Item -Recurse -Force $tmp
    Write-Host "JDK installed to $jdkDir"
}
else { Write-Host "JDK already present in $jdkDir" }

# ---------------------------------------------------------------------------
# Apache Maven (from archive.apache.org)

$mavenVersion = '3.9.11'
$mvnDir = Join-Path $tools 'maven'
if (-not (Test-Path (Join-Path $mvnDir 'bin\mvn.cmd')))
{
    $name = "apache-maven-$mavenVersion-bin.zip"
    $base = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/$name"
    $zip  = Join-Path $dl $name
    Write-Host "Downloading $name..."
    Invoke-WebRequest -Uri $base -OutFile $zip
    $expected = ((Invoke-WebRequest -Uri "$base.sha512" -UseBasicParsing).Content -split '\s+')[0].Trim().ToLowerInvariant()
    if ((Get-Sha512 $zip) -ne $expected) { throw "Checksum mismatch for $zip" }

    $tmp = Join-Path $tools 'maven-extract'
    if (Test-Path $tmp) { Remove-Item -Recurse -Force $tmp }
    Expand-Archive -Path $zip -DestinationPath $tmp
    $inner = Get-ChildItem -Directory $tmp | Select-Object -First 1
    if (Test-Path $mvnDir) { Remove-Item -Recurse -Force $mvnDir }
    Move-Item $inner.FullName $mvnDir
    Remove-Item -Recurse -Force $tmp
    Write-Host "Maven installed to $mvnDir"
}
else { Write-Host "Maven already present in $mvnDir" }

Remove-Item -Recurse -Force $dl
Write-Host 'Done. Build with: powershell -ExecutionPolicy Bypass -File scripts\build.ps1'
