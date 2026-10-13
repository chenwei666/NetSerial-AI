# NetSerial AI Guided Troubleshooting V0.10.0

<img src="design/app-icon-512.png" width="144" alt="NetSerial AI app icon">

NetSerial AI is an Android terminal for network engineers. It combines USB Console, SSH, Telnet, multi-vendor command references, Tab/control keys, AI command drafts, change control, configuration comparison, and field network tools.

Developer: chenwei666

V0.10.0 production build: the production-signed APK passed local release gates. Publication and downloads: [v0.10.0 Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0).

## V0.10.0 · Start with a problem, not a command

Ten searchable tasks cover no internet, slow connections, DNS, switch login, ports, VLAN/gateway, device identification, switch health, configuration backup and AI help. Professional tools remain in purpose-based groups; favorites and recent tools are collapsible.

1. Enter one affected IP/domain and service port. Run bounded DNS, three Ping samples and one TCP check from this phone. Cancel at any time; no subnet scan.
2. Connect to a switch, choose its brand and review explained read-only commands. Copy one command, manually review/run it, tap Resume troubleshooting, import output and read the findings.
3. Review a question/report and open a fresh AI draft. Nothing is sent automatically. Local diagnosis works without AI.
4. Preview reports before sharing. Configuration snapshots require explicit review/save and may be incomplete.

Checks do not prove computer-adapter health, bandwidth, authentication or whole-network health. Unknown vendors do not generate guessed commands. Redacted, bounded handoff drafts are process-local. Internal addresses may remain after best-effort redaction. English/Chinese, existing themes and dark mode are retained; phone cards use one column, tablets two, large fonts one.

Run scripts/build.ps1 for JVM tests, Lint and a Debug APK. Existing toolchain: C:/tmp/NetSerial-tools; independent build mirror: C:/tmp/NetSerial-v0100-build. Debug uses **com.chenwei666.netserial.preview / NetSerial AI Preview** and does not overwrite production or migrate keys, sessions or history. Release uses the original application ID `com.chenwei666.netserial`, the established production certificate and versionCode 12. Maintainers use `scripts/build.ps1 -Release`; contributors need no production signing keys to build the isolated Debug variant.

All 246 unit tests, Release Lint (0 errors, 158 warnings), V1/V2/V3 production signatures and ZIP alignment checks passed. Production APK SHA-256: `1a081ef60a5208c881d737b389e2289addb36397f1f1f803f1f95c8d679afb16`. See the [test report](docs/TEST_REPORT.md) and [guided workflow validation](docs/BEGINNER_VALIDATION.md). Physical USB/SSH/Telnet devices and owned AI accounts still require field acceptance.

## New in V0.9.1

- After USB, SSH, or Telnet connects, Operations Center shows the active terminal and can read and identify it with one tap.
- The snapshot reuses recent terminal output, strips ANSI controls, redacts sensitive fields, and is bounded to 100,000 characters in memory only.
- H3C, Huawei, Cisco, and Ruijie fingerprinting automatically selects the matching operations context.
- Empty and disconnected states provide actionable guidance. Identification never opens a hidden connection or silently sends a device command.

## New in V0.9.0

- Full in-app AI conversations with multi-turn context, new/select/rename/delete history, cancellation, retry, copy, and redacted sharing.
- All 18 provider entries and upstream model selections are reused, with active-profile priority, bounded transient retries, and safe provider failover.
- Android 6.0+ encrypts bounded chat history with Android Keystore. History, device memory, and terminal context are redacted before transmission.
- Fenced command drafts are allow-list extracted and locally reclassified from R0 to R4. R4 remains blocked and every other command requires manual loading.
- Operations Center adds one-tap incident evidence planning and deep AI analysis. Read-only playbooks gain prompt, timeout, retry, and stop-on-first-error contracts.
- Configuration Diff adds drift severity and sensitive-change counts. Security review adds SSHv1, weak algorithm, SNMPv3, AAA, and central logging checks.
- Change tasks require complete precheck/change/verification/rollback plans before start and execution plus verification evidence before completion.

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
