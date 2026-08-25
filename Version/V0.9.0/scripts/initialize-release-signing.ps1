[CmdletBinding()]
param(
    [string]$KeytoolPath = 'C:\tmp\NetSerial-tools\jdk17\bin\keytool.exe'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Add-Type -AssemblyName System.Security

$signingRoot = Join-Path $env:LOCALAPPDATA 'NetSerial-AI\signing'
$keystorePath = Join-Path $signingRoot 'netserial-release.p12'
$secretPath = Join-Path $signingRoot 'release-password.dpapi'
$metadataPath = Join-Path $signingRoot 'release-signing.json'
$statusPath = Join-Path $env:TEMP 'netserial-release-signing-status.json'
$alias = 'netserial-release'
$passwordText = $null
$confirmationText = $null
$passwordBytes = $null
$protectedPassword = $null
$temporaryKeystorePath = "$keystorePath.$PID.tmp"
$temporarySecretPath = "$secretPath.$PID.tmp"
$temporaryMetadataPath = "$metadataPath.$PID.tmp"

if (Test-Path -LiteralPath $statusPath) {
    Remove-Item -LiteralPath $statusPath -Force
}

function Write-Status([string]$status, [string]$message) {
    [pscustomobject]@{
        status = $status
        message = $message
        keystore = $keystorePath
        alias = $alias
        timestamp = (Get-Date).ToString('o')
    } | ConvertTo-Json | Set-Content -LiteralPath $statusPath -Encoding UTF8
}

function Read-ProtectedPassphrase([string]$promptText) {
    Add-Type -AssemblyName System.Windows.Forms
    Add-Type -AssemblyName System.Drawing

    $form = New-Object System.Windows.Forms.Form
    $form.Text = 'NetSerial AI 正式发布签名 / Production Signing'
    $form.Size = New-Object System.Drawing.Size(540, 205)
    $form.StartPosition = [System.Windows.Forms.FormStartPosition]::CenterScreen
    $form.FormBorderStyle = [System.Windows.Forms.FormBorderStyle]::FixedDialog
    $form.MaximizeBox = $false
    $form.MinimizeBox = $false
    $form.TopMost = $true
    $form.ShowInTaskbar = $true

    $label = New-Object System.Windows.Forms.Label
    $label.Location = New-Object System.Drawing.Point(18, 18)
    $label.Size = New-Object System.Drawing.Size(490, 52)
    $label.Text = $promptText
    $form.Controls.Add($label)

    $textBox = New-Object System.Windows.Forms.TextBox
    $textBox.Location = New-Object System.Drawing.Point(20, 76)
    $textBox.Size = New-Object System.Drawing.Size(485, 28)
    $textBox.UseSystemPasswordChar = $true
    $form.Controls.Add($textBox)

    $okButton = New-Object System.Windows.Forms.Button
    $okButton.Location = New-Object System.Drawing.Point(330, 116)
    $okButton.Size = New-Object System.Drawing.Size(82, 30)
    $okButton.Text = '确定 / OK'
    $okButton.DialogResult = [System.Windows.Forms.DialogResult]::OK
    $form.AcceptButton = $okButton
    $form.Controls.Add($okButton)

    $cancelButton = New-Object System.Windows.Forms.Button
    $cancelButton.Location = New-Object System.Drawing.Point(423, 116)
    $cancelButton.Size = New-Object System.Drawing.Size(82, 30)
    $cancelButton.Text = '取消 / Cancel'
    $cancelButton.DialogResult = [System.Windows.Forms.DialogResult]::Cancel
    $form.CancelButton = $cancelButton
    $form.Controls.Add($cancelButton)

    $form.Add_Shown({ $form.Activate(); $textBox.Focus() })
    try {
        if ($form.ShowDialog() -ne [System.Windows.Forms.DialogResult]::OK) {
            return $null
        }
        return $textBox.Text
    }
    finally {
        $textBox.Clear()
        $form.Dispose()
    }
}

try {
    if (-not (Test-Path -LiteralPath $KeytoolPath)) {
        throw 'JDK keytool is unavailable'
    }
    if ((Test-Path -LiteralPath $keystorePath) -or
        (Test-Path -LiteralPath $secretPath) -or
        (Test-Path -LiteralPath $metadataPath)) {
        throw 'Release signing files already exist; refusing to overwrite them'
    }

    $passwordText = Read-ProtectedPassphrase `
        "Enter a production-signing passphrase of at least 16 characters.`r`nKeep an offline backup: this passphrase protects every future APK update."
    $confirmationText = Read-ProtectedPassphrase `
        "Enter the same production-signing passphrase again to confirm."
    if ($null -eq $passwordText -or $null -eq $confirmationText) {
        throw 'Signing setup was cancelled'
    }
    if ($passwordText.Length -lt 16) {
        throw 'Passphrase must contain at least 16 characters'
    }
    if ($passwordText -cne $confirmationText) {
        throw 'Passphrases do not match'
    }

    New-Item -ItemType Directory -Path $signingRoot -Force | Out-Null
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent().Name
    & icacls.exe $signingRoot /inheritance:r /grant:r "${identity}:(OI)(CI)F" | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw 'Unable to restrict the signing directory ACL'
    }

    $env:NETSERIAL_NEW_SIGNING_PASSWORD = $passwordText
    & $KeytoolPath -genkeypair -alias $alias -keyalg RSA -keysize 4096 `
        -sigalg SHA256withRSA -validity 10000 -dname 'CN=chenwei666, O=chenwei666, C=CN' `
        -keystore $temporaryKeystorePath -storetype PKCS12 `
        -storepass:env NETSERIAL_NEW_SIGNING_PASSWORD `
        -keypass:env NETSERIAL_NEW_SIGNING_PASSWORD -noprompt
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $temporaryKeystorePath)) {
        throw 'keytool failed to create the production signing key'
    }

    $passwordBytes = [Text.Encoding]::UTF8.GetBytes($passwordText)
    $protectedPassword = [Security.Cryptography.ProtectedData]::Protect(
        $passwordBytes,
        $null,
        [Security.Cryptography.DataProtectionScope]::CurrentUser
    )
    [Convert]::ToBase64String($protectedPassword) |
        Set-Content -LiteralPath $temporarySecretPath -Encoding ASCII
    [pscustomobject]@{
        storeFile = $keystorePath
        keyAlias = $alias
        certificateSubject = 'CN=chenwei666, O=chenwei666, C=CN'
        createdAt = (Get-Date).ToString('o')
    } | ConvertTo-Json | Set-Content -LiteralPath $temporaryMetadataPath -Encoding UTF8

    Move-Item -LiteralPath $temporaryKeystorePath -Destination $keystorePath
    Move-Item -LiteralPath $temporarySecretPath -Destination $secretPath
    Move-Item -LiteralPath $temporaryMetadataPath -Destination $metadataPath

    Write-Status 'success' 'Production signing key created and protected for the current Windows user.'
    Write-Host ''
    Write-Host 'Signing key created successfully. This window can now close.' -ForegroundColor Green
}
catch {
    Write-Status 'failed' $_.Exception.Message
    exit 1
}
finally {
    Remove-Item Env:NETSERIAL_NEW_SIGNING_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $temporaryKeystorePath -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $temporarySecretPath -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $temporaryMetadataPath -Force -ErrorAction SilentlyContinue
    if ($null -ne $passwordBytes) {
        [Array]::Clear($passwordBytes, 0, $passwordBytes.Length)
    }
    if ($null -ne $protectedPassword) {
        [Array]::Clear($protectedPassword, 0, $protectedPassword.Length)
    }
    $passwordText = $null
    $confirmationText = $null
    [GC]::Collect()
}
