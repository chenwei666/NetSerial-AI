# NetSerial AI 版本历史

本文记录公开仓库的完整版本演进、发布状态、安装包可信边界和升级关系。所有版本均由 `chenwei666` 维护，完整源码分别保存在 [`Version/`](Version/) 的独立目录中，历史版本不会被后续版本覆盖。

## 版本总览

| 版本 | GitHub | 发布状态 | APK | 主要定位 |
|---|---|---|---|---|
| V0.1.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.1.0) · [源码](Version/V0.1.0/) | 历史预发布 | Debug APK | 首个可信 USB 串口与安全补全基线 |
| V0.2.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.2.0) · [源码](Version/V0.2.0/) | 历史源码记录 | 无可验证 APK | 多 AI 厂商、加密凭据、本地记忆 |
| V0.3.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.3.0) · [源码](Version/V0.3.0/) | 历史源码记录 | 无可验证 APK | SSH、Telnet、分类命令库与多语言设置 |
| V0.3.1 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.3.1) · [源码](Version/V0.3.1/) | 首个稳定正式版 | 生产签名 APK | 长期生产签名与正式发布门禁 |
| V0.4.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.4.0) · [源码](Version/V0.4.0/) | 正式版候选 | 生产签名 APK | 变更管理、配置 Diff、网络工具与增强 SSH |
| V0.5.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.5.0) · [源码](Version/V0.5.0/) | 历史稳定版 | 生产签名 APK | Material 3、主题系统、运维工作台与新图标 |
| V0.6.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.6.0) · [源码](Version/V0.6.0/) | 历史稳定版 | 生产签名 APK | 厂商识别、Web 向导、更新检测、AI 诊断、运维中心、多会话、快照与 XMODEM |
| V0.7.0 | [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.7.0) · [源码](Version/V0.7.0/) | 历史稳定版 | 生产签名 APK | 五区导航、AI 故障转移、高级诊断、安全传输、签名剧本与受控批次 |
| V0.8.0 | [源码](Version/V0.8.0/) | 开发候选，尚未发布 | 尚无正式 APK | 现代任务卡片、18 类 AI、上游模型目录与直达网络工具 |
| V0.9.0 | [Latest](https://github.com/chenwei666/NetSerial-AI/releases/latest) · [源码](Version/V0.9.0/) | 最新稳定正式版 | 生产签名 APK | 完整 AI 对话、加密历史、故障取证、运行手册、漂移与变更闭环 |
| V0.9.1 | [PR #11](https://github.com/chenwei666/NetSerial-AI/pull/11) · [源码](Version/V0.9.1/) | 已合并开发候选，尚未发布 | 本地生产签名候选 APK | 当前终端一键读取、内存脱敏快照与自动厂商选择 |

## V0.9.1 — 当前设备一键读取

- USB、SSH、Telnet 连接后向统一的只读会话桥接层提供最近输出，运维中心显示当前连接状态。
- 点击一次即可读取最近活动会话、填入分析区、识别 H3C/Huawei/Cisco/Ruijie 并切换厂商上下文。
- 快照最多保留 4 个会话、每个 100,000 字符，仅存于应用进程内并执行 ANSI 清理和敏感字段脱敏。
- 不打开隐藏连接、不自动发送识别命令、不绕过终端目标与风险门禁。
- 60 个测试类、186 项测试及 Release Lint/构建/签名门禁通过；PR #11 已合并，GitHub Latest 仍为 V0.9.0。

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

## V0.6.0 — 智能运维增强

- 增加四厂商自动识别与自动选择命令页面。
- 增加 HTTPS 优先的 Web 开通向导、GitHub Latest 更新检测与 AI 一键故障诊断。
- 增加只读剧本、LLDP/CDP 发现、合规初筛、金丝雀批次计划、多会话和配置快照。
- 增加 Tab 补全 2.0、自定义命令包和 USB XMODEM-128 发送。
- 125 项单元测试、Debug/Release Lint、Debug/Release 构建、ZIP 对齐和 V1/V2/V3 签名通过。
- APK SHA-256：`1468f7dab82c4caa3ed0729cecbdb66754b1d2bfc38f1627dc5e7778e87687ec`。该候选尚未发布为 GitHub Latest。

## V0.7.0 — 安全工作台

- 重整为主页、连接、终端、工具箱、设置五区导航，增加全局搜索、收藏和最近使用。
- 增加 AI 多厂商故障转移、高级诊断、配置备份、拓扑解析和 SNMPv3 只读计划。
- 增加 Keystore 设备凭据别名、临时 HTTP/TFTP、安全签名运行手册和受控批次基础。
- 147 项测试、Release Lint、V1/V2/V3 签名与 ZIP 对齐通过。
- APK SHA-256：962e23209f57b24203a917474f90bda44c250c6071178e84a3d9a1b171504b81。

## V0.8.0 — AI 目录与直达工具

- AI 扩展到 18 类入口，加入智谱等国内外厂商。
- 增加上游模型动态同步、缓存、搜索选择和手工回退。
- 统一现代任务卡片，重做 AI 设置与网络工具信息架构。
- 增加受控多端口检查、地址摘要与结果复制/分享。
- 当前是源码开发候选；正式签名 APK 与现场验收尚未完成。

## V0.9.0 — AI 对话与现场运维闭环

- AI 从单轮命令提案升级为应用内多轮对话，支持加密历史、切换、重命名、停止、重试、复制和脱敏分享。
- 继续使用 18 类厂商、上游模型目录、活动档案优先和故障转移，不新增重复的凭据或网络实现。
- 新增一键故障取证、结构化只读运行手册、配置漂移分级、安全巡检和变更证据完整性门禁。
- AI 回复只允许从显式围栏提取网络 CLI 白名单命令，仍须本地分级和人工装载；R4 保持阻断。
- versionCode 10、versionName 0.9.0；已发布为 GitHub Latest，APK SHA-256 为 `b4c4040cfc2f1895404dd0c9b6f8024f3824f4a1baf161621e132e504ee29697`，现场验收仍需在授权环境完成。

## 安装与升级关系

- V0.1.0、V0.2.0、V0.3.0 属于 Debug/开发检查点，不能直接覆盖生产签名版本。
- 从 V0.3.1 开始，所有正式版本使用同一生产证书；V0.9.0 已沿用该证书，可覆盖升级并保留应用私有数据。
- 安装前必须从对应 Release 下载附件并核对 SHA-256；不要安装来源不明或哈希不一致的 APK。
- V0.9.0 是当前 GitHub `Latest` 正式版；V0.8.0 保留为未单独发布的源码检查点。部署到生产网络前仍须在授权手机、USB 串口芯片、目标交换机和自有 AI 账号上完成现场验收。
