# NetSerial AI · V0.13.0 autonomous AI tasks

<p align="center">
  <img src="Version/V0.13.0/design/app-icon-512.png" width="112" alt="NetSerial AI app icon">
</p>

<p align="center">
  <strong>Your phone or tablet, your field network toolkit.</strong><br>
  Start with a symptom, or jump straight to a professional tool.<br>
  Root-free diagnostics · USB Console · SSH/SFTP · AI chat and controlled tasks
</p>

<p align="center">
  <a href="https://github.com/chenwei666/NetSerial-AI/releases/latest"><strong>Download the latest stable release</strong></a>
  · <a href="#get-started-in-three-steps">Quick start</a>
  · <a href="README.md">简体中文</a>
  · <a href="https://github.com/chenwei666/NetSerial-AI/issues">Report a problem / Suggest a feature</a>
</p>

<p align="center">
  <a href="https://github.com/chenwei666/NetSerial-AI/releases/latest"><img src="https://img.shields.io/github/v/release/chenwei666/NetSerial-AI?label=release" alt="GitHub Release"></a>
  <img src="https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white" alt="Minimum Android 5.0">
  <img src="https://img.shields.io/badge/languages-English%20%2F%20Chinese-006C93" alt="English and Chinese">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="MIT License"></a>
</p>

**NetSerial AI is an open-source Android network operations toolkit.** Start with no Internet access, an unreachable switch or a port problem; collect evidence, understand the result and decide what to check next. Experienced users can open Ping, DNS, TCP or TLS directly.

V0.13.0 adds explicit autonomous tasks to the **16-tool root-free toolkit** and confirmed AI memory. Start from the current device terminal, describe a goal, then let AI inspect state, request missing details and execute sequentially; high risks still require your approval.

> **V0.13.0 production release is authorized and being prepared.** Local production build, signing and in-place upgrade are verified; remote publication checks remain pending. This page does not prove assets are uploaded. [Planned release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.13.0) · [Planned formal APK](https://github.com/chenwei666/NetSerial-AI/releases/download/v0.13.0/NetSerial-AI-0.13.0.apk) (may be unavailable until publication). The published [V0.12.0](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.12.0) remains available; the actual [GitHub Latest](https://github.com/chenwei666/NetSerial-AI/releases/latest) target is determined remotely.

> **Ordinary chat remains draft-only; autonomous tasks require an explicit start.** Tasks bind the current connection, never a guessed latest device. Exact allowlisted queries and narrowly scoped VLAN/description changes can run automatically. High/unknown risk requires the full typed target; protected devices still require a matching change task and maintenance window.

## Autonomous tasks start from one connected device

- Connect USB Console / SSH / Telnet, complete login and reach a ready CLI prompt. Open AI from that terminal → **AI auto-execute**. Only the selected provider/model is used, without silent failover.
- Strict single-action JSON, local risk rules, identification, state prechecks and read-back checks—not unrestricted command execution. Maximum 32 steps/10 minutes; missing parameters prompt a question. Errors, paging/interaction, timeout, disconnect, cancellation or leaving the page stop execution, without automatic retry/resume.
- Receipts distinguish actual steps and uncertainty. Sent commands may have taken effect; no automatic startup-config save or rollback. A model summary is not proof of success.
- Task-page memory controls share the ordinary-chat preference and select at most five confirmed memories; tasks never automatically save new memories or treat them as execution authority.
- **Web accounts and HTTPS:** WEB_ACCESS uses local H3C Comware 5/7, Huawei VRP, Cisco IOS/IOS XE and Ruijie RGOS templates. Enter passwords only in the secure local dialog, not AI/chat/memory. One high-risk approval covers the template, including possible account/privilege replacement and HTTP exposure. Sensitive output stays suppressed until reconnect; CLI completion is not proof of reachability or login. Arrays are wiped, but complete JVM String/system-copy erasure is not promised.

## Pick the problem, find the tool

| What you need | Open this |
|---|---|
| Not sure why a target is unreachable | Basic health check: single-target DNS, Ping and TCP with evidence and next steps |
| Investigate intermittent latency | Ping: per-reply RTT, sent/received counts, loss, min/average/max and a trend |
| Check a switch or service port | TCP: specified ports with refusal, timeout and connection outcomes |
| Understand a domain or website failure | DNS records, HTTP/HTTPS HEAD checks and TLS certificate inspection |
| Investigate path or packet-size issues | Root-free trace and IPv4 DF/MTU, with unknown/unsupported results stated clearly |
| Inspect the current network or authorized private LAN | Network information, address summary and bounded LAN discovery |
| Wake a WOL-capable device | Confirm the IPv4 destination and MAC, then send one magic packet |
| Calculate or look up network values | IPv4/IPv6 CIDR, MAC/OUI, port reference and identifier extraction |
| Explain switch output or unfamiliar commands | Guided checks, vendor command references, report preview and AI chat |

## Built for both phones and tablets

### Tablet · Light

![V0.12.0 tablet light network toolbox](Version/V0.12.0/docs/images/toolbox-tablet-light.png)

### Narrow phone layout · Dark

<img src="Version/V0.12.0/docs/images/toolbox-phone-dark.png" width="380" alt="V0.12.0 narrow phone dark network toolbox">

These are **V0.12.0 baseline toolkit captures** from an Android API 34 app, not V0.13.0 task-page acceptance screenshots or proof of compatibility with every physical device. Chinese/English, system/light/dark appearance and the existing four accent themes are supported.

- Search and categories; only the selected tool's parameters are shown.
- Numerical facts, progress and detail rows first; raw output remains available in a collapsed section.
- Stop active work; distinguish failure, partial completion, unsupported capability and cancellation.
- Redact and preview reports before copying, sharing or opening an editable AI draft. No automatic AI submission.

## AI that is easier to read—and remembers with permission

Headings, bold text, lists, quotes, inline code and code blocks use native formatting instead of displaying raw `**` markers. Actual command symbols and line endings remain intact. Copy a code block or the original answer, then review any command before loading it.

**Confirmed memory:** choose Remember → edit the content → select personal or named-device scope → confirm saving. View, edit, delete or disable memories independently of chat history. Deleting a conversation does not delete long-term memories, and ordinary replies are not automatically remembered.

- Android 6.0+ encrypted storage; 180-day expiry; at most five applicable records per request, with a receipt showing the selected entries.
- Device scope uses a profile name, not verified hardware identity. Legacy automatic memories are not imported implicitly.
- Multi-turn chat, stop/retry, conversation management and existing command risk gates remain.

There are **18 AI provider entry types**: OpenAI, Anthropic, Gemini, DeepSeek, Zhipu GLM, Qwen, Doubao, Tencent Hunyuan / TokenHub, Baidu Qianfan, Kimi, MiniMax, SiliconFlow, Groq, Mistral, xAI, OpenRouter, a custom compatible endpoint and HTTPS Ollama. Bring your own API key; supported integrations load upstream models with caching, filtering and manual fallback.

Custom endpoints target **OpenAI-compatible** APIs, not every provider's arbitrary protocol. Your chosen provider receives the context and enabled memories you decide to send; its fees and data policies apply. **Do not submit passwords, private keys or unreviewed sensitive configurations.** Local deletion cannot retract upstream data.

## Your existing field tools remain

| Capability | Field use |
|---|---|
| USB Console | OTG serial, baud rate, flow control, control lines, HEX and background receive |
| SSH / SFTP | Remote terminals, jump hosts, keepalive, file transfer and changed-host-key blocking |
| Controlled Telnet | Legacy compatibility; disabled by default with a plaintext warning for each connection |
| Command assistance | Real Tab, `?`, Ctrl+C and arrow keys; categorized H3C / Huawei / Cisco / Ruijie commands |
| Configuration and changes | Redacted snapshots, Diff, rollback drafts, target checks, maintenance windows and evidence |
| Operations center | Incident evidence plans, runbooks, security checks, topology-output parsing and vendor detection |
| Guided troubleshooting | Ten problem scenarios, existing terminal-output capture, explanations and resumable handoff |

Tab completion depends on the connected terminal. Vendor rules are not a claim of testing every model or firmware. The operations-center capture entry reads existing output; explicit AI tasks actively send commands under the rules above. Incomplete or paginated configuration is not a full recovery backup.

## Honest diagnostic boundaries

- **No Root or permission bypass.** Trace depends on system TTL support and router replies; unknown hops stay unknown. IPv4 DF/MTU timeout is not an exact upper bound; success at 1500 bytes proves only that size can pass.
- **Active discovery needs confirmation.** RFC1918 private `/26`–`/32` only, at most 64 addresses and four selected TCP ports, with a 40-second budget. Silence is not proof of offline status, and this is not a full inventory. A sent WOL packet does not prove a device woke up.
- **Safe protocol checks.** DNS uses system or explicitly selected servers, without hidden public-DNS fallback or DNSSEC validation. HTTP is HEAD-only, with no redirects, body downloads, credentials or cookies. TLS does not bypass trust/hostname failures and is not an exhaustive protocol/revocation scan.
- **Not bandwidth testing or a one-click cure.** Checks originate from the phone/tablet. Ping failure does not prove a device is offline; TCP success does not prove login works. Bundled OUI data is limited; unknown vendors remain unknown.

## Get started in three steps

1. **Install and check the version:** obtain a published APK and matching `SHA256SUMS.txt` from [GitHub Latest](https://github.com/chenwei666/NetSerial-AI/releases/latest). While V0.13.0 is being prepared, planned links are not proof of downloadable artifacts. Verify the original certificate before upgrading without uninstalling. An isolated Preview is not a production update.
2. **Choose a problem or tool:** use the guided home scenarios or search the toolbox directly. Only assess authorized targets; review discovery and WOL confirmations carefully.
3. **Choose chat or a task:** configure AI for ordinary questions, or explicitly open a task from the connected terminal, describe the goal and start. Review the target and impact before high-risk approval.

Minimum Android 5.0. Encrypted credentials, persisted chat/memory and current-default-network details require Android 6.0+. The added normal `ACCESS_NETWORK_STATE` permission reads current network/DNS details; no Root, location or VPN capture permission is requested.

## Verification and documentation

The local V0.13.0 production build passed **435 tests; Release Lint: 0 errors / 222 warnings**. V1/V2/V3 signature verification passed, the production application ID is unchanged, and the certificate matches the GitHub V0.12.0 formal APK. API 34 passed a formal V0.12.0 code 14 → V0.13.0 code 15 in-place upgrade, preserved first-install time and cold-start Status ok. See the [current test report](Version/V0.13.0/docs/TEST_REPORT.md).

The isolated preview built from final source was reinstalled and again passed **eight phone/tablet × Chinese/English × light/dark UI combinations, 42 assertions each**. Emulator reconnection allowed completion of the stricter Chinese task-scope build retest. This is preview UI evidence, not live hardware/provider end-to-end acceptance.

Local formal APK: **6,560,459 bytes**; SHA-256: `c1fadf90e9a2045eedb2aca715f6b2bd32a69e65f6433bd1807a80dc32ffb85d`. GitHub upload and re-download verification are still pending; a local hash does not establish a remote asset.

Live AI providers, physical/OEM devices, actual switches and real-user migration remain unaccepted. Validate automatic execution and local templates on authorized equipment; compilation, synthetic transcripts and UI checks do not certify all devices. Earlier results remain in [version history](VERSION_HISTORY_EN.md).

- [English guide](Version/V0.13.0/README_EN.md) · [中文使用说明](Version/V0.13.0/README.md)
- [Security boundaries](Version/V0.13.0/docs/SECURITY.md) · [Report a security issue](SECURITY.md)
- [Build and signing](Version/V0.13.0/docs/RELEASE.md) · [Architecture](Version/V0.13.0/docs/ARCHITECTURE.md)
- [Changelog](CHANGELOG.md) · [Version history](VERSION_HISTORY_EN.md) · [Contributing](CONTRIBUTING.md)

Build an isolated preview:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.13.0\scripts\build.ps1
```

Debug ID: `com.chenwei666.netserial.preview013`. It coexists with production without importing its credentials/history. Historical sources remain under `Version/V*`; V0.11.0 is a development record incorporated into V0.12.0, not a separate Release. Production signing material stays outside the repository.

## Help turn field experience into better tools

The best feedback is **“this device had this problem,” “this command fails on my firmware,”** or **“I could not understand this result.”** Open an [issue](https://github.com/chenwei666/NetSerial-AI/issues) with your device, Android version, switch/firmware and redacted steps. Never include passwords or API keys.

Commands, bilingual documentation, real-device results and small reproducible PRs are welcome. If the app helps in the field, consider a **Star** so you can find it again and other engineers can discover it.

## Origin and license

Built on Kai Morich's [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal), retaining the MIT license and upstream attribution. New features and maintenance: [chenwei666](https://github.com/chenwei666). See [LICENSE](LICENSE) and [UPSTREAM.md](Version/V0.13.0/UPSTREAM.md).
