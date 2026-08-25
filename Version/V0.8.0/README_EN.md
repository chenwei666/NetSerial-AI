# NetSerial AI Operations Terminal V0.8.0

<img src="design/app-icon-512.png" width="144" alt="NetSerial AI app icon">

NetSerial AI is an Android terminal for network engineers. It combines USB Console, SSH, Telnet, multi-vendor command references, Tab/control keys, AI command drafts, change control, configuration comparison, and field network tools.

Developer: chenwei666

## New in V0.8.0

- A modern Material 3 task-card system across Home, search, Toolbox, and Settings with clearer hierarchy and larger touch targets.
- Eighteen AI entry points: OpenAI, Anthropic, Gemini, DeepSeek, Zhipu GLM, Qwen, Volcano Ark/Doubao, Tencent TokenHub/Hunyuan, Baidu Qianfan, Kimi, MiniMax, SiliconFlow, Groq, Mistral, xAI, OpenRouter, custom compatible APIs, and HTTPS Ollama.
- Dynamic upstream model catalogs with searchable selection, explicit refresh/cancel, local non-secret caching, offline fallback, and manual model-name fallback.
- A task-oriented Network Toolbox split into quick diagnostics, immediate results, CIDR calculation, and offline utilities.
- Bounded single-target checks for up to 16 TCP ports, a common operations port preset, address-property summaries, copy/share actions, and concurrent-task protection.
- Every V0.7.0 navigation, AI failover, advanced diagnostics, signed runbook, temporary transfer, and guarded batch capability remains available.

## New in V0.7.0

- A clean five-area shell: Home, Connections, Terminal, Toolbox, and Settings, with a tablet navigation rail.
- Global feature search, four favorites, and six recent items without duplicating global actions in terminal menus.
- Multi-provider AI failover with the final provider and attempt count shown; API keys remain encrypted by Android Keystore.
- Vendor-aware health analysis, IP/MAC/interface troubleshooting, LLDP/CDP parsing, redacted backups, and bounded SNMPv3 read-only planning.
- A secure toolkit for SSH-usable device credential aliases, tokenized temporary HTTP/read-only TFTP transfer, and RSA/SHA-256 signed runbook verification.
- Canary-first guarded batch foundations with per-target approval, precheck/change/verify/rollback stages, stop-on-failure, and rollback policy.
- Android 16 predictive-back support and Android 5.0–16 compatible runbook verification.

Temporary sharing binds only to private or loopback addresses and expires after ten minutes or three downloads. SNMPv3 currently produces a non-secret read-only query plan and does not actively scan. Keystore-backed device/AI credential storage requires Android 6.0+. AI, runbook, and batch results remain review-only until an authorized engineer approves execution.

## New in V0.6.0

- High-confidence offline identification of H3C Comware, Huawei VRP, Cisco IOS, and Ruijie RGOS, with automatic vendor selection for commands and Web access.
- Secure Web access wizard for vendor-specific HTTPS and optional HTTP enablement plus a new administrator account. Passwords exist only for the current send; previews and evidence are redacted.
- GitHub Latest update checks at startup (daily) or on demand, restricted to HTTPS GitHub Release URLs.
- One-tap AI diagnosis using redacted terminal output with any configured provider: causes, read-only checks, remediation draft, verification, and rollback. Local memory remains explicit opt-in.
- Operations center with read-only health, interface, VLAN, LLDP/CDP, and security playbooks, heuristic configuration compliance triage, and canary-first batch planning.
- Multi-session workspace that stores only non-sensitive SSH/Telnet metadata and opens separate Android tasks; passwords and private keys are never saved.
- Private configuration snapshots with redaction, normalization, SHA-256, comparison, and rollback-draft handoff.
- Tab completion 2.0 and custom command packs with vendor/view isolation, prefix-first ranking, context assistance, and secret/multi-line rejection.
- USB XMODEM-128 file send with CRC/checksum negotiation, retry, timeout, cancellation, progress, and preflight SHA-256, protected by R3 target/change gates.
- All V0.5.0 Material 3, dark mode, four themes, bilingual UI, SSH/Telnet/SFTP, and safety controls remain available.

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
5. Add any supported provider or compatible API in AI Settings, then ask AI to inspect, complete, diagnose, or draft a four-phase command plan.
6. Use Operations Center to identify a vendor, generate read-only playbooks, triage configuration compliance, or plan a canary batch.
7. For production work, create a change task first and export redacted Markdown/PDF evidence afterwards.

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
