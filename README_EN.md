# NetSerial AI

> An Android switch terminal for network engineers, combining USB Console, SSH, controlled Telnet, command completion, vendor command libraries, and an AI copilot in one safety-focused workflow.

[中文](README.md) | [English](README_EN.md)

[![Release](https://img.shields.io/github/v/release/chenwei666/NetSerial-AI?label=release)](https://github.com/chenwei666/NetSerial-AI/releases/latest)
[![Android](https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white)](Version/V0.4.0/app/build.gradle)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

NetSerial AI is designed for engineers who operate H3C, Huawei, Cisco, and Ruijie switches. It keeps the direct and dependable workflow of a traditional serial terminal while adding remote connections, offline command assistance, configurable AI providers, and deterministic local safety gates. AI generates, checks, and explains drafts; the engineer always controls the final send action.

## Highlights

| Capability | Description |
|---|---|
| USB Console | Android OTG serial, baud-rate selection, HEX, line endings, flow control, control lines, and a background connection service. |
| SSH / SFTP | Password, keyboard-interactive, session-only private keys, jump hosts, keepalive, and SFTP with changed-key blocking. |
| Telnet | Controlled compatibility mode for legacy devices; disabled by default with a plaintext warning before every connection. |
| Real terminal controls | TAB sends the actual `0x09` byte, with ESC, Ctrl+C, Ctrl+Z, arrows, backspace, delete, and `?` shortcuts. |
| Vendor command library | Ten operations categories for H3C Comware, Huawei VRP, Cisco IOS, and Ruijie RGOS. |
| Offline completion | Local suggestions based on vendor, CLI mode, and current input, available without network access. |
| AI Copilot | Generate, review, complete, and explain switch commands from natural language, with optional sanitized terminal context. |
| Device memory | Store device role, vendor, CLI mode, and explicitly confirmed knowledge with expiry and safe import/export. |
| Local risk controls | R0–R4 command classification; high-risk confirmation and typed `EXECUTE` approval for restart, erase, and upgrade operations. |
| Change governance | Change tasks, maintenance windows, protected targets, exact management-address checks, target colors, and redacted evidence exports. |
| Config and network tools | Ordered configuration diff, rollback drafts, IPv4/IPv6, DNS, Ping, trace, TCP, MTU, and MAC/OUI tools. |
| Bilingual UI | Follow system, Simplified Chinese, or English, with terminal font size, encoding, and timeout settings. |

## AI providers

Create, test, and switch between multiple profiles inside the app. API keys are entered locally by the user.

- OpenAI
- Claude / Anthropic
- Google Gemini
- DeepSeek
- Qwen
- Kimi
- Ollama over HTTPS
- Custom OpenAI-compatible HTTPS endpoints

Credentials are protected with Android Keystore and AES-GCM. They are excluded from configuration exports, terminal logs, device memory, and source code. Changing a provider or endpoint requires credential re-entry to prevent an old key from being sent to a new destination.

## Command categories

All four vendors include these common operations groups:

1. Device information
2. Interface status and configuration
3. VLAN
4. Layer 3 interfaces
5. Routing
6. Spanning tree and loop troubleshooting
7. Link aggregation
8. Security and MAC
9. Troubleshooting
10. Save and backup

The library is an offline operations starting point, not a replacement for the official command reference for a specific model and software release. Engineers must verify interfaces, VLANs, addresses, and placeholders before sending a command.

## Safety workflow

```text
Natural-language goal / manual command
                 ↓
AI or offline library creates a draft
                 ↓
Local rules evaluate risk again (AI cannot lower it)
                 ↓
Engineer reviews, edits, and explicitly sends
                 ↓
R3 confirmation / typed EXECUTE for R4
                 ↓
USB, SSH, or controlled Telnet terminal
```

- AI and command-library results never execute automatically.
- Recent terminal context is disabled by default. When enabled, ANSI sequences are removed, sensitive text is redacted, and length is limited.
- Unknown SSH hosts require manual fingerprint verification; changed known-host keys block the connection.
- Telnet cannot provide transport encryption and should only be used temporarily on isolated, trusted management networks.
- Android backup and ordinary cleartext HTTP are disabled.

## Download and install

Production builds: [GitHub Releases](https://github.com/chenwei666/NetSerial-AI/releases/latest)

Current release: **V0.4.0**  
Requirement: **Android 5.0 / API 21 or later**

V0.4.0 APK SHA-256:

```text
f4036401ee19d0a6ec8e87008a837f4bfbda23abe703d2d6346108efded13262
```

If V0.3.0 Debug is installed, uninstall it before installing the production build because the signatures differ. Production releases after V0.3.1 will use the same long-term certificate for in-place upgrades.

## Quick start

1. Install the production APK, then connect a USB Console adapter through OTG or open the SSH / Telnet remote terminal.
2. Select the switch vendor and CLI mode in Device Profile and AI Memory.
3. Choose a preset provider or custom HTTPS endpoint in AI Settings, then enter the API key locally.
4. Ask AI to generate or review commands, or use offline completion and the categorized command library.
5. Verify the device model, software release, interfaces, VLANs, addresses, and risk warnings before manually sending anything.

Detailed documentation:

- [V0.4.0 Chinese guide](Version/V0.4.0/README.md)
- [V0.4.0 English guide](Version/V0.4.0/README_EN.md)
- [Remote connections and security](Version/V0.4.0/docs/REMOTE_CONNECTIONS.md)
- [AI provider compatibility](Version/V0.4.0/docs/AI_PROVIDER_COMPATIBILITY.md)
- [Architecture](Version/V0.4.0/docs/ARCHITECTURE.md)
- [Production release and signing](Version/V0.4.0/docs/RELEASE.md)
- [Test report](Version/V0.4.0/docs/TEST_REPORT.md)

## Build and verification

JDK 17, Android SDK Platform 36, and Build Tools 36.0.0 are required. On Windows PowerShell:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.4.0\scripts\build.ps1
```

V0.4.0 release gates:

- 31 test suites and all 105 unit tests passed.
- Release Lint: zero errors.
- APK ZIP alignment and Android V1/V2/V3 signatures verified.
- applicationId: `com.chenwei666.netserial`.

Production signing material is not stored in this repository. Complete source snapshots are preserved under `Version/V*`; historical versions are never overwritten.

## Current boundaries

- Continued hardware-matrix testing is required across real Android devices, USB serial chipsets, and switch software releases.
- SSH now supports private keys, keyboard-interactive authentication, password jump hosts, and SFTP; real-device compatibility still requires ongoing validation.
- Some Android vendors do not provide complete Traceroute or DF Ping tooling, so path MTU depends on system and ICMP policy.
- Telnet is plaintext by design. The app can constrain its use but cannot remove the protocol risk.
- Provider APIs, model names, and quotas may change; consult each provider's current documentation.

## Origin and license

This project extends Kai Morich's [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal), retaining its MIT license and upstream attribution. New functionality and maintenance: `chenwei666`.

See [LICENSE](LICENSE) and `UPSTREAM.md` in each version directory.
