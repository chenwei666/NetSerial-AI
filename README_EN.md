# NetSerial AI Android Switch Operations Terminal

[中文](README.md) | [English](README_EN.md)

Current development version: **V0.3.0**. Stable baseline: **V0.1.0**.

NetSerial AI is an Android terminal for network engineers who maintain H3C, Huawei, Cisco, and Ruijie switches. It combines USB Console, SSH, explicitly enabled Telnet, real TAB input, a categorized command library, multiple AI-provider APIs, device memory, and local command-risk checks.

## New in V0.3.0

- Interactive SSH terminal. Passwords remain in memory only; first connections require manual host-key fingerprint verification; changed keys are blocked.
- Interactive Telnet terminal. Telnet is disabled by default and every connection requires a plaintext-risk confirmation. Use it only on an isolated management network.
- Ten command categories for four vendors: device information, interfaces, VLAN, Layer 3, routing, spanning tree, link aggregation, security, troubleshooting, and save/backup.
- USB and remote terminals share command drafts, AI drafts, real TAB/ESC/Ctrl+C/arrow shortcuts, and deterministic R0–R4 risk rules.
- High-risk commands require confirmation. Restart, erase, and upgrade commands require typing `EXECUTE`.
- App settings cover system/Chinese/English language, Telnet enablement, timeout, terminal text size, UTF-8/GBK/ISO-8859-1 encoding, and SSH known-host management.
- Existing OpenAI, Claude/Anthropic, Gemini, DeepSeek, Qwen, Kimi, Ollama-over-HTTPS, and custom OpenAI-compatible APIs remain supported.

## Documentation and source

- [V0.3.0 Chinese guide](Version/V0.3.0/README.md)
- [V0.3.0 English guide](Version/V0.3.0/README_EN.md)
- [Remote connections and security](Version/V0.3.0/docs/REMOTE_CONNECTIONS.md)
- [Architecture](Version/V0.3.0/docs/ARCHITECTURE.md)
- [Test report](Version/V0.3.0/docs/TEST_REPORT.md)
- [Full feature plan](FULL_AI_FEATURE_COMPLETION_PLAN.md)
- [Original APK analysis](ANALYSIS_AND_COMPLETION_PLAN.md)

The repository contains no real API keys, tokens, switch accounts, passwords, or site configuration. Automated tests, Android Lint, and a Debug build do not replace acceptance testing on a physical Android device and real switches.

Developer: chenwei666.
