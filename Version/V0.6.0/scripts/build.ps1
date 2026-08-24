[CmdletBinding()]
param(
    [string[]]$Tasks,
    [switch]$Release
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Add-Type -AssemblyName System.Security

$sourceRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$buildRoot = 'C:\tmp\NetSerial-v060-build'
$toolRoot = 'C:\tmp\NetSerial-tools'
$jdkRoot = Join-Path $toolRoot 'jdk17'
$sdkRoot = Join-Path $toolRoot 'android-sdk'
$signingRoot = Join-Path $env:LOCALAPPDATA 'NetSerial-AI\signing'
$signingMetadataPath = Join-Path $signingRoot 'release-signing.json'
$signingSecretPath = Join-Path $signingRoot 'release-password.dpapi'
$plainPassword = $null

if (-not $PSBoundParameters.ContainsKey('Tasks')) {
    $Tasks = if ($Release) {
        @('testDebugUnitTest', 'lintRelease', 'assembleRelease')
    }
    else {
        @('testDebugUnitTest', 'lintDebug', 'assembleDebug')
    }
}

if ($buildRoot -ne 'C:\tmp\NetSerial-v060-build') {
    throw "Unexpected build mirror path: $buildRoot"
}
if (-not (Test-Path -LiteralPath (Join-Path $jdkRoot 'bin\java.exe'))) {
    throw "JDK 17 is missing from $jdkRoot"
}
if (-not (Test-Path -LiteralPath (Join-Path $sdkRoot 'platforms\android-36\android.jar'))) {
    throw "Android SDK Platform 36 is missing from $sdkRoot"
}
New-Item -ItemType Directory -Path $buildRoot -Force | Out-Null
$robocopyArgs = @(
    $sourceRoot,
    $buildRoot,
    '/MIR',
    '/R:2',
    '/W:1',
    '/NFL',
    '/NDL',
    '/NJH',
    '/NJS',
    '/NP',
    '/XD', '.git', '.gradle', 'build',
    '/XF', 'local.properties'
)
& robocopy.exe @robocopyArgs | Out-Null
if ($LASTEXITCODE -gt 7) {
    throw "Failed to mirror the project. Robocopy exit code: $LASTEXITCODE"
}

$env:JAVA_HOME = $jdkRoot
$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_SDK_ROOT = $sdkRoot

if ($Release) {
    if (-not (Test-Path -LiteralPath $signingMetadataPath) -or
        -not (Test-Path -LiteralPath $signingSecretPath)) {
        throw 'Release signing is not initialized for the current Windows user'
    }
    $metadata = Get-Content -LiteralPath $signingMetadataPath -Raw | ConvertFrom-Json
    $protectedPassword = [Convert]::FromBase64String(
        (Get-Content -LiteralPath $signingSecretPath -Raw).Trim()
    )
    $passwordBytes = [Security.Cryptography.ProtectedData]::Unprotect(
        $protectedPassword,
        $null,
        [Security.Cryptography.DataProtectionScope]::CurrentUser
    )
    try {
        $plainPassword = [Text.Encoding]::UTF8.GetString($passwordBytes)
    }
    finally {
        [Array]::Clear($passwordBytes, 0, $passwordBytes.Length)
        [Array]::Clear($protectedPassword, 0, $protectedPassword.Length)
    }
    if ([string]::IsNullOrWhiteSpace($plainPassword)) {
        throw 'Release signing secret could not be unlocked'
    }
    $env:NETSERIAL_RELEASE_STORE_FILE = [string]$metadata.storeFile
    $env:NETSERIAL_RELEASE_STORE_PASSWORD = $plainPassword
    $env:NETSERIAL_RELEASE_KEY_ALIAS = [string]$metadata.keyAlias
    $env:NETSERIAL_RELEASE_KEY_PASSWORD = $plainPassword
}

Push-Location $buildRoot
try {
    & .\gradlew.bat @Tasks --console=plain --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle failed with exit code $LASTEXITCODE"
    }
}
finally {
    Pop-Location
    if ($Release) {
        Remove-Item Env:NETSERIAL_RELEASE_STORE_FILE -ErrorAction SilentlyContinue
        Remove-Item Env:NETSERIAL_RELEASE_STORE_PASSWORD -ErrorAction SilentlyContinue
        Remove-Item Env:NETSERIAL_RELEASE_KEY_ALIAS -ErrorAction SilentlyContinue
        Remove-Item Env:NETSERIAL_RELEASE_KEY_PASSWORD -ErrorAction SilentlyContinue
        $plainPassword = $null
        [GC]::Collect()
    }
}
