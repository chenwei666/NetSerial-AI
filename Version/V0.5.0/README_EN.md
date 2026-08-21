# NetSerial AI Operations Terminal V0.5.0

NetSerial AI is an Android terminal for network engineers. It combines USB Console, SSH, Telnet, multi-vendor command references, Tab/control keys, AI command drafts, change control, configuration comparison, and field network tools.

Developer: chenwei666

## New in V0.5.0

- Modern Material 3 interface with consistent color, rounded cards, spacing, hierarchy, and touch targets.
- System-adaptive, fixed-light, and fixed-dark appearance modes applied consistently across the app.
- Ocean, Emerald, Violet, and Sunset accent themes, each with dedicated light and dark contrast palettes.
- An operations dashboard showing the current device profile, active change task, and shortcuts to remote terminal, AI, commands, network tools, change control, and settings.
- Built-in command favorites and recents. Long-press to favorite and filter by All, Favorites, or Recent. Terminal input is never captured.
- Optional keep-awake behavior for long console and maintenance sessions.
- Modernized USB device cards, bilingual device feedback, and terminal colors adapted for light/dark mode.

## Core capabilities

- USB serial driver discovery, baud rate, HEX, newline, flow control, control lines, and background receive.
- SSH/Telnet with host-key verification, password/keyboard-interactive authentication, session-only private keys, jump hosts, keepalive, and SFTP. Telnet is disabled by default and always displays its plaintext risk.
- Categorized H3C Comware, Huawei VRP, Cisco IOS, and Ruijie RGOS command references.
- Multiple built-in AI providers and custom OpenAI-compatible endpoints. API keys use Android Keystore-backed protection. AI output is a review-only draft and is never auto-sent.
- R3 confirmation, R4 `EXECUTE` confirmation, protected-device change gates, wrong-target protection, and sensitive-text redaction.
- IPv4/IPv6 CIDR, DNS, Ping, Traceroute, TCP, path MTU, MAC/OUI, port reference, configuration Diff, and rollback drafts.
- System, Simplified Chinese, and English language modes.

## Quick start

1. Verify the device name, vendor, environment, and management address in Device Profile.
2. Choose a USB device from the dashboard or open the SSH/Telnet terminal.
3. Use Tab, cursor keys, Ctrl+C, and the question-mark shortcut during CLI interaction.
4. Search the command library by vendor/category; tap to insert or copy, or long-press to favorite.
5. Add any supported provider or compatible API in AI Settings, then ask AI to inspect, complete, or draft a four-phase command plan.
6. For production work, create a change task first and export redacted Markdown/PDF evidence afterwards.

## Build

JDK 17 and the Android SDK are required. On Windows:

```powershell
.\scripts\build.ps1
```

On a workstation with production signing initialized:

```powershell
.\scripts\build.ps1 -Release
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/SECURITY.md](docs/SECURITY.md), [docs/TEST_REPORT.md](docs/TEST_REPORT.md), and [docs/RELEASE.md](docs/RELEASE.md).

## Field acceptance

Automated checks do not replace real-device testing. Before production use, validate the target Android versions, USB OTG chipsets, real switches from all supported vendors, SSH private-key/jump-host/SFTP paths, platform network tools, and your authorized AI accounts. Qualified engineers must review every AI, Diff, and rollback result.
