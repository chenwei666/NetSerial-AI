# NetSerial AI

<img src="Version/V0.5.0/design/app-icon-512.png" width="144" alt="NetSerial AI 应用图标">

> 面向网络运维工程师的 Android 移动终端：把 USB Console、SSH/SFTP、受控 Telnet、多厂商交换机命令、AI 命令审查与变更安全工作流装进一部手机。

[中文](README.md) | [English](README_EN.md)

[![Release](https://img.shields.io/github/v/release/chenwei666/NetSerial-AI?label=release)](https://github.com/chenwei666/NetSerial-AI/releases/latest)
[![Android](https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white)](Version/V0.5.0/app/build.gradle)
[![Theme](https://img.shields.io/badge/UI-Material%203-6750A4)](Version/V0.5.0/README.md)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

NetSerial AI 面向经常维护 H3C、华为、Cisco、锐捷交换机的网络工程师。它既能作为可靠的 USB 串口终端，也能通过 SSH/SFTP 或经明确授权的 Telnet 连接旧设备；内置命令库、Tab 补全、网络诊断、配置 Diff、变更留证，以及可接入主流厂商或自定义兼容接口的 AI Copilot。AI 只生成、检查和解释草稿，任何命令最终仍由工程师确认发送。

## 发布状态与版本历史

- 当前最新稳定正式版：[V0.5.0](https://github.com/chenwei666/NetSerial-AI/releases/latest)，包含现代化界面、深色模式、四套主题、全新图标和 V0.4.0 的完整运维工作流。
- V0.3.1 是首次启用长期生产签名的历史稳定版；V0.5.0 沿用同一证书，可从 V0.3.1/V0.4.0 覆盖升级。
- V0.2.0、V0.3.0 是没有可验证 APK 的历史源码检查点；V0.4.0 保持候选版，V0.5.0 已提升为 GitHub `Latest` 正式版。
- 完整演进、兼容性和下载说明见 [版本历史](VERSION_HISTORY.md)，发布后元数据调整见 [项目 CHANGELOG](CHANGELOG.md)；全部版本源码保存在 [`Version/`](Version/) 中，历史归档不覆盖。

| 版本 | 定位 | 主要里程碑 |
|---|---|---|
| V0.1.0 | 可信基线 / Debug | USB 串口、真实 Tab、离线补全、R0-R4 安全模型 |
| V0.2.0 | 历史源码检查点 | 多 AI 厂商、加密 API Key、本地记忆、四厂商补全 |
| V0.3.0 | 历史源码检查点 | SSH、受控 Telnet、分类命令库、中英文设置 |
| V0.3.1 | 首个稳定正式版 | 生产签名、完整 Release 门禁、后续覆盖升级基线 |
| V0.4.0 | 候选版 | 变更任务、配置 Diff、网络工具、SFTP、跳板机 |
| V0.5.0 | 最新稳定正式版 | Material 3、深浅模式、四套主题、运维工作台、新图标 |

## V0.5.0

- Material 3 现代化界面，支持跟随系统、浅色、深色。
- 海洋蓝、翡翠绿、科技紫、日落橙四套主题。
- 首页运维工作台：设备档案、活动变更和六个快捷工具。
- 命令收藏、最近使用和分类筛选。
- 可选终端防休眠，深浅模式专用终端颜色。
- V0.4.0 的 USB、SSH/Telnet、SFTP、AI、变更门禁、配置 Diff 与网络工具全部保留。

## 核心能力

| 能力 | 说明 |
|---|---|
| USB Console | Android OTG 串口、波特率、HEX、换行、流控、控制线和后台接收。 |
| SSH / SFTP | 密码、键盘交互、会话私钥、跳板机、Keepalive、SFTP 和主机密钥变更阻断。 |
| Telnet | 面向旧设备的受控兼容模式；默认关闭，每次连接提示明文风险。 |
| 真实终端键 | TAB 发送真实 `0x09`，并提供 ESC、Ctrl+C、方向键、删除和 `?`。 |
| 多厂商命令库 | H3C Comware、Huawei VRP、Cisco IOS、Ruijie RGOS，覆盖十类常见运维命令。 |
| 命令收藏 | 长按收藏内置命令，支持全部、收藏和最近使用筛选。 |
| AI Copilot | 接入多个内置厂商或自定义 OpenAI-compatible HTTPS API，生成、补全、检查和解释命令。 |
| AI 记忆 | 按设备保存经确认的非敏感运维知识，支持过期、导入与导出。 |
| 变更与防错 | 维护窗口、生产设备保护、目标精确匹配、R3/R4 门禁和脱敏证据。 |
| 运维工具 | 配置 Diff、回滚草稿、IPv4/IPv6、DNS、Ping、Traceroute、TCP、MTU、MAC/OUI。 |
| 外观与语言 | 系统/浅色/深色、四套主题、简体中文/English、字体与字符集设置。 |

## AI 厂商

内置 OpenAI、Anthropic、Gemini、DeepSeek、Qwen、Kimi 和 HTTPS Ollama，也支持自定义 OpenAI-compatible Endpoint、模型和 API Key。凭据使用 Android Keystore + AES-GCM 保护，不进入源码、配置导出、终端记录或设备记忆。

## 安全工作流

```text
自然语言目标 / 手工命令
        → AI 或离线命令库生成草稿
        → 本地规则重新评估风险
        → 工程师检查并主动发送
        → R3 二次确认 / R4 输入 EXECUTE
        → USB、SSH 或受控 Telnet 终端
```

- AI 和命令库不会自动执行命令。
- Telnet 无法提供传输加密，只应在隔离且可信的管理网络临时使用。
- 生产保护、变更门禁和目标防错不会因为 AI 或主题切换而放宽。

## 下载与安装

正式版本：[GitHub Releases](https://github.com/chenwei666/NetSerial-AI/releases/latest)

V0.5.0 APK SHA-256：

```text
c16a95034a45adbf3c38a9e24a0211aeb9d4197d52132420e47f4c00ab264cf3
```

V0.4.0 与 V0.5.0 使用同一生产证书，可覆盖升级并保留应用数据。

## 文档

- [V0.5.0 中文使用说明](Version/V0.5.0/README.md)
- [V0.5.0 English guide](Version/V0.5.0/README_EN.md)
- [架构说明](Version/V0.5.0/docs/ARCHITECTURE.md)
- [安全边界](Version/V0.5.0/docs/SECURITY.md)
- [测试报告](Version/V0.5.0/docs/TEST_REPORT.md)
- [正式发布与签名](Version/V0.5.0/docs/RELEASE.md)

## 构建

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.5.0\scripts\build.ps1
```

完整源码按版本独立保存在 `Version/V*`，历史版本不覆盖。生产签名材料不进入仓库。

## 来源与许可证

本项目基于 Kai Morich 的 [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal) 扩展，保留原 MIT 许可证与上游来源记录。新增功能与维护者：`chenwei666`。

详见 [LICENSE](LICENSE) 和版本目录中的 `UPSTREAM.md`。
