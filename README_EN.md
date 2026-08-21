# NetSerial AI Android Network Operations Terminal

[中文](README.md) | [English](README_EN.md)

Current development version: **V0.2.0**. Stable baseline: **V0.1.0**.

NetSerial AI is an Android USB Console terminal for network engineers who maintain H3C, Huawei, Cisco, Ruijie, and other CLI-driven switches. It combines serial access, a real TAB key, offline command suggestions, multi-provider AI, deterministic safety checks, device profiles, and controlled local memory.

V0.2.0 includes:

- OpenAI, Claude/Anthropic, Gemini, DeepSeek, Qwen, Kimi, OpenAI-compatible, and Ollama-over-HTTPS adapters;
- Android Keystore-encrypted API keys, multiple profiles, active-profile switching, and explicit minimal connection tests;
- an in-terminal AI Copilot for natural-language generation and command checking/completion, with optional redacted terminal context;
- local R0-R4 reclassification of every AI command: R4 is blocked, while other commands are only loaded into the editor and never auto-sent;
- offline H3C, Huawei, Cisco, and Ruijie suggestions plus a real ASCII `0x09` TAB;
- ESC, Ctrl+C, Ctrl+Z, arrow, backspace, delete, `?`, and `|` keys;
- device name, vendor, CLI mode, and baud-rate profiles;
- provider-neutral structured memory with scope, expiry, deletion, safe import/export, and credential rejection;
- bounded terminal buffering, ANSI cleanup, and sensitive-text redaction.
- redacted session export and credential-free device/memory backups.

Project resources:

- [V0.2.0 source and usage](Version/V0.2.0/README_EN.md)
- [V0.2.0 test report (Chinese)](Version/V0.2.0/docs/TEST_REPORT.md)
- [Full feature plan (Chinese)](FULL_AI_FEATURE_COMPLETION_PLAN.md)
- [Original APK analysis (Chinese)](ANALYSIS_AND_COMPLETION_PLAN.md)

The repository contains no real API key, token, account, or device password. The Debug APK passes automated tests, Android Lint, and compilation, but physical acceptance on Android devices, USB serial adapters, and real switches is still required.

Developer: chenwei666.
