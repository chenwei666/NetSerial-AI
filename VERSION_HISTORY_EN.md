# NetSerial AI Version History

This document records the complete public evolution, release status, artifact trust boundary, and upgrade path. Every version is maintained by `chenwei666` and preserved as an immutable standalone source archive under [`Version/`](Version/).

## Overview

| Version | GitHub | Status | APK | Focus |
|---|---|---|---|---|
| V0.1.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.1.0) · [Source](Version/V0.1.0/) | Historical prerelease | Debug APK | First trusted USB serial and guarded-completion baseline |
| V0.2.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.2.0) · [Source](Version/V0.2.0/) | Historical source record | No verified APK | Multi-provider AI, encrypted credentials, local memory |
| V0.3.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.3.0) · [Source](Version/V0.3.0/) | Historical source record | No verified APK | SSH, Telnet, categorized commands, bilingual settings |
| V0.3.1 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.3.1) · [Source](Version/V0.3.1/) | First stable production release | Production-signed APK | Long-term signing and formal release gates |
| V0.4.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.4.0) · [Source](Version/V0.4.0/) | Release candidate | Production-signed APK | Change control, configuration Diff, network tools, enhanced SSH |
| V0.5.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.5.0) · [Source](Version/V0.5.0/) | Historical stable release | Production-signed APK | Material 3, themes, operations dashboard, new icon |
| V0.6.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.6.0) · [Source](Version/V0.6.0/) | Historical stable release | Production-signed APK | Vendor detection, Web wizard, update checks, AI diagnosis, operations center, sessions, snapshots, XMODEM |
| V0.7.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.7.0) · [Source](Version/V0.7.0/) | Historical stable release | Production-signed APK | Five-area navigation, AI failover, diagnostics, secure transfer, signed runbooks, guarded batch |
| V0.8.0 | [Source](Version/V0.8.0/) | Development candidate, not published | No formal APK | Modern task cards, 18 AI entries, upstream model catalogs, direct network toolbox |
| V0.9.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.9.0) · [Source](Version/V0.9.0/) | Historical stable release | Production-signed APK | Full AI chat, encrypted history, incident evidence, guided runbooks, drift and change closure |
| V0.9.1 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.9.1) · [Source](Version/V0.9.1/) | Historical stable release | Production-signed APK | One-tap active terminal read, redacted memory snapshot, and automatic vendor selection |
| V0.10.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0) · [Source](Version/V0.10.0/) | Historical stable release | Production-signed APK, downloaded and verified | Ten guided scenarios, phone-side probes, read-only switch guidance and report/AI drafts |
| V0.11.0 | [Source](Version/V0.11.0/) | Development checkpoint, no separate Release | Isolated Debug preview | Readable AI chat, fixed settings and confirmed memory; incorporated into V0.12.0 |
| V0.12.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.12.0) · [Source](Version/V0.12.0/) | Published stable production release | Production-signed APK, downloaded and verified | Sixteen root-free tools, adaptive phone/tablet layouts and confirmed AI memory |
| V0.13.0 | [Release / Latest](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.13.0) · [Source](Version/V0.13.0/) | Latest stable production release | Production-signed APK; in-place upgrade and remote download verified | Bound-session AI tasks, risk approval, local Web credentials and confirmed memory |

## V0.13.0 — Bound-session AI tasks (published production release)

- 2026-10-14: [PR #18](https://github.com/chenwei666/NetSerial-AI/pull/18) merged as `eff52f6d8960ebf5847f32cac0eedf34a3e3515e`; tag `v0.13.0` points to that exact commit. The formal Release was published at `2026-10-14T08:14:56Z`; Latest API returns v0.13.0 with draft/prerelease flags false. The [formal APK](https://github.com/chenwei666/NetSerial-AI/releases/download/v0.13.0/NetSerial-AI-0.13.0.apk) and checksum file were downloaded again and verified.
- Ordinary chat remains draft-only. Explicit tasks bind the current USB/SSH/Telnet token and use only the selected provider/model without failover. Strict COMMAND/ASK/FINISH/WEB_ACCESS JSON, device identification, state prechecks and task scope allow exact queries and limited VLAN/description automation.
- High/unknown risks require the exact typed target; protected devices require an authorized matching change task/window. Maximum 32 steps/10 minutes; stop on errors, paging/interaction, timeout, disconnection, cancellation or leaving. No automatic retry, resume, save or rollback.
- Memory controls share chat preferences and use at most five confirmed records without automatic writes. WEB_ACCESS uses a local password dialog and vendor templates with one high-risk template approval. Passwords do not enter model/chat/memory; output is suppressed until reconnect, not proof of Web reachability or login. Full JVM String/system-copy erasure is not promised.
- The final-source preview APK was reinstalled and again passed eight phone/tablet × Chinese/English × light/dark UI combinations, 42 assertions each. The stricter Chinese task-scope retest completed after emulator reconnection without a manual emulator restart. Preview UI evidence is not live provider/switch end-to-end acceptance.
- Production build: 435 tests passed; Release Lint 0 errors / 222 warnings. Formal V1/V2/V3 signatures passed and the certificate matches GitHub V0.12.0. API 34 formal V0.12.0 code 14 → V0.13.0 code 15 upgrade preserved firstInstallTime `2026-10-14 12:06:14`; cold-start Status ok. No real-user credentials/history were read.
- Formal APK `NetSerial-AI-0.13.0.apk`: 6,560,459 bytes; SHA-256 `c1fadf90e9a2045eedb2aca715f6b2bd32a69e65f6433bd1807a80dc32ffb85d`. The public APK and `SHA256SUMS.txt` were downloaded again; size/hash match the local artifact, and V1/V2/V3 signatures with the original production certificate passed again.
- Configuration: `0.13.0`, code `15`, Debug `.preview013`; production ID remains `com.chenwei666.netserial`. See the [current test report](Version/V0.13.0/docs/TEST_REPORT.md) for local evidence; this section records final remote publication verification.
- Live providers, physical Android/OEM devices, switches and real-user history/credential migration remain unaccepted. Local templates cover H3C Comware 5/7, Huawei VRP, Cisco IOS/IOS XE and Ruijie RGOS, not every firmware/model.

## V0.12.0 — Root-free toolkit and confirmed AI memory

- 2026-10-14: [PR #16](https://github.com/chenwei666/NetSerial-AI/pull/16) is merged; tag `v0.12.0` points to its exact merge commit `185f995e0ef3c667a1a7421f11cf33b671d5db96`. The production Release is published; the Latest API returns v0.12.0 with draft/prerelease flags false. The APK and checksum file were downloaded again and verified.
- Sixteen entries: health check, Ping, TCP, Trace, MTU, address summary, DNS, HTTP/HTTPS, TLS, local network information, LAN discovery, WOL, CIDR, MAC/OUI, port reference and identifier extraction. Search, categories and focused inputs adapt to phones and tablets.
- Ping displays actual RTT/statistics and a trend. Failures, unknowns, partial outcomes and unsupported capabilities remain distinct. Trace is root-free but system/router-dependent; IPv4 DF/MTU timeouts do not establish an upper bound, and a successful 1500-byte probe establishes only that lower bound.
- Bounded DNS records, credential-free HTTP HEAD and verified TLS; discovery limited to RFC1918 `/26`–`/32`, 64 addresses, four TCP ports and a 40-second budget. Discovery/WOL require confirmation; sending a packet does not prove a device woke up.
- Incorporates V0.11.0 readable AI chat, permanent settings and confirmed encrypted memory. Reports are previewed before sharing or opening an AI draft; no automatic AI requests or switch commands.
- 338 JVM tests; Debug/Release Lint 0 errors / 218 warnings; eight UI combinations with 138 assertions each and 64 AI regression assertions passed. V1/V2/V3 signing and ZIP alignment passed; the certificate matches a freshly downloaded V0.10.0 formal APK.
- A clean API 34 environment passed an actual V0.10.0 code 12 → V0.12.0 code 14 in-place install, preserved first-install timestamp and cold start. This is not proof of real-user credential/history migration.
- APK: `NetSerial-AI-0.12.0.apk`, 6,506,868 bytes; SHA-256: `fce9f7d5a4a60fe16d72ab357dabfbd769347767549b22075a6b8d6325f19cf8`.
- The downloaded APK size/hash match the local artifact and published `SHA256SUMS.txt`. An additional production-APK Ping UI retest did not finish because the emulator's `system_server` exited; it is not counted as passed.
- Production identity/certificate remain; isolated Debug ID ends in `.preview012`. Adds normal `ACCESS_NETWORK_STATE`; no Root, database or credential migration. Physical devices, live network equipment, Internet services and paid AI still need authorized field acceptance.

## V0.11.0 — AI chat and confirmed-memory development record

- Local development checkpoint with independent source and `.preview011` Debug preview; no separate GitHub Release or production-signed publication.
- Conversation-first layout, collapsible context, permanent settings and readable themed menus; native Markdown subset, independent code copying and intact command symbols.
- Explicitly confirmed encrypted memory with personal/named-device scopes, add/edit/delete management, use toggle and last-request receipt. Separate from history, 180-day expiry, at most five records per request; legacy automatic memories are not imported.
- Incorporated into V0.12.0; this development APK is not a production in-place update.

## V0.10.0 — Start with a problem, not a command

- 2026-10-13: published as GitHub Latest after PR [#14](https://github.com/chenwei666/NetSerial-AI/pull/14) merged, with draft and prerelease flags both false. The published APK and SHA256SUMS.txt were downloaded again; the 6,384,466-byte size and SHA-256 below match the local artifact.
- Problem-first home, ten searchable scenarios, collapsible shortcuts and purpose-based professional tools.
- Single-target DNS, three Ping samples and one TCP-port check with cancellation, timeouts, cautious explanations and next steps.
- Read-only switch command drafts, existing terminal output and resumable USB/remote handoff; preview reports before sharing or opening a fresh AI draft, with no automatic commands or AI requests.
- 65 test classes / 246 tests passed; Release Lint: 0 errors / 158 warnings; V1/V2/V3 signing, ZIP alignment and package metadata verified.
- An API 34 clean test environment passed an in-place v0.9.1 → v0.10.0 upgrade, cold start and new-home check. This does not prove real-user data migration or field compatibility.
- APK: `NetSerial-AI-0.10.0.apk`, 6,384,466 bytes; SHA-256: `1a081ef60a5208c881d737b389e2289addb36397f1f1f803f1f95c8d679afb16`.
- Production identity/certificate and minSdk 21/targetSdk 36 remain. The isolated Preview does not migrate production credentials. Real USB/SSH/Telnet, switches and AI accounts still require authorized field acceptance.

## V0.9.1 — One-tap current device reading

- Connected USB, SSH, and Telnet terminals expose recent output through a unified read-only session bridge, with current status in Operations Center.
- One tap reads the most recently active session, fills the analysis input, identifies H3C/Huawei/Cisco/Ruijie, and selects its vendor context.
- At most four sessions and 100,000 characters per session are kept in process memory after ANSI cleanup and sensitive-field redaction.
- The bridge never opens a hidden connection, silently sends an identification command, or bypasses terminal target and risk gates.
- 60 test classes, 186 tests, Release Lint, build, alignment, and signing gates passed. V0.9.1 was GitHub Latest at publication and is now a historical stable release, with formal APK SHA-256 `8890591333b84658539bff9ba80a2367a85ca50825578c065d4d4c1e6884e646`.

## V0.1.0 — Trusted baseline

- Established the independent application from the MIT-licensed SimpleUsbTerminal base.
- Preserved USB serial, HEX, flow control, control lines, and background receive.
- Added real `0x09` Tab, offline completion, the R0-R4 risk model, and a guarded AI extension boundary.
- The published Debug APK is a historical validation artifact, not part of the production upgrade chain.
- APK SHA-256: `f8129d2c7663ae5f2c762f689a4167ddc0b173996be4db57049cd7c6404d3763`.

## V0.2.0 — AI and memory foundation

- Added OpenAI, Anthropic, Gemini, DeepSeek, Qwen, Kimi, Ollama over HTTPS, and custom compatible endpoints.
- Protected API keys with Android Keystore and AES-GCM; keys never enter exports, logs, or memory.
- Added device profiles, structured local AI memory, redacted terminal context, and safe import/export.
- Expanded offline candidates for H3C, Huawei, Cisco, and Ruijie plus full terminal control keys.
- This was a Debug development checkpoint. No verifiable APK remains, so the Release is source and documentation only.

## V0.3.0 — Remote access and command library

- Added an interactive SSH terminal with first-use host-key confirmation and changed-key blocking.
- Added controlled Telnet, disabled by default with a plaintext warning before each connection.
- Added a ten-category command library for four vendors, search, draft insertion, and a shared Tab-completion source.
- Added Simplified Chinese and English plus charset, font-size, and timeout settings.
- This remained a Debug checkpoint. No verifiable APK remains, so the Release is source and documentation only.

## V0.3.1 — First stable production release

- Established the long-term RSA 4096 production certificate, local DPAPI passphrase protection, and repository-external key storage.
- Unified unit tests, Release Lint, Release build, and V1/V2/V3 signing into one release gate.
- Became the signing baseline for in-place upgrades to V0.4.0 and V0.5.0.
- APK SHA-256: `f4c410c3bf0016ecc5532bcdc27aa3ed109539d352983246a8bfd90c14972c76`.

## V0.4.0 — Field change workflow

- Added change tasks, maintenance windows, protected production targets, verification, rollback, and a timeline.
- Added configuration normalization, SHA-256, line Diff, rollback drafts, and redacted evidence exports.
- Added IPv4/IPv6, DNS, Ping, Traceroute, TCP, path MTU, MAC/OUI, and other field tools.
- Extended SSH with keyboard-interactive, session keys, jump hosts, keepalive, and SFTP.
- Signed with the V0.3.1 production certificate. Automated gates passed; the real-device matrix remains pending.
- APK SHA-256: `f4036401ee19d0a6ec8e87008a837f4bfbda23abe703d2d6346108efded13262`.

## V0.5.0 — Modern interface

- Migrated the application to Material 3 with system, light, and dark appearance modes.
- Added Ocean, Emerald, Violet, and Sunset accent themes.
- Added the operations dashboard, device and active-change summaries, and six quick actions.
- Added command favorites, recents, scope filters, and optional keep-awake behavior for connected sessions.
- Added the original terminal-and-switch app icon while preserving every V0.4.0 capability.
- Production-signed; 109 unit tests, Release Lint, V1/V2/V3 signing, and ZIP alignment passed. The real-device matrix remains pending.
- APK SHA-256: `c16a95034a45adbf3c38a9e24a0211aeb9d4197d52132420e47f4c00ab264cf3`.

## V0.6.0 — Intelligent operations expansion

- Added four-vendor identification and automatic command-page selection.
- Added an HTTPS-first Web access wizard, GitHub Latest checks, and one-tap AI diagnosis.
- Added read-only playbooks, LLDP/CDP discovery, compliance triage, canary batch planning, multi-session profiles, and configuration snapshots.
- Added Tab completion 2.0, custom command packs, and USB XMODEM-128 send.
- Passed 125 unit tests, Debug/Release Lint, Debug/Release builds, ZIP alignment, and V1/V2/V3 signature verification.
- APK SHA-256: `1468f7dab82c4caa3ed0729cecbdb66754b1d2bfc38f1627dc5e7778e87687ec`. This candidate is not yet GitHub Latest.

## V0.7.0 — Guarded operations workspace

- Introduced five-area navigation, global search, favorites, and recents.
- Added AI failover, advanced diagnostics, configuration backup, topology parsing, and bounded SNMPv3 planning.
- Added Keystore device aliases, temporary HTTP/TFTP, signed runbooks, and guarded batch foundations.
- Passed 147 tests, Release Lint, V1/V2/V3 signing, and ZIP alignment.
- APK SHA-256: 962e23209f57b24203a917474f90bda44c250c6071178e84a3d9a1b171504b81.

## V0.8.0 — AI catalogs and direct tools

- Expands AI coverage to 18 entries, including Zhipu and other Chinese/global providers.
- Adds upstream model sync, caching, searchable selection, and manual fallback.
- Unifies modern task cards and redesigns AI Settings and Network Toolbox.
- Adds bounded multi-port checks, address summaries, and copy/share actions.
- This is a source development candidate; a formal signed APK and field acceptance are pending.

## V0.9.0 — AI chat and field operations closure

- The single-turn command copilot becomes an in-app multi-turn assistant with encrypted history, selection, rename, cancellation, retry, copy, and redacted sharing.
- All 18 providers, upstream model catalogs, active-profile ordering, and failover remain shared rather than duplicated.
- One-tap incident evidence, guided read-only runbooks, drift severity, security review, and change-evidence completeness gates are added.
- Only allow-listed network CLI lines in explicit fenced blocks can be loaded after deterministic local risk classification; R4 remains blocked.
- versionCode 10 and versionName 0.9.0; published as GitHub Latest with APK SHA-256 `b4c4040cfc2f1895404dd0c9b6f8024f3824f4a1baf161621e132e504ee29697`. Authorized field acceptance remains pending.

## Installation and upgrade path

- V0.1.0, V0.2.0, and V0.3.0 are Debug or development checkpoints and cannot directly replace production-signed builds.
- Formal releases retain the production certificate established at V0.3.1. The V0.12.0 local formal APK matches a freshly downloaded V0.10.0 certificate and passed an isolated in-place upgrade; this does not prove real-user data migration.
- Download APKs only from the matching Release and verify SHA-256 before installation.
- V0.12.0 is verified as the current [GitHub Latest](https://github.com/chenwei666/NetSerial-AI/releases/latest) production release. V0.9.1 and V0.10.0 are historical releases; “Latest at publication” is not a current-version claim. V0.8.0 and V0.11.0 remain unpublished development checkpoints; Preview packages do not replace production. Authorized phone, USB-chipset, switch and owned-AI field acceptance is still required.
