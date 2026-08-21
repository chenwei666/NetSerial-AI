# NetSerial AI

> 为网络运维工程师打造的 Android 交换机终端：把 USB Console、SSH、受控 Telnet、命令补全、厂商命令库与 AI Copilot 放进同一个安全工作流。

[中文](README.md) | [English](README_EN.md)

[![Release](https://img.shields.io/github/v/release/chenwei666/NetSerial-AI?label=release)](https://github.com/chenwei666/NetSerial-AI/releases/latest)
[![Android](https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white)](Version/V0.3.1/app/build.gradle)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

NetSerial AI 面向经常维护 H3C、华为、Cisco、锐捷交换机的网络运维人员。它保留传统串口终端的直接、可靠，同时增加远程连接、离线命令辅助、可配置 AI 和本地安全门禁。AI 只负责生成、检查和解释命令草稿，最终发送始终由工程师确认。

## 核心功能

| 能力 | 说明 |
|---|---|
| USB Console | 支持 Android OTG 串口、波特率选择、HEX、换行、流控、控制线与后台连接服务。 |
| SSH | 交互式 Shell、首次主机指纹人工核验、已知主机保存与密钥变化阻断；密码不持久化。 |
| Telnet | 为旧设备保留的受控兼容模式；默认关闭，每次连接都提示明文风险。 |
| 真实终端控制 | TAB 发送真实 `0x09`，并提供 ESC、Ctrl+C、Ctrl+Z、方向键、退格、删除和 `?` 快捷键。 |
| 厂商命令库 | H3C Comware、华为 VRP、Cisco IOS、锐捷 RGOS，覆盖十类常用运维命令。 |
| 离线补全 | 根据厂商、CLI 模式和当前输入给出本地候选；无网络时仍可使用。 |
| AI Copilot | 用自然语言生成、检查、补全和解释交换机命令，可选择附带脱敏后的终端上下文。 |
| 设备记忆 | 保存设备角色、厂商、CLI 模式和经确认的运维知识；支持过期时间与安全导入导出。 |
| 本地风险控制 | R0–R4 命令风险分级；高风险命令二次确认，重启、擦除、升级等命令必须输入 `EXECUTE`。 |
| 中英文界面 | 支持跟随系统、简体中文和 English，并可调整终端字号、字符编码和连接超时。 |

## AI 接入

应用内可以创建、测试和切换多个 AI 配置，API Key 由用户在本机输入。

- OpenAI
- Claude / Anthropic
- Google Gemini
- DeepSeek
- 通义千问 / Qwen
- Kimi
- Ollama（HTTPS）
- 自定义 OpenAI-compatible HTTPS Endpoint

AI 凭据使用 Android Keystore + AES-GCM 保存，不进入配置导出、终端日志、设备记忆或源码。切换厂商或 Endpoint 时要求重新输入凭据，避免把旧 Key 误发到新目标。

## 命令库分类

四个厂商均提供以下常用分类：

1. 设备信息
2. 接口状态与配置
3. VLAN
4. 三层接口
5. 路由
6. 生成树与环路排查
7. 链路聚合
8. 安全与 MAC
9. 故障排查
10. 保存与备份

命令库是离线运维起点，不替代对应型号和系统版本的官方命令手册。模板中的接口、VLAN 和示例地址必须由工程师核对后再发送。

## 安全工作流

```text
自然语言目标 / 手工命令
          ↓
AI 或离线命令库生成草稿
          ↓
本地规则重新评估风险（AI 不能降低风险等级）
          ↓
工程师检查、编辑并主动发送
          ↓
R3 二次确认 / R4 输入 EXECUTE
          ↓
USB、SSH 或受控 Telnet 终端
```

- AI 和命令库不会自动执行命令。
- 最近终端上下文默认不发送；启用后会先清理 ANSI、遮蔽敏感内容并限制长度。
- SSH 未知主机必须人工核对指纹，已知主机密钥变化会阻止连接。
- Telnet 无法提供传输加密，只建议在隔离、可信的管理网络临时使用。
- Android 应用备份与普通 HTTP 默认关闭。

## 下载与安装

正式版本：[GitHub Releases](https://github.com/chenwei666/NetSerial-AI/releases/latest)

当前版本：**V0.3.1**  
系统要求：**Android 5.0 / API 21 及以上**

V0.3.1 APK SHA-256：

```text
f4c410c3bf0016ecc5532bcdc27aa3ed109539d352983246a8bfd90c14972c76
```

如果设备安装过 V0.3.0 Debug APK，由于签名不同，需要先卸载 Debug 版，再安装正式版。V0.3.1 之后的正式版本将使用同一生产证书覆盖升级。

## 快速开始

1. 安装正式 APK，通过 OTG 接入 USB Console，或打开“SSH / Telnet 远程终端”。
2. 在“设备档案与 AI 记忆”中选择交换机厂商和 CLI 模式。
3. 在“AI 设置”中选择预设厂商或自定义 HTTPS Endpoint，并在本机录入 API Key。
4. 输入自然语言目标，让 AI 生成或检查命令；也可以直接使用离线补全和分类命令库。
5. 检查设备型号、软件版本、接口、VLAN、地址及风险提示后，再手动发送命令。

详细说明：

- [V0.3.1 中文使用说明](Version/V0.3.1/README.md)
- [V0.3.1 English guide](Version/V0.3.1/README_EN.md)
- [远程连接与安全](Version/V0.3.1/docs/REMOTE_CONNECTIONS.md)
- [AI 厂商兼容说明](Version/V0.3.1/docs/AI_PROVIDER_COMPATIBILITY.md)
- [架构说明](Version/V0.3.1/docs/ARCHITECTURE.md)
- [正式发布与签名](Version/V0.3.1/docs/RELEASE.md)
- [测试报告](Version/V0.3.1/docs/TEST_REPORT.md)

## 构建与验证

需要 JDK 17、Android SDK Platform 36 和 Build Tools 36.0.0。在 Windows PowerShell 中：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.3.1\scripts\build.ps1
```

V0.3.1 发布门禁结果：

- 23 个测试套件、69 项单元测试全部通过。
- Release Lint：0 Error。
- APK 通过 ZIP 对齐和 Android V1/V2/V3 签名验证。
- applicationId：`com.chenwei666.netserial`。

生产签名材料不在仓库中。完整源码按版本独立保存在 `Version/V*`，历史版本不会被覆盖。

## 项目边界

- 尚需在更多真实 Android 设备、USB 串口芯片和不同系统版本的交换机上持续完成硬件矩阵验证。
- SSH 当前支持密码认证；私钥、跳板机、代理和端口转发尚未实现。
- Telnet 是明文协议，应用只能限制其使用条件，不能消除协议本身的风险。
- 厂商 API、模型名称和配额可能变化，请以服务商当前文档为准。

## 来源与许可证

本项目基于 Kai Morich 的 [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal) 扩展，保留原 MIT 许可证与上游来源记录。新增功能与维护者：`chenwei666`。

详见 [LICENSE](LICENSE) 和各版本目录中的 `UPSTREAM.md`。
