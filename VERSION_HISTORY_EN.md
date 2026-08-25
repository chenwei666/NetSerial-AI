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
| V0.9.1 | [Latest](https://github.com/chenwei666/NetSerial-AI/releases/latest) · [Source](Version/V0.9.1/) | Latest stable release | Production-signed APK | One-tap active terminal read, redacted memory snapshot, and automatic vendor selection |

## V0.9.1 — One-tap current device reading

- Connected USB, SSH, and Telnet terminals expose recent output through a unified read-only session bridge, with current status in Operations Center.
- One tap reads the most recently active session, fills the analysis input, identifies H3C/Huawei/Cisco/Ruijie, and selects its vendor context.
- At most four sessions and 100,000 characters per session are kept in process memory after ANSI cleanup and sensitive-field redaction.
- The bridge never opens a hidden connection, silently sends an identification command, or bypasses terminal target and risk gates.
- 60 test classes, 186 tests, Release Lint, build, alignment, and signing gates passed. V0.9.1 is GitHub Latest with formal APK SHA-256 `8890591333b84658539bff9ba80a2367a85ca50825578c065d4d4c1e6884e646`.

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
- Every formal release from V0.3.1 uses the same production certificate. V0.9.1 preserves that certificate for in-place upgrades and application-private data.
- Download APKs only from the matching Release and verify SHA-256 before installation.
- V0.9.1 is the current GitHub `Latest` production release. V0.8.0 remains an unpublished source checkpoint; field acceptance on authorized phones, USB serial chipsets, target switches, and owned AI accounts remains required before production deployment.
