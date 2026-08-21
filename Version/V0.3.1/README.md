# NetSerial AI 运维终端 V0.3.1

[中文](README.md) | [English](README_EN.md)

V0.3.1 是首个生产签名正式版。在保留 USB 串口、AI 多厂商接入、设备记忆和离线补全的基础上，包含独立 SSH/Telnet 远程终端、分类命令库、完整应用设置和可复现的安全发布流程。

## 快速使用

### USB Console

1. 通过 OTG 连接 USB 串口线，在设备列表中选择端口和波特率。
2. 在终端菜单进入“设备档案与 AI 记忆”，选择交换机厂商和当前 CLI 模式。
3. 输入命令时点击离线候选，或点击“命令库”按厂商和分类挑选草稿。
4. `TAB` 按钮会向设备发送真实字节 `0x09`；AI 和命令库只填入输入框，不自动发送。

### SSH

1. 从设备列表菜单打开“SSH / Telnet 远程终端”。
2. 选择 SSH，输入主机/IP、端口、用户名和本次连接密码。
3. 首次连接会显示服务器主机密钥信息。必须通过交换机 Console、运维平台或管理员提供的可信渠道核对指纹，再选择信任。
4. 已保存主机的密钥发生变化时，应用阻止连接。只有确认设备或密钥确实更换后，才可在“应用设置”中忘记已知主机并重新核对。

### Telnet

1. Telnet 默认关闭。进入“应用设置”阅读风险说明并显式启用。
2. 每次 Telnet 连接前仍需确认明文传输风险。
3. Telnet 登录在终端内手工完成；应用不保存 Telnet 用户名或密码。
4. 仅在隔离、可信的管理网络中临时使用；能够使用 SSH 时不要使用 Telnet。

## 命令库和 AI

- 厂商：H3C Comware、华为 VRP、Cisco IOS、锐捷 RGOS。
- 分类：设备信息、接口、VLAN、三层接口、路由、环路与生成树、链路聚合、安全与 MAC、故障排查、保存与备份。
- 命令模板中的接口、VLAN 和 RFC 5737 示例地址必须替换并核对；不同型号和系统版本以厂商文档及设备 `?`/TAB 回显为准。
- AI 支持 OpenAI、Claude/Anthropic、Gemini、DeepSeek、Qwen、Kimi、Ollama HTTPS 和自定义 OpenAI-compatible Endpoint。
- AI 可以生成或检查命令，并使用用户显式保存的设备记忆；AI 返回内容会再次经过本地风险分类，只作为草稿。
- R3 高风险命令发送前要求确认；R4 重启、擦除、升级等命令要求输入 `EXECUTE`。

## 应用设置

- 语言：跟随系统、简体中文、English；Android 13 及以上也声明了应用语言列表。
- Telnet：默认关闭。
- 远程连接超时：2–60 秒。
- 终端字号：12/14/18/22 sp，同时应用于 USB 和远程终端。
- 字符编码：UTF-8、GBK、ISO-8859-1。
- SSH 已知主机：可以清空，但会要求所有设备重新核验指纹。

## 安全边界

- SSH 强制主机密钥校验；未知主机需要人工确认，已知主机密钥变化会被拒绝。
- SSH 密码不写入 SharedPreferences、文件、日志、备份或源码；密码框关闭状态保存和自动填充，远程终端禁止截图。
- Telnet 明文风险无法由 App 消除，因此默认关闭并双重确认。
- AI Key 使用 Android Keystore AES-GCM；Endpoint 仅允许 HTTPS；Android 备份和应用明文 HTTP 关闭。
- 终端上下文发送给 AI 前执行 ANSI 清洗、敏感内容脱敏和长度限制，并且默认不附带。

## 构建

在当前 Windows 环境运行：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1
# 已初始化生产签名的发布机：
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1 -Release
```

脚本把项目镜像到 `C:\tmp\NetSerial-build`。普通构建执行 `testDebugUnitTest`、`lintDebug`、`assembleDebug`；正式构建执行 `testDebugUnitTest`、`lintRelease`、`assembleRelease`。需要 JDK 17、Android SDK 36 和当前 Windows 用户的生产签名。签名初始化与备份见 `docs/RELEASE.md`。

## 外部验收要求

- 真实 Android 手机上的安装、语言切换、旋转、后台恢复和软键盘布局。
- H3C、华为、Cisco、锐捷至少各一台设备的 SSH 主机密钥首次信任、重复连接和密钥变化阻断。
- 隔离实验网络中的 Telnet 登录、IAC 协商、UTF-8/GBK 输出和断线恢复。
- USB 串口芯片与真实 TAB 回显。
- 使用用户自己的测试账号验证各 AI 厂商；自动化开发过程没有调用真实 Key。
- V0.3.1 已生成并验签正式 Release APK；真实手机覆盖安装与现场交换机矩阵仍需外部验收。

开发负责人：chenwei666。
