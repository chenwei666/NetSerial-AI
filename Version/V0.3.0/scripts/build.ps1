[CmdletBinding()]
param(
    [string[]]$Tasks = @('testDebugUnitTest', 'lintDebug', 'assembleDebug')
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$sourceRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$buildRoot = 'C:\tmp\NetSerial-build'
$toolRoot = 'C:\tmp\NetSerial-tools'
$jdkRoot = Join-Path $toolRoot 'jdk17'
$sdkRoot = Join-Path $toolRoot 'android-sdk'

if ($buildRoot -ne 'C:\tmp\NetSerial-build') {
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

Push-Location $buildRoot
try {
    & .\gradlew.bat @Tasks --console=plain --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle failed with exit code $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}
