# NetSerial AI

<p align="center">
  <img src="Version/V0.10.0/design/app-icon-512.png" width="112" alt="NetSerial AI 应用图标">
</p>

<p align="center">
  <strong>网络出问题，先选症状，再查原因。</strong><br>
  给新手一条排障路径，给工程师一套随身工具。<br>
  Android · USB Console · SSH/SFTP · 网络诊断 · 多厂商命令 · AI 助手
</p>

<p align="center">
  <a href="https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0"><strong>下载 V0.10.0</strong></a>
  · <a href="#三步开始">快速上手</a>
  · <a href="README_EN.md">English</a>
  · <a href="https://github.com/chenwei666/NetSerial-AI/issues">反馈问题 / 提建议</a>
</p>

<p align="center">
  <a href="https://github.com/chenwei666/NetSerial-AI/releases/latest"><img src="https://img.shields.io/github/v/release/chenwei666/NetSerial-AI?label=release" alt="GitHub Release"></a>
  <img src="https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white" alt="最低 Android 5.0">
  <img src="https://img.shields.io/badge/languages-Chinese%20%2F%20English-006C93" alt="中文和英文">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="MIT License"></a>
</p>

不用先记住一串命令，也不用在一堆菜单里找工具。**NetSerial AI 是一个开源 Android 网络运维工具箱**：从“上不了网”“交换机登不上”“端口不通”出发，逐步收集信息、解释结果，再告诉你下一步查什么。

熟悉命令行？USB 串口、SSH、SFTP、真实 Tab 键、多厂商命令库和配置对比仍然都在。需要帮手？把排障报告核对后交给自己配置的 AI，继续讨论原因和命令草稿。

> **AI 不替你操作交换机。** AI 回复和新手向导命令都是待审核草稿，不会自动下发；真正发送仍需你核对目标、命令和风险。

**当前正式版：[V0.10.0 · GitHub Latest](https://github.com/chenwei666/NetSerial-AI/releases/latest)**。生产签名 APK 已发布，线上安装包重新下载校验通过；升级时直接覆盖安装，不要先卸载旧正式版。

## 遇到这些问题，可以从这里开始

| 你遇到的问题 | 工具箱帮你做什么 |
|---|---|
| 上不了网 | 对填写的单个目标做 DNS、Ping、TCP 检查，给出解释与下一步 |
| 网络慢、偶尔掉线 | 观察短时连通性与延迟，区分已知现象和仍需排查的原因 |
| 域名打不开 | 检查域名解析，结合连通结果缩小排查范围 |
| 交换机登不上 | 检查指定管理端口，提示继续核对协议、地址与认证 |
| 交换机端口不通 | 提供厂商对应的只读检查命令，解释粘贴或读取的端口回显 |
| VLAN / 网关有问题 | 引导核对 VLAN、接口和三层信息，查看证据与检查建议 |
| 不知道设备是什么品牌 | 读取当前终端已有输出，识别 H3C、华为、Cisco、锐捷特征 |
| 想给交换机做体检 | 查看只读检查步骤，分析回显中的健康线索 |
| 想保存配置记录 | 收集回显、检查内容，再到快照中心显式保存与对比 |
| 看不懂输出、记不住命令 | 预览排障报告并带入新的 AI 对话草稿，确认后再发送 |

**检测有边界：** 网络检查从手机发起，不是电脑网卡诊断，也不是带宽测速；Ping 不通不等于设备离线，TCP 连通不等于账号能登录。读取设备只读取当前会话已有回显，不会偷偷发送新命令。截断或缺页的配置不能当作完整恢复备份。

## 看看实际界面

### 中文 · 浅色

![V0.10.0 中文浅色新手工具箱首页](Version/V0.10.0/docs/images/home-chinese-light.png)

### English · Dark

![V0.10.0 English dark guided toolbox home](Version/V0.10.0/docs/images/home-english-dark.png)

以上是 **Android API 34、1600×900 平板测试环境中预览包的真实截图**，不是效果图，也不代表物理手机或真实交换机已完成兼容验收。应用支持中英文、跟随系统 / 浅色 / 深色以及四套主题。

## V0.10.0：从“工具列表”到“按问题排障”

- **十个问题入口 + 症状搜索**：先选你遇到的问题，不要求先知道该用哪个工具。
- **结果旁边就是解释**：明确检查了什么、没有证明什么，以及下一步该看哪里。
- **回到终端，再继续排障**：在连接页和向导之间保留本次输入；草稿仅保留在当前进程，不承诺退出应用后恢复。
- **报告先预览，AI 后发送**：可以分享排障记录，或将报告带入新的 AI 草稿；不会自动提交给 AI。
- **专业工具保留、入口分组**：仍可直达终端、命令库、网络诊断和运维中心，收藏与最近使用默认折叠。

完整记录见 [CHANGELOG](CHANGELOG.md) · [版本历史](VERSION_HISTORY.md) · [V0.10.0 使用说明](Version/V0.10.0/README.md)。

## 工程师常用的能力，也都保留

| 能力 | 适合的现场工作 |
|---|---|
| USB Console | OTG 串口、波特率、流控、控制线、HEX 与后台接收 |
| SSH / SFTP | 远程终端、跳板机、Keepalive、文件传输与主机密钥变更阻断 |
| 受控 Telnet | 兼容旧设备；默认关闭，每次连接提示明文风险 |
| 命令辅助 | 真实 Tab、问号、Ctrl+C、方向键；H3C / Huawei / Cisco / Ruijie 分类命令 |
| AI 对话与记忆 | 多轮会话、停止 / 重试、设备记忆、命令解释与审核 |
| 网络工具 | DNS、Ping、Traceroute、受控 TCP 检查、MTU、IPv4/IPv6 CIDR、MAC/OUI |
| 配置与变更 | 脱敏快照、配置 Diff、回滚草稿、目标校验、风险门禁与变更证据 |
| 运维中心 | 故障取证计划、运行手册、安全巡检、拓扑回显解析与设备识别 |

命令与识别覆盖相应厂商的常见平台和规则，**不表示所有型号、系统版本都已实测**。真实 Tab 补全结果取决于设备终端本身。

## AI：你选服务商，你控制发送

内置 **18 类接入入口**：OpenAI、Anthropic、Gemini、DeepSeek、智谱 GLM、通义千问、豆包、腾讯混元 / TokenHub、百度千帆、Kimi、MiniMax、硅基流动、Groq、Mistral、xAI、OpenRouter、自定义兼容接口和 HTTPS Ollama。

- 使用自己的 API Key；可按接入能力从上游同步模型，支持缓存、筛选和手动填写模型名。
- 自定义入口面向 **OpenAI-compatible** 接口，不等于任意厂商的任意协议都能直接接入；模型清单与可用性取决于服务商及账号权限。
- 本地诊断、离线命令和终端操作不需要 AI 账号。使用在线 AI 时，确认发送的内容会交给所选服务商，费用和数据处理规则以该服务商为准。
- API Key、持久化 AI 历史等安全存储能力需要 Android 6.0+，使用 Android Keystore 加密保护。发送与分享前尽力脱敏，但仍请检查内部地址、设备名和业务信息。

**不要上传密码、私钥或完整敏感配置。AI 可能给出不适用的命令，必须结合设备型号、版本和现场权限复核。**

## 三步开始

1. **安装**：打开 [V0.10.0 发布页](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0)，在 Assets 中下载正式 APK；校验值见同页 `SHA256SUMS.txt`。最低安装版本为 Android 5.0，推荐 Android 6.0+ 使用凭据加密相关功能。
2. **选问题**：从首页选择“上不了网”等场景并按提示填写目标。排查交换机时，先通过 USB OTG 或 SSH 连接设备，再手动运行向导给出的只读命令。
3. **看结果、做下一步**：返回“继续排障”读取回显、查看解释。需要 AI 时再配置自己的服务商，并在报告预览后确认发送。

请只检测你拥有或获授权管理的目标。生产设备操作前，确认设备、权限、维护窗口和回退方案。

## 常见问题

**不懂命令，可以用吗？**

可以从新手场景入手，先做手机侧检查，再按解释逐步排查。它帮助缩小范围，不保证一键修好所有网络问题。

**不接 AI 能用吗？**

能。终端、离线命令库、本地诊断和回显解释不依赖 AI。AI 是可选助手。

**连上交换机，就能自动读取完整配置吗？**

不会。当前设备读取取自已有终端输出；需要新信息时，先人工核对并执行只读命令。保存前还需检查分页、截断和内容完整性。

**为什么有些高级操作还要确认？**

因为改错目标或命令可能影响生产网络。AI 页面阻断 R4 命令，其余可装载命令也需人工审核；终端另有本地风险门禁。新手入口不会关闭这些保护。

**能替代现场验收吗？**

不能。本版本已通过 246 项单元测试、资源检查、正式包签名与对齐检查；Release Lint 为 0 错误、158 项告警。API 34 测试环境已验证新手流程，以及 V0.9.1 正式包覆盖升级到 V0.10.0 后的首页冷启动。物理手机、实际 USB 芯片、真实交换机与自有 AI 账号仍需现场验证。详见 [验证记录与未覆盖范围](Version/V0.10.0/docs/BEGINNER_VALIDATION.md)。

## 文档与源码

- [中文使用说明](Version/V0.10.0/README.md) · [English guide](Version/V0.10.0/README_EN.md)
- [安全边界](Version/V0.10.0/docs/SECURITY.md) · [安全问题报告](SECURITY.md)
- [构建与签名说明](Version/V0.10.0/docs/RELEASE.md) · [架构](Version/V0.10.0/docs/ARCHITECTURE.md)
- [开发路线](ROADMAP.md) · [贡献指南](CONTRIBUTING.md) · [历史版本](VERSION_HISTORY.md)

从源码构建预览包：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.10.0\scripts\build.ps1
```

完整版本源码保留在 `Version/V*`，生产签名材料不进入仓库。自行构建的预览包与正式版使用不同包名，不会自动迁移正式版数据。

## 一起把现场经验变成好用的工具

最欢迎三类反馈：**“我用什么设备遇到了什么问题”**、**“这段命令在我的版本不适用”**、**“这个结果我看不懂”**。提交 [Issue](https://github.com/chenwei666/NetSerial-AI/issues) 时附上机型、Android 版本、设备厂商 / 系统版本和脱敏后的复现步骤；不要附带密码或 API Key。

也欢迎补充命令、贡献中英文说明、测试真实设备或提交小而可验证的 PR。如果它在现场帮到了你，欢迎点一个 **Star**，方便以后找到，也让更多同行发现它。

## 来源与许可证

基于 Kai Morich 的 [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal) 扩展，保留上游 MIT 许可证和来源记录。新增功能与维护者：[chenwei666](https://github.com/chenwei666)。详见 [LICENSE](LICENSE) 与 [UPSTREAM.md](Version/V0.10.0/UPSTREAM.md)。
