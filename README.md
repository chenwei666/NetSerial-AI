# NetSerial AI

<p align="center">
  <img src="Version/V0.9.0/design/app-icon-512.png" width="128" alt="NetSerial AI app icon">
</p>

<p align="center">
  <strong>把 USB Console、SSH/SFTP、多厂商命令库与安全 AI 助手装进一部 Android 手机。</strong>
</p>

<p align="center">
  An open-source Android field terminal for network engineers.
</p>

<p align="center">
  <a href="https://github.com/chenwei666/NetSerial-AI/releases/latest"><strong>下载最新版 APK</strong></a>
  · <a href="README_EN.md">English</a>
  · <a href="ROADMAP.md">Roadmap</a>
  · <a href="https://github.com/chenwei666/NetSerial-AI/issues">反馈问题</a>
</p>

<p align="center">
  <a href="https://github.com/chenwei666/NetSerial-AI/releases/latest"><img src="https://img.shields.io/github/v/release/chenwei666/NetSerial-AI?label=release" alt="Release"></a>
  <img src="https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white" alt="Android 5+">
  <img src="https://img.shields.io/badge/UI-Material%203-6750A4" alt="Material 3">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="MIT License"></a>
</p>

NetSerial AI 面向需要在机房、弱电间和现场快速处理网络设备的工程师。它支持 H3C、华为、Cisco 与锐捷设备，通过 Android OTG USB 串口、SSH/SFTP 或受控 Telnet 建立连接，并提供离线命令库、配置 Diff、网络诊断和可插拔 AI Copilot。

> **安全原则：AI 只生成、检查和解释命令，绝不自动执行。最终发送权始终属于工程师。**

> **正式版状态：V0.9.0 已通过自动化门禁、合并到 `main` 并发布为 GitHub Latest；真实手机、交换机和自有 AI 账号兼容性仍需在授权环境现场验收。**

> **开发候选：V0.9.1 已实现连接后从运维中心一键读取当前 USB/SSH/Telnet 会话并自动识别厂商；尚未推送、合并或发布，Latest 仍指向 V0.9.0。**

## 为什么选择 NetSerial AI？

- **真正面向网络工程师**：不是普通聊天机器人，而是串口、远程终端、命令库和变更工作流的一体化工具。
- **一部手机完成现场运维**：使用 OTG 连接 Console 线，或通过 SSH/SFTP 管理远程设备。
- **支持主流网络厂商**：覆盖 H3C Comware、Huawei VRP、Cisco IOS 和 Ruijie RGOS 常用命令。
- **AI 可插拔**：覆盖 18 类国内外厂商和兼容接口，支持从上游 API 获取模型、缓存筛选和手工回退。
- **默认防误操作**：生产设备保护、目标精确匹配、维护窗口、R3/R4 风险门禁和脱敏变更证据。
- **隐私优先**：API Key 使用 Android Keystore + AES-GCM 加密，不进入源码、配置导出、终端记录或设备记忆。

## 核心能力

| 场景 | 能力 |
|---|---|
| 现场 Console | Android OTG USB 串口、波特率、流控、控制线、HEX、后台接收 |
| 远程管理 | SSH、SFTP、跳板机、Keepalive、主机密钥变更阻断 |
| 旧设备兼容 | 默认关闭的受控 Telnet，每次连接均提示明文风险 |
| 命令输入 | 真实 TAB、ESC、Ctrl+C、方向键、删除、问号补全 |
| 多厂商运维 | H3C、华为、Cisco、锐捷命令库、收藏与最近使用 |
| AI 运维助手 | 18 类厂商、多轮对话、加密历史、设备记忆、停止/重试/分享和安全命令装载 |
| 变更安全 | 配置 Diff、回滚草稿、目标校验、风险分级与人工确认 |
| 网络诊断 | IPv4/IPv6、DNS、Ping、Traceroute、受控多端口 TCP、MTU、地址摘要、MAC/OUI |
| V0.8 AI 与界面 | 18 类 AI 入口、智谱等国内厂商、上游模型同步、现代任务卡片和直达网络工具 |
| V0.9 运维闭环 | 一键故障取证、结构化运行手册、配置漂移、安全巡检和变更证据门禁 |

## 3 分钟开始使用

1. 从 [GitHub Releases](https://github.com/chenwei666/NetSerial-AI/releases/latest) 下载最新 APK。
2. 在 Android 5.0 及以上设备安装应用。
3. 使用 USB OTG Console 线连接交换机，或新建 SSH 连接。
4. 选择设备厂商并使用离线命令库；如需 AI，再在设置中配置自己的 API。
5. 检查命令与目标设备，确认风险提示后手动发送。

当前稳定版：**V0.9.0**

APK SHA-256：

```text
b4c4040cfc2f1895404dd0c9b6f8024f3824f4a1baf161621e132e504ee29697
```

## 安全工作流

```text
自然语言目标 / 手工命令
        ↓
AI 或离线命令库生成草稿
        ↓
本地规则重新评估风险
        ↓
工程师检查并主动发送
        ↓
R3 二次确认 / R4 输入 EXECUTE
        ↓
USB、SSH 或受控 Telnet 终端
```

AI、收藏命令和命令库均不会自动执行命令。Telnet 无法提供传输加密，只应在隔离且可信的管理网络中临时使用。

## 已支持与计划

当前 V0.9.0 正式版在 V0.8.0 全功能基础上增加完整应用内 AI 多轮对话、加密历史、一键故障取证、结构化运行手册、配置漂移和更严格的变更闭环。

接下来的重点包括：真实设备兼容矩阵、更多厂商命令、可复现演示、截图与使用视频。详见 [ROADMAP.md](ROADMAP.md)。

## 文档

- [V0.9.0 中文使用说明](Version/V0.9.0/README.md)
- [English README](README_EN.md)
- [架构说明](Version/V0.9.0/docs/ARCHITECTURE.md)
- [安全边界](Version/V0.9.0/docs/SECURITY.md)
- [测试报告](Version/V0.9.0/docs/TEST_REPORT.md)
- [版本历史](VERSION_HISTORY.md)
- [贡献指南](CONTRIBUTING.md)
- [安全问题报告](SECURITY.md)

## 从源码构建

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.9.0\scripts\build.ps1
```

当前最新源码位于 `Version/V0.9.0`，GitHub Latest 正式版为 V0.9.0；历史版本保留在 `Version/V*`。生产签名材料不会进入仓库。

## 参与项目

欢迎网络工程师、Android 开发者和真实设备用户参与：

- 提交设备兼容性结果或故障日志
- 补充 H3C、华为、Cisco、锐捷命令
- 改进中英文文档
- 提交安全边界清晰、可测试的 Pull Request
- 如果这个工具对你有帮助，请点一个 **Star**，让更多现场工程师找到它

## 来源与许可证

项目基于 Kai Morich 的 [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal) 扩展，并保留上游 MIT 许可证与来源记录。新增功能与维护者：[chenwei666](https://github.com/chenwei666)。

详见 [LICENSE](LICENSE) 和版本目录中的 `UPSTREAM.md`。
