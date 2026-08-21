# NetSerial AI 安卓交换机运维终端

[中文](README.md) | [English](README_EN.md)

当前开发版本：**V0.3.0**；稳定基线：**V0.1.0**。

NetSerial AI 面向经常维护 H3C、华为、Cisco、锐捷交换机的网络运维工程师，把 USB Console、SSH、显式启用的 Telnet、真实 TAB、分类命令库、多 AI 厂商 API、设备记忆和本地命令风险检查整合到一个 Android App。

## V0.3.0 新增

- SSH 交互式终端：密码只在连接期间存在于内存；首次连接必须人工核对主机密钥指纹；密钥变更时阻止连接。
- Telnet 交互式终端：默认关闭；启用后每次连接仍显示明文风险确认；建议仅用于隔离的管理网络。
- 四厂商、十分类常用命令库：设备信息、接口、VLAN、三层接口、路由、生成树、链路聚合、安全、故障排查、保存与备份。
- USB 和远程终端共用命令草稿、AI 草稿、真实 TAB/ESC/Ctrl+C/方向键快捷键和 R0–R4 本地风险规则。
- 高风险命令发送前再次确认；重启、擦除、升级等关键命令要求输入 `EXECUTE`。
- 应用设置：跟随系统/简体中文/English、Telnet 开关、连接超时、终端字号、UTF-8/GBK/ISO-8859-1 编码、SSH 已知主机密钥管理。
- 继续支持 OpenAI、Claude/Anthropic、Gemini、DeepSeek、通义千问、Kimi、Ollama HTTPS 和自定义 OpenAI-compatible API。

## 文档和源码

- [V0.3.0 中文使用说明](Version/V0.3.0/README.md)
- [V0.3.0 English guide](Version/V0.3.0/README_EN.md)
- [远程连接与安全说明](Version/V0.3.0/docs/REMOTE_CONNECTIONS.md)
- [架构说明](Version/V0.3.0/docs/ARCHITECTURE.md)
- [测试报告](Version/V0.3.0/docs/TEST_REPORT.md)
- [完整功能方案](FULL_AI_FEATURE_COMPLETION_PLAN.md)
- [原 APK 分析](ANALYSIS_AND_COMPLETION_PLAN.md)

仓库不包含真实 API Key、Token、交换机账号、密码或现场配置。自动化测试、Android Lint 和 Debug 构建不能替代真实 Android 手机与真实交换机的硬件验收。

开发负责人：chenwei666。
