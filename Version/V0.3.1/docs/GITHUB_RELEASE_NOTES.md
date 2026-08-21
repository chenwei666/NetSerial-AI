# NetSerial AI V0.3.1 正式版 / Production Release

这是 NetSerial AI 首个生产签名正式版本，面向交换机网络运维场景。

主要功能：

- USB Console、SSH 和显式启用的 Telnet 交互终端。
- H3C Comware、华为 VRP、Cisco IOS、锐捷 RGOS 四厂商十分类常用命令库。
- 真实 TAB/ESC/Ctrl+C/方向键终端控制与离线补全。
- OpenAI、Claude/Anthropic、Gemini、DeepSeek、通义千问、Kimi、Ollama HTTPS 和自定义 OpenAI-compatible API。
- 设备记忆、本地 R0–R4 命令风险检查、中文/English 切换和完整应用设置。

正式发布验证：69 项单元测试全部通过；Release Lint 0 Error；APK 通过 ZIP 对齐和 Android V1/V2/V3 签名验证。

SHA-256：`f4c410c3bf0016ecc5532bcdc27aa3ed109539d352983246a8bfd90c14972c76`

首次从 V0.3.0 Debug 版迁移时，由于签名不同，需要先卸载 Debug 版再安装此正式版。后续正式版本可使用同一生产证书覆盖升级。

This is the first production-signed NetSerial AI release for switch operations. It includes USB Console, SSH, opt-in Telnet, four-vendor categorized commands, real terminal control keys, offline completion, multi-provider AI, device memory, local command-risk checks, bilingual UI, and complete settings. All 69 unit tests passed; Release Lint has zero errors; ZIP alignment and Android V1/V2/V3 signatures were verified.

Real Android devices, USB serial adapters, switches, and user-owned AI accounts remain external acceptance items.
