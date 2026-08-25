# NetSerial AI

<img src="Version/V0.9.0/design/app-icon-512.png" width="144" alt="NetSerial AI app icon">

> An Android field terminal for network engineers, bringing USB Console, SSH/SFTP, controlled Telnet, multi-vendor switch commands, AI-assisted command review, and a safety-focused change workflow to one phone.

[中文](README.md) | [English](README_EN.md)

[![Release](https://img.shields.io/github/v/release/chenwei666/NetSerial-AI?label=release)](https://github.com/chenwei666/NetSerial-AI/releases/latest)
[![Android](https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white)](Version/V0.5.0/app/build.gradle)
[![Theme](https://img.shields.io/badge/UI-Material%203-6750A4)](Version/V0.5.0/README_EN.md)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

NetSerial AI is built for engineers operating H3C, Huawei, Cisco, and Ruijie switches. It works as a dependable USB serial terminal and also reaches remote or legacy equipment through SSH/SFTP and explicitly authorized Telnet. Offline commands, Tab completion, network diagnostics, configuration Diff, change evidence, and an AI copilot with built-in or custom compatible providers share the same guarded workflow. AI drafts, checks, and explains; the engineer always controls the final send action.

> Release status: V0.9.0 passed the automated release gates, was merged into `main`, and is now GitHub Latest. Compatibility on physical phones, switches, and owned AI accounts still requires authorized field acceptance.

> Development candidate: V0.9.1 was merged into `main` through [PR #11](https://github.com/chenwei666/NetSerial-AI/pull/11). It adds one-tap reading of the current USB/SSH/Telnet session with automatic vendor identification. A V0.9.1 Release has not been created, so Latest remains V0.9.0.

## Release status and version history

- Current latest stable release: [V0.9.0](https://github.com/chenwei666/NetSerial-AI/releases/latest), with full in-app AI chat, 18 provider entries, encrypted history, incident evidence, guided runbooks, configuration drift, and guarded change closure.
- V0.3.1 was the first long-term production-signed stable release. V0.5.0 uses the same certificate and supports in-place upgrades from V0.3.1 or V0.4.0.
- V0.2.0 and V0.3.0 are source-only historical checkpoints with no verified APK. V0.4.0 remains a historical candidate; V0.5.0, V0.6.0, and V0.7.0 are historical stable releases.
- See the complete [version history](VERSION_HISTORY_EN.md) and the repository-level [project changelog](CHANGELOG.md). Every immutable source archive remains under [`Version/`](Version/).

| Version | Status | Milestone |
|---|---|---|
| V0.1.0 | Trusted Debug baseline | USB serial, real Tab, offline completion, R0-R4 safety model |
| V0.2.0 | Historical source checkpoint | Multi-provider AI, encrypted API keys, local memory, four-vendor completion |
| V0.3.0 | Historical source checkpoint | SSH, controlled Telnet, categorized command library, bilingual settings |
| V0.3.1 | First stable production release | Production signing, complete release gates, in-place upgrade baseline |
| V0.4.0 | Release candidate | Change tasks, configuration Diff, network tools, SFTP, jump hosts |
| V0.5.0 | Historical stable release | Material 3, light/dark modes, four themes, operations dashboard, new icon |
| V0.6.0 | Local release candidate | Vendor detection, Web wizard, update checks, AI diagnosis, operations center, multi-session, snapshots, completion 2.0, XMODEM |
| V0.7.0 | Historical stable release | Five-area navigation, AI failover, diagnostics, secure transfer, signed runbooks, guarded batch |
| V0.8.0 | Development candidate | Modern task cards, 18 AI entries, upstream model catalogs, direct network toolbox |
| V0.9.0 | Latest stable release | Full in-app AI chat, encrypted history, incident evidence, guided runbooks, drift and change closure |
| V0.9.1 | Merged development candidate | One-tap active terminal read, redacted in-memory snapshot, and automatic vendor selection |

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
| AI operations assistant | Eighteen provider entries, multi-turn chat, encrypted history, device memory, cancel/retry/share, and guarded command loading. |
| AI memory | Device-scoped, confirmed, non-sensitive operations knowledge with expiry and import/export. |
| Change safety | Maintenance windows, production protection, exact target matching, R3/R4 gates, and redacted evidence. |
| Operations tools | Config Diff, rollback drafts, IPv4/IPv6, DNS, Ping, Traceroute, bounded multi-port TCP, MTU, address summaries, and MAC/OUI. |
| Appearance/language | System/light/dark, four themes, Simplified Chinese/English, font size, and charset settings. |
| V0.6 operations | Vendor detection, secure Web drafts, GitHub update checks, AI diagnosis, read-only playbooks, compliance triage, sessions, snapshots, and XMODEM. |

## AI providers

Presets cover 18 provider types including Zhipu GLM, Qwen, Doubao, Tencent TokenHub, Baidu Qianfan, Kimi, MiniMax, SiliconFlow, Groq, Mistral, xAI, OpenRouter, and HTTPS Ollama. Upstream model catalogs, cached filtering, manual fallback, and custom compatible APIs are supported. Credentials use Android Keystore and AES-GCM protection and are excluded from source, model caches, exports, terminal records, and device memory.

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

V0.7.0 APK SHA-256:

```text
962e23209f57b24203a917474f90bda44c250c6071178e84a3d9a1b171504b81
```

V0.4.0 and V0.5.0 use the same production certificate, so an in-place upgrade can retain app data.

## Documentation

- [V0.9.0 Chinese guide](Version/V0.9.0/README.md)
- [V0.9.0 English guide](Version/V0.9.0/README_EN.md)
- [Architecture](Version/V0.9.0/docs/ARCHITECTURE.md)
- [Security boundaries](Version/V0.9.0/docs/SECURITY.md)
- [Test report](Version/V0.9.0/docs/TEST_REPORT.md)
- [Release and signing](Version/V0.9.0/docs/RELEASE.md)

## Build

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.9.0\scripts\build.ps1
```

Complete sources are archived independently under `Version/V*`. Production signing material is never stored in the repository.

## Origin and license

This project extends Kai Morich's [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal), retaining the upstream MIT license and attribution. New features and maintenance: `chenwei666`.

See [LICENSE](LICENSE) and each version's `UPSTREAM.md`.
