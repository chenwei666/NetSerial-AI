# NetSerial AI 安卓交换机运维终端

[中文](README.md) | [English](README_EN.md)

当前开发版本：**V0.2.0**；稳定基线：**V0.1.0**。

NetSerial AI 面向需要通过 Console 串口维护 H3C、华为、Cisco、锐捷等交换机的网络运维工程师。它把 USB 串口终端、真实 TAB、离线命令候选、多 AI 厂商接入、本地命令风险检查、设备档案和可控记忆放在一个 Android App 中。

V0.2.0 已实现：

- OpenAI、Claude/Anthropic、Gemini、DeepSeek、通义千问、Kimi、OpenAI-compatible 和 Ollama HTTPS 代理接入；
- Android Keystore 加密 API Key，多配置切换和最小连接测试；
- 终端内 AI 助手：自然语言生成、手写命令检查/补全、脱敏上下文、结构化命令计划；
- 所有 AI 命令重新经过本地 R0-R4 风险判定，R4 禁止载入，其他命令也只载入输入框、不自动发送；
- H3C、华为、Cisco、锐捷离线命令候选，以及真实 `TAB` 字节 `0x09`；
- ESC、Ctrl+C、Ctrl+Z、方向键、退格、删除、`?` 和 `|` 快捷键；
- 设备名称、厂商、CLI 模式、波特率档案；
- 供应商无关的本地结构化记忆，支持作用域、过期、删除、安全导入导出，拒绝保存密码、Token 和密钥；
- 受控终端缓冲、ANSI 清洗和敏感信息脱敏。
- 脱敏会话日志导出，以及不含凭据的设备档案/记忆备份。

源码与详细说明：

- [V0.2.0 完整源码和使用说明](Version/V0.2.0/README.md)
- [V0.2.0 测试报告](Version/V0.2.0/docs/TEST_REPORT.md)
- [完整功能方案](FULL_AI_FEATURE_COMPLETION_PLAN.md)
- [原 APK 分析](ANALYSIS_AND_COMPLETION_PLAN.md)

仓库不包含真实 API Key、Token、设备密码或账号信息。当前 Debug APK 已通过自动化测试、Android Lint 和构建，但仍需在真实 Android 手机、USB 转串口线和不同品牌交换机上完成硬件验收。

开发负责人：chenwei666。
