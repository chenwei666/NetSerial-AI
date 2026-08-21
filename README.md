# NetSerial AI

> 为网络运维工程师打造的现代化 Android 交换机终端：USB Console、SSH、受控 Telnet、多厂商命令库、Tab 补全、AI Copilot 与变更安全工作流。

[中文](README.md) | [English](README_EN.md)

[![Release](https://img.shields.io/github/v/release/chenwei666/NetSerial-AI?label=release)](https://github.com/chenwei666/NetSerial-AI/releases/latest)
[![Android](https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white)](Version/V0.5.0/app/build.gradle)
[![Theme](https://img.shields.io/badge/UI-Material%203-6750A4)](Version/V0.5.0/README.md)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

NetSerial AI 面向经常维护 H3C、华为、Cisco、锐捷交换机的网络工程师。它保留传统串口终端的直接与可靠，同时整合远程连接、离线命令辅助、任意兼容 AI API、本地记忆、变更留证和确定性的安全门禁。AI 只生成、检查和解释草稿，最终发送始终由工程师确认。

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
ca3b337f24a07c981b95a8c8056be64a1078b46279f3f02c8c6db4cb729a76c8
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
