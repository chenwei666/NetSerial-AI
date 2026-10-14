param([string]$Serial = 'emulator-5554', [string]$OutputDirectory)
$ErrorActionPreference = 'Stop'
$adb = 'C:\tmp\NetSerial-tools\android-sdk\platform-tools\adb.exe'
if (-not $OutputDirectory) { throw 'Provide a directory for synthetic test reports and own-view images.' }
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$original = (& $adb -s $Serial shell wm size) -join "`n"
if ($LASTEXITCODE -ne 0) { throw 'Cannot read display size.' }
$override = if ($original -match 'Override size: (\d+x\d+)') { $Matches[1] } else { $null }
try {
    foreach ($profile in @('tablet', 'phone')) {
        if ($profile -eq 'tablet') { & $adb -s $Serial shell wm size 1600x900 }
        else { & $adb -s $Serial shell wm size 600x1100 }
        if ($LASTEXITCODE -ne 0) { throw 'Cannot configure test display.' }
        foreach ($language in @('zh', 'en')) {
            foreach ($appearance in @('LIGHT', 'DARK')) {
                $label = "$profile-$language-$($appearance.ToLowerInvariant())"
                $output = & $adb -s $Serial shell am instrument -w -e appearance $appearance -e language $language -e screenshot true com.chenwei666.netserial.preview012.test/de.kai_morich.simple_usb_terminal.ToolboxInstrumentation
                $exit = $LASTEXITCODE
                $output | Out-File -LiteralPath (Join-Path $OutputDirectory "$label.txt") -Encoding utf8
                if ($exit -ne 0 -or ($output -join "`n") -notmatch 'PASS \d+ assertions' -or ($output -join "`n") -match '(?m)^FAIL ') { throw "$label failed; inspect its report." }
                $output | Select-String 'PASS \d+ assertions'
                foreach ($image in @('catalog', 'ping')) {
                    & $adb -s $Serial pull "/sdcard/Android/data/com.chenwei666.netserial.preview012/files/toolbox-$image.png" (Join-Path $OutputDirectory "$label-$image.png")
                    if ($LASTEXITCODE -ne 0) { throw 'Cannot retrieve own-view test image.' }
                }
            }
        }
    }
} finally {
    if ($override) { & $adb -s $Serial shell wm size $override }
    else { & $adb -s $Serial shell wm size reset }
    & $adb -s $Serial shell wm size
}
