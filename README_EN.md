# NetSerial AI Android Network Operations Terminal

[中文](README.md) | [English](README_EN.md)

Current buildable version: **V0.1.0**.

- [V0.1.0 source and version documentation](Version/V0.1.0/README_EN.md)
- [Full feature completion plan (Chinese)](FULL_AI_FEATURE_COMPLETION_PLAN.md)
- [Original APK analysis (Chinese)](ANALYSIS_AND_COMPLETION_PLAN.md)
- [V0.1.0 test report (Chinese)](Version/V0.1.0/docs/TEST_REPORT.md)

NetSerial AI is an Android USB serial terminal designed for network operations engineers working with switches and other CLI-driven network equipment. V0.1.0 establishes a trusted USB serial baseline, a native TAB key, offline command completion, deterministic command-risk checks, a pluggable multi-provider AI seam, and a safety layer that prevents AI output from bypassing local policy.

The project is based on the MIT-licensed [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal). It currently supports the existing USB serial features inherited from that baseline, an initial H3C completion pack, R0-R4 local risk classification, and provider definitions for OpenAI, Claude/Anthropic, Gemini, DeepSeek, Qwen, Kimi, OpenAI-compatible endpoints, and Ollama.

Live AI HTTP adapters, in-app credential management, structured memory, a full vendor command knowledge base, and real-device acceptance testing are planned for later versions. No real API key, token, account, or device password is included in the repository.

Developer: chenwei666.
