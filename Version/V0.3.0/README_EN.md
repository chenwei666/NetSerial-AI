# NetSerial AI Operations Terminal V0.3.0

[中文](README.md) | [English](README_EN.md)

V0.3.0 retains USB serial, multi-provider AI, device memory, and offline completion while adding separate SSH/Telnet transport, a categorized command library, and complete app settings.

## Quick start

### USB Console

1. Connect a USB serial adapter through OTG and select its port and baud rate.
2. Open Device Profile and AI Memory, then select the switch vendor and current CLI mode.
3. Pick an offline suggestion while typing, or open the command library and filter by vendor and category.
4. TAB sends the actual `0x09` byte. AI and library results fill the editor only and never send automatically.

### SSH

1. Open SSH / Telnet Remote Terminal from the device-list menu.
2. Select SSH and enter host/IP, port, username, and the password for this connection.
3. A first connection displays host-key information. Verify its fingerprint through Console, a trusted operations platform, or the administrator before trusting it.
4. A changed key blocks the connection. Forget stored host keys in App Settings only after the device or key replacement has been independently verified.

### Telnet

1. Telnet is disabled by default. Read the warning and explicitly enable it in App Settings.
2. Every Telnet connection still requires plaintext-risk confirmation.
3. Log in manually inside the terminal. The app does not save Telnet usernames or passwords.
4. Use Telnet temporarily on an isolated, trusted management network only. Prefer SSH whenever available.

## Command library and AI

- Vendors: H3C Comware, Huawei VRP, Cisco IOS, and Ruijie RGOS.
- Categories: device information, interfaces, VLAN, Layer 3, routing, loop prevention/STP, link aggregation, security/MAC, troubleshooting, and save/backup.
- Replace and verify interface names, VLAN IDs, and RFC 5737 example addresses. Exact syntax depends on model and software release; vendor documentation and device `?`/TAB output remain authoritative.
- AI providers include OpenAI, Claude/Anthropic, Gemini, DeepSeek, Qwen, Kimi, Ollama over HTTPS, and custom OpenAI-compatible endpoints.
- AI can generate or review commands and use explicitly saved device memory. Every result is locally reclassified and remains a draft.
- R3 high-risk commands require confirmation. R4 restart, erase, and upgrade commands require typing `EXECUTE`.

## App settings

- Language: system default, Simplified Chinese, or English. Android 13+ application locales are declared.
- Telnet: disabled by default.
- Remote timeout: 2–60 seconds.
- Terminal size: 12/14/18/22 sp for both USB and remote terminals.
- Character encoding: UTF-8, GBK, or ISO-8859-1.
- SSH known hosts: can be cleared, after which every device requires fingerprint verification again.

## Security boundaries

- SSH enforces host-key verification. Unknown hosts require approval and changed known keys are rejected.
- SSH passwords are never written to preferences, files, logs, backups, or source. The password field disables state saving and autofill, and the remote terminal blocks screenshots.
- Telnet plaintext exposure cannot be removed by the app, so it is disabled by default and protected by two explicit confirmations.
- AI keys use Android Keystore AES-GCM. Endpoints require HTTPS. Android backup and application cleartext HTTP remain disabled.
- Terminal context is ANSI-cleaned, redacted, length-limited, and excluded from AI requests by default.

## Build

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

The script mirrors the project to `C:\tmp\NetSerial-build`, then runs `testDebugUnitTest`, `lintDebug`, and `assembleDebug` with JDK 17 and Android SDK 36.

## External acceptance required

- Installation, language switching, rotation, background restoration, and keyboard layout on a physical Android device.
- First trust, repeat connection, and changed-host-key rejection on at least one H3C, Huawei, Cisco, and Ruijie device.
- Telnet login, IAC negotiation, UTF-8/GBK output, and disconnect recovery on an isolated lab network.
- USB serial chipsets and real TAB feedback.
- Live AI providers with the user's own test accounts; development automation used no real key.
- The current APK is Debug-signed and is not a production Release.

Developer: chenwei666.
