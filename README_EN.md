# NetSerial AI

<img src="Version/V0.5.0/design/app-icon-512.png" width="144" alt="NetSerial AI app icon">

> A modern Android switch terminal for network engineers, combining USB Console, SSH, controlled Telnet, multi-vendor commands, Tab completion, an AI copilot, and a safety-focused change workflow.

[中文](README.md) | [English](README_EN.md)

[![Release](https://img.shields.io/github/v/release/chenwei666/NetSerial-AI?label=release)](https://github.com/chenwei666/NetSerial-AI/releases/latest)
[![Android](https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white)](Version/V0.5.0/app/build.gradle)
[![Theme](https://img.shields.io/badge/UI-Material%203-6750A4)](Version/V0.5.0/README_EN.md)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

NetSerial AI is built for engineers operating H3C, Huawei, Cisco, and Ruijie switches. It preserves the direct workflow of a serial terminal while adding remote access, offline command assistance, compatible AI APIs, local memory, change evidence, and deterministic safety gates. AI drafts, checks, and explains; the engineer always controls the final send action.

## V0.5.0 highlights

- Modern Material 3 UI with system-adaptive, light, and dark modes.
- Ocean, Emerald, Violet, and Sunset accent themes.
- Operations dashboard with device, active-change, and quick-tool summaries.
- Built-in command favorites, recents, and scope filters.
- Optional keep-awake behavior and dedicated light/dark terminal colors.
- Full compatibility with the V0.4.0 USB, SSH/Telnet, SFTP, AI, change-gate, Diff, and network-tool capabilities.

## Capabilities

| Capability | Description |
|---|---|
| USB Console | Android OTG serial, baud rate, HEX, newline, flow control, control lines, and background receive. |
| SSH / SFTP | Password, keyboard-interactive, session-only private keys, jump hosts, keepalive, SFTP, and changed-key blocking. |
| Telnet | Controlled legacy compatibility; disabled by default with a plaintext warning before every connection. |
| Terminal keys | TAB sends the real `0x09` byte, with ESC, Ctrl+C, arrows, delete, and `?`. |
| Vendor commands | Ten categories for H3C Comware, Huawei VRP, Cisco IOS, and Ruijie RGOS. |
| Favorites/recents | Long-press built-in commands to favorite and filter by All, Favorites, or Recent. |
| AI Copilot | Built-in providers or custom OpenAI-compatible HTTPS APIs for generation, completion, review, and explanation. |
| AI memory | Device-scoped, confirmed, non-sensitive operations knowledge with expiry and import/export. |
| Change safety | Maintenance windows, production protection, exact target matching, R3/R4 gates, and redacted evidence. |
| Operations tools | Config Diff, rollback drafts, IPv4/IPv6, DNS, Ping, Traceroute, TCP, MTU, and MAC/OUI. |
| Appearance/language | System/light/dark, four themes, Simplified Chinese/English, font size, and charset settings. |

## AI providers

Presets cover OpenAI, Anthropic, Gemini, DeepSeek, Qwen, Kimi, and HTTPS Ollama. Custom OpenAI-compatible endpoints, models, and API keys are supported. Credentials use Android Keystore and AES-GCM protection and are excluded from source, exports, terminal records, and device memory.

## Safety workflow

```text
Natural-language goal / manual command
        → AI or offline command draft
        → deterministic local risk evaluation
        → engineer review and explicit send
        → R3 confirmation / typed EXECUTE for R4
        → USB, SSH, or controlled Telnet terminal
```

AI and command-library entries never auto-execute. Telnet should only be used temporarily on isolated, trusted management networks. Theme changes never relax production protection or command gates.

## Download

Production releases: [GitHub Releases](https://github.com/chenwei666/NetSerial-AI/releases/latest)

V0.5.0 APK SHA-256:

```text
c16a95034a45adbf3c38a9e24a0211aeb9d4197d52132420e47f4c00ab264cf3
```

V0.4.0 and V0.5.0 use the same production certificate, so an in-place upgrade can retain app data.

## Documentation

- [V0.5.0 Chinese guide](Version/V0.5.0/README.md)
- [V0.5.0 English guide](Version/V0.5.0/README_EN.md)
- [Architecture](Version/V0.5.0/docs/ARCHITECTURE.md)
- [Security boundaries](Version/V0.5.0/docs/SECURITY.md)
- [Test report](Version/V0.5.0/docs/TEST_REPORT.md)
- [Release and signing](Version/V0.5.0/docs/RELEASE.md)

## Build

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.5.0\scripts\build.ps1
```

Complete sources are archived independently under `Version/V*`. Production signing material is never stored in the repository.

## Origin and license

This project extends Kai Morich's [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal), retaining the upstream MIT license and attribution. New features and maintenance: `chenwei666`.

See [LICENSE](LICENSE) and each version's `UPSTREAM.md`.
