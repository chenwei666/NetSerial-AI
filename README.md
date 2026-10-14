# NetSerial AI

<p align="center">
  <img src="Version/V0.12.0/design/app-icon-512.png" width="112" alt="NetSerial AI 应用图标">
</p>

<p align="center">
  <strong>手机和平板，就是你的随身网络运维工具箱。</strong><br>
  不懂命令，按问题排查；熟悉现场，直达专业工具。<br>
  免 Root 网络诊断 · USB Console · SSH/SFTP · 多厂商命令 · AI 对话
</p>

<p align="center">
  <a href="https://github.com/chenwei666/NetSerial-AI/releases/latest"><strong>下载最新正式版</strong></a>
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

**NetSerial AI 是一个开源 Android 网络运维工具箱。** 从“上不了网”“交换机登不上”“端口不通”出发，收集证据、读懂结果，再决定下一步；也可以直接打开 Ping、DNS、TCP、TLS 等工具做专业检查。

V0.12.0 把常用网络工具重新整理为 **16 个清晰入口**：可搜索、按用途分类，结果先显示数值和结论。AI 对话也换成更易读的排版，设置固定在右上角；重要信息由你确认后再记住。

> **当前正式版：[V0.12.0 · GitHub Latest](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.12.0)。** [PR #16](https://github.com/chenwei666/NetSerial-AI/pull/16) 已合并，正式 Release 已发布（非草稿、非预发布）；线上 APK 与 `SHA256SUMS.txt` 重新下载核验一致。V0.10.0 保留为[历史稳定版](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0)。

> **AI 不替你操作交换机。** 回复与命令都是待审核草稿，不会自动下发；真正发送仍由你核对设备、命令和风险。

## 遇到问题，直接找到工具

| 你想解决的问题 | 可以打开 |
|---|---|
| 上不了网，不知道从哪查 | 基础体检：组合单目标 DNS、Ping、TCP，查看证据与下一步 |
| 网络慢、偶尔掉线 | Ping：逐包 RTT、发送/接收、丢包率、最小/平均/最大值与趋势 |
| 交换机、服务端口连不上 | TCP 检测：指定端口，区分拒绝、超时和连接成功 |
| 域名或网站打不开 | DNS 查询、HTTP/HTTPS 响应头检查、TLS 证书检查 |
| 想知道路径或报文大小问题 | 免 Root 路由追踪、IPv4 DF/MTU 探测，明确未知或受限结果 |
| 看本机网络、找授权私网内的设备 | 当前网络信息、地址摘要、受限局域网发现 |
| 远程设备支持网络唤醒 | Wake-on-LAN：确认目标与 MAC 后发送唤醒报文 |
| 要算网段、查 MAC 或常用端口 | IPv4/IPv6 CIDR、MAC/OUI、端口速查、网络标识提取 |
| 不懂交换机输出、记不住命令 | 新手向导、多厂商只读命令、报告预览与 AI 对话 |

## 手机和平板，都有合适的布局

### 平板 · 浅色

![V0.12.0 平板浅色网络工具箱](Version/V0.12.0/docs/images/toolbox-tablet-light.png)

### 手机窄屏 · 深色

<img src="Version/V0.12.0/docs/images/toolbox-phone-dark.png" width="380" alt="V0.12.0 手机窄屏深色网络工具箱">

截图来自 Android API 34 测试环境的实际应用，不是效果图，也不代表所有实体手机、平板或交换机已经完成兼容验收。支持中英文、跟随系统 / 浅色 / 深色及既有四套主题。

- 工具搜索与用途分类，选中后只显示相关参数，不再堆满按钮。
- 数值、结果明细与进度优先；原始输出折叠保留，便于专业排查。
- 支持停止任务；失败、部分完成、不支持、取消分别解释，不把错误文字当成检测成功。
- 报告先脱敏预览，再复制、分享或带入 AI 草稿；不会自动发送给 AI。

## AI：读得清楚，也记得有分寸

标题、粗体、列表、引用、行内代码和代码块原生排版，不再满屏显示 `**`。命令中的真实星号与换行仍保留，支持单独复制代码、复制原文和人工审核命令。

**确认后记住：** 点击“记住这条” → 编辑内容 → 选择个人偏好或已命名设备档案 → 确认保存。记忆可查看、编辑、删除、关闭使用，与聊天历史分开；删除聊天不会误删记忆，也不会把每次问答自动存为长期记忆。

- Android 6.0+ 加密保存，180 天到期，每次最多选用 5 条适用记忆，可查看最近请求选用了哪些条目。
- 设备范围按档案名称区分，不是验证后的硬件身份；旧自动记忆不自动导入。
- 多轮会话、停止/重试、历史管理和既有命令风险门禁继续保留。

内置 **18 类 AI 接入入口**：OpenAI、Anthropic、Gemini、DeepSeek、智谱 GLM、通义千问、豆包、腾讯混元 / TokenHub、百度千帆、Kimi、MiniMax、硅基流动、Groq、Mistral、xAI、OpenRouter、自定义兼容接口和 HTTPS Ollama。使用自己的 API Key，按接入能力同步上游模型，支持缓存、筛选和手动填写。

自定义入口面向 **OpenAI-compatible** 接口，不等于任何厂商的任意协议均可直连。在线 AI 会收到你确认发送的上下文与启用的记忆，费用及数据规则以服务商为准。**不要发送密码、私钥或未经检查的敏感配置。** 本地删除不能撤回服务商已接收的内容。

## 工程师常用能力，继续保留

| 能力 | 现场用途 |
|---|---|
| USB Console | OTG 串口、波特率、流控、控制线、HEX 与后台接收 |
| SSH / SFTP | 远程终端、跳板机、Keepalive、文件传输、主机密钥变更阻断 |
| 受控 Telnet | 兼容旧设备，默认关闭，每次连接提示明文风险 |
| 命令辅助 | 真实 Tab、问号、Ctrl+C、方向键；H3C / Huawei / Cisco / Ruijie 分类命令 |
| 配置与变更 | 脱敏快照、Diff、回滚草稿、目标校验、维护窗口与变更证据 |
| 运维中心 | 故障取证计划、运行手册、安全巡检、拓扑回显解析与厂商识别 |
| 新手排障 | 十个问题场景，当前终端已有回显读取、解释与继续排障 |

Tab 补全依赖设备终端本身。厂商命令与识别规则不表示全部型号或固件已实测。读取当前设备只利用已有终端输出，不偷偷发送命令；缺页/截断配置不是完整恢复备份。

## 检测有边界，结果不夸大

- **不需要 Root，也不绕过系统权限。** Trace 依赖系统 TTL 探测能力及路由器回包，未知跳保持未知；IPv4 DF/MTU 超时不被当作准确上限，1500 字节成功仅证明该大小可通过。
- **主动发现需要确认。** 仅 RFC1918 私网 `/26`–`/32`、最多 64 个地址和 4 个指定 TCP 端口、40 秒预算。未响应不等于离线，不是完整资产扫描。WOL 已发送不等于设备已开机。
- **协议检查保持安全。** DNS 使用系统或明确指定服务器，不暗中切换公共 DNS，不验证 DNSSEC。HTTP 仅 HEAD、不跟随跳转、不下载正文、不发送账号或 Cookie；TLS 不忽略证书/域名错误，不做完整协议或吊销扫描。
- **不是一键修复或带宽测速。** 检查从手机/平板发起；Ping 不通不证明离线，TCP 成功不证明账号可登录。OUI 内置数据有限，未知厂商不猜测。

## 三步开始

1. **安装并核对版本**：从 [V0.12.0 正式发布页](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.12.0) 获取 APK 与同页 `SHA256SUMS.txt`。旧正式版可使用同证书正式包覆盖安装，不要先卸载；独立 Preview 不是正式版覆盖升级包。
2. **选问题或工具**：新手从首页场景开始，熟悉操作可直达工具箱搜索。只有获授权的目标才应被检测；私网发现与 WOL 需核对后确认。
3. **看结果，再决定下一步**：需要交换机回显时先连接 USB/SSH 并手动执行检查命令；需要 AI 时再配置服务商，预览上下文后手动发送。

最低 Android 5.0；加密凭据、持久化聊天/记忆及当前默认网络详情需要 Android 6.0+。新增普通 `ACCESS_NETWORK_STATE` 权限用于读取当前网络/DNS；不申请 Root、定位或 VPN 抓包权限。

## 验证与文档

本地发布验收已通过：**338 项 JVM 测试**，Debug/Release Lint **0 错误 / 218 警告**，手机/平板 × 中英文 × 深浅色 **8 组界面检查，每组 138 项断言**，以及 **64 项 AI 回归断言**。正式 APK 的 V1/V2/V3 签名与 ZIP 对齐通过，证书与重新下载的 V0.10.0 正式 APK 一致；API 34 干净测试环境已完成 V0.10.0 → V0.12.0 覆盖安装、首次安装时间保持与冷启动检查。

V0.12.0 已发布为 GitHub Latest，远程重新下载的 APK 为 **6,506,868 bytes**，SHA-256 为 `fce9f7d5a4a60fe16d72ab357dabfbd769347767549b22075a6b8d6325f19cf8`，与本地产物和线上校验文件一致。

上述验证不证明真实用户凭据/历史迁移，也不代替实体手机、USB 芯片、交换机、公网服务或自有 AI 账号验收。另一次正式包 Ping 界面附加复测因模拟器 `system_server` 退出而未完成，未计为通过；详见 [测试报告](Version/V0.12.0/docs/TEST_REPORT.md)。

- [中文使用说明](Version/V0.12.0/README.md) · [English guide](Version/V0.12.0/README_EN.md)
- [安全边界](Version/V0.12.0/docs/SECURITY.md) · [安全问题报告](SECURITY.md)
- [构建与签名](Version/V0.12.0/docs/RELEASE.md) · [架构](Version/V0.12.0/docs/ARCHITECTURE.md)
- [变更记录](CHANGELOG.md) · [历史版本](VERSION_HISTORY.md) · [贡献指南](CONTRIBUTING.md)

从源码构建独立预览：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.12.0\scripts\build.ps1
```

Debug 包名 `com.chenwei666.netserial.preview012`，与正式版共存，不自动导入其凭据和历史。完整历史源码保存在 `Version/V*`；V0.11.0 是并入 V0.12.0 的开发记录，不单独发布 Release。生产签名材料不进入仓库。

## 一起把现场经验变成好用的工具

最欢迎三类反馈：**“什么设备遇到什么问题”**、**“这条命令在我的固件不适用”**、**“这个结果我看不懂”**。提交 [Issue](https://github.com/chenwei666/NetSerial-AI/issues) 时附机型、Android 版本、设备厂商/固件与脱敏复现步骤，不附带密码或 API Key。

欢迎贡献命令、中英文说明、真实设备验证或小而可复现的 PR。如果它在现场帮到了你，欢迎点一个 **Star**，方便以后找到，也让更多同行发现它。

## 来源与许可证

基于 Kai Morich 的 [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal) 扩展，保留 MIT 许可证与上游署名。新增功能与维护者：[chenwei666](https://github.com/chenwei666)。详见 [LICENSE](LICENSE) 与 [UPSTREAM.md](Version/V0.12.0/UPSTREAM.md)。
