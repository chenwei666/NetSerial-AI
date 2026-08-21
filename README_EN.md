# NetSerial AI Android Network Operations Terminal

[中文](README.md) | [English](README_EN.md)

Current development version: **V0.2.0**. Stable baseline: **V0.1.0**.

- [V0.2.0 source and version documentation](Version/V0.2.0/README_EN.md)
- [V0.2.0 test report (Chinese)](Version/V0.2.0/docs/TEST_REPORT.md)
- [V0.1.0 stable baseline](Version/V0.1.0/README_EN.md)
- [Full feature completion plan (Chinese)](FULL_AI_FEATURE_COMPLETION_PLAN.md)
- [Original APK analysis (Chinese)](ANALYSIS_AND_COMPLETION_PLAN.md)

NetSerial AI is an Android USB serial terminal designed for network operations engineers working with switches and other CLI-driven network equipment. V0.1.0 establishes a trusted USB serial baseline, a native TAB key, offline command completion, deterministic command-risk checks, a pluggable multi-provider AI seam, and a safety layer that prevents AI output from bypassing local policy.

The project is based on the MIT-licensed [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal). It currently supports the existing USB serial features inherited from that baseline, an initial H3C completion pack, R0-R4 local risk classification, and provider definitions for OpenAI, Claude/Anthropic, Gemini, DeepSeek, Qwen, Kimi, OpenAI-compatible endpoints, and Ollama.

V0.2.0 adds in-app multi-provider profile management, an Android Keystore-backed credential vault, a common OpenAI-compatible Chat Completions adapter, and an explicit minimal connection test. The AI conversation panel, structured memory, full vendor command packs, and real-device acceptance remain in development. No real API key, token, account, or device password is included in the repository.

Developer: chenwei666.
