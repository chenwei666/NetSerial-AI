# NetSerial AI 版本历史

本文记录公开仓库的完整版本演进、发布状态、安装包可信边界和升级关系。所有版本均由 `chenwei666` 维护，完整源码分别保存在 [`Version/`](Version/) 的独立目录中，历史版本不会被后续版本覆盖。

## 版本总览

| 版本 | GitHub | 发布状态 | APK | 主要定位 |
|---|---|---|---|---|
| V0.1.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.1.0) · [源码](Version/V0.1.0/) | 历史预发布 | Debug APK | 首个可信 USB 串口与安全补全基线 |
| V0.2.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.2.0) · [源码](Version/V0.2.0/) | 历史源码记录 | 无可验证 APK | 多 AI 厂商、加密凭据、本地记忆 |
| V0.3.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.3.0) · [源码](Version/V0.3.0/) | 历史源码记录 | 无可验证 APK | SSH、Telnet、分类命令库与多语言设置 |
| V0.3.1 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.3.1) · [源码](Version/V0.3.1/) | 稳定正式版 | 生产签名 APK | 长期生产签名与正式发布门禁 |
| V0.4.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.4.0) · [源码](Version/V0.4.0/) | 正式版候选 | 生产签名 APK | 变更管理、配置 Diff、网络工具与增强 SSH |
| V0.5.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.5.0) · [源码](Version/V0.5.0/) | 最新正式版候选 | 生产签名 APK | Material 3、主题系统、运维工作台与新图标 |

## V0.1.0 — 可信基线

- 从 MIT 许可的 SimpleUsbTerminal 建立独立应用基线。
- 保留 USB 串口、HEX、流控、控制线和后台接收。
- 增加真实 `0x09` Tab、离线补全、R0-R4 风险模型与 AI 安全扩展边界。
- 发布物是 Debug APK，只用于历史验证，不是生产升级链的一部分。
- APK SHA-256：`f8129d2c7663ae5f2c762f689a4167ddc0b173996be4db57049cd7c6404d3763`。

## V0.2.0 — AI 与记忆基础

- 接入 OpenAI、Anthropic、Gemini、DeepSeek、Qwen、Kimi、Ollama HTTPS 和自定义兼容接口。
- API Key 使用 Android Keystore + AES-GCM 保护，不进入导出、日志或记忆。
- 增加设备档案、本地结构化 AI 记忆、脱敏终端上下文和安全导入导出。
- 扩充 H3C、Huawei、Cisco、Ruijie 的离线候选及完整终端控制键。
- 该版本是 Debug 开发检查点；仓库没有可验证 APK，因此 Release 只提供源码和文档记录。

## V0.3.0 — 远程连接与命令库

- 增加 SSH 交互式终端、主机密钥首次确认和密钥变化阻断。
- 增加默认关闭、每次连接提示风险的受控 Telnet。
- 增加四厂商十分类命令库、搜索、填入草稿和统一 Tab 补全数据源。
- 增加简体中文、English、字符集、字号、连接超时等应用设置。
- 该版本仍为 Debug 检查点；仓库没有可验证 APK，因此 Release 只提供源码和文档记录。

## V0.3.1 — 首个稳定正式版

- 建立长期 RSA 4096 生产证书、DPAPI 本机口令保护和仓库外密钥管理。
- 发布门禁统一执行单元测试、Release Lint、Release 构建及 V1/V2/V3 签名。
- 成为后续 V0.4.0、V0.5.0 可覆盖升级的签名基线。
- APK SHA-256：`f4c410c3bf0016ecc5532bcdc27aa3ed109539d352983246a8bfd90c14972c76`。

## V0.4.0 — 现场变更工作流

- 增加变更任务、维护窗口、生产设备保护、验证、回滚和事件时间线。
- 增加配置规范化、SHA-256、逐行 Diff、回滚草稿与脱敏证据导出。
- 增加 IPv4/IPv6、DNS、Ping、Traceroute、TCP、路径 MTU、MAC/OUI 等网络工具。
- SSH 增加 keyboard-interactive、会话私钥、跳板机、Keepalive 和 SFTP。
- 使用与 V0.3.1 相同的生产证书签名；自动化门禁通过，真实设备矩阵仍待验收。
- APK SHA-256：`f4036401ee19d0a6ec8e87008a837f4bfbda23abe703d2d6346108efded13262`。

## V0.5.0 — 现代化界面

- 全应用迁移至 Material 3，支持跟随系统、浅色和深色模式。
- 增加海洋蓝、翡翠绿、科技紫、日落橙四套主题。
- 增加首页运维工作台、设备与活动变更摘要和六个快捷入口。
- 增加命令收藏、最近使用、范围筛选和连接会话按需常亮。
- 增加原创终端与交换机应用图标，并保留 V0.4.0 全部功能。
- 使用生产证书签名；109 项单元测试、Release Lint、V1/V2/V3 签名和 ZIP 对齐通过，真实设备矩阵仍待验收。
- APK SHA-256：`c16a95034a45adbf3c38a9e24a0211aeb9d4197d52132420e47f4c00ab264cf3`。

## 安装与升级关系

- V0.1.0、V0.2.0、V0.3.0 属于 Debug/开发检查点，不能直接覆盖生产签名版本。
- 从 V0.3.1 开始，V0.3.1、V0.4.0、V0.5.0 使用同一生产证书，可按版本顺序覆盖升级并保留应用私有数据。
- 安装前必须从对应 Release 下载附件并核对 SHA-256；不要安装来源不明或哈希不一致的 APK。
- V0.4.0、V0.5.0 当前是候选版，投入生产前仍需在授权手机、USB 串口芯片和目标交换机上完成验收。
