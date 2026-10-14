$ErrorActionPreference = 'Stop'
$versionRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$resources = Join-Path $versionRoot 'app\src\main\res'
# Regression: every MainActivity layout must provide the resume control it binds.
$layouts = @('layout\activity_main.xml', 'layout-sw600dp\activity_main.xml')
foreach ($layout in $layouts) {
    $text = Get-Content -LiteralPath (Join-Path $resources $layout) -Raw -Encoding UTF8
    $null = [xml]$text
    if ($text -notmatch '@\+id/main_resume_guide') { throw "Missing resume control in $layout" }
}
foreach ($name in @('strings_beginner_home.xml', 'strings_beginner_guide.xml')) {
    [xml]$chinese = Get-Content -LiteralPath (Join-Path $resources "values\$name") -Raw -Encoding UTF8
    [xml]$english = Get-Content -LiteralPath (Join-Path $resources "values-en\$name") -Raw -Encoding UTF8
    $difference = Compare-Object @($chinese.resources.string.name | Sort-Object) @($english.resources.string.name | Sort-Object)
    if ($null -ne $difference) { throw "Translation resource keys differ for $name" }
}
Write-Output 'Guided resource contracts passed: phone/tablet resume controls and bilingual keys.'
