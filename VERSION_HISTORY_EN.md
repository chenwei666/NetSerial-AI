# NetSerial AI Version History

This document records the complete public evolution, release status, artifact trust boundary, and upgrade path. Every version is maintained by `chenwei666` and preserved as an immutable standalone source archive under [`Version/`](Version/).

## Overview

| Version | GitHub | Status | APK | Focus |
|---|---|---|---|---|
| V0.1.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.1.0) · [Source](Version/V0.1.0/) | Historical prerelease | Debug APK | First trusted USB serial and guarded-completion baseline |
| V0.2.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.2.0) · [Source](Version/V0.2.0/) | Historical source record | No verified APK | Multi-provider AI, encrypted credentials, local memory |
| V0.3.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.3.0) · [Source](Version/V0.3.0/) | Historical source record | No verified APK | SSH, Telnet, categorized commands, bilingual settings |
| V0.3.1 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.3.1) · [Source](Version/V0.3.1/) | Stable production release | Production-signed APK | Long-term signing and formal release gates |
| V0.4.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.4.0) · [Source](Version/V0.4.0/) | Release candidate | Production-signed APK | Change control, configuration Diff, network tools, enhanced SSH |
| V0.5.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.5.0) · [Source](Version/V0.5.0/) | Latest candidate | Production-signed APK | Material 3, themes, operations dashboard, new icon |

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

## Installation and upgrade path

- V0.1.0, V0.2.0, and V0.3.0 are Debug or development checkpoints and cannot directly replace production-signed builds.
- V0.3.1, V0.4.0, and V0.5.0 share the same production certificate and support sequential in-place upgrades with application-private data retained.
- Download APKs only from the matching Release and verify SHA-256 before installation.
- V0.4.0 and V0.5.0 remain candidates until accepted on authorized phones, USB serial chipsets, and target switches.
