# NetSerial AI 运维终端 V0.5.0

NetSerial AI 是面向网络运维工程师的 Android 移动终端，整合 USB Console、SSH、Telnet、多厂商命令库、Tab/控制键、AI 命令草稿、变更管控、配置对比和现场网络工具。

开发人员：chenwei666

## V0.5.0 新增

- 现代化 Material 3 界面：统一颜色、圆角卡片、层级、间距和触控尺寸。
- 深色模式：支持跟随系统、固定浅色、固定深色，切换后全应用统一生效。
- 主题切换：海洋蓝、翡翠绿、科技紫、日落橙；深浅模式均有独立高对比配色。
- 运维工作台：首页显示当前设备档案和进行中的变更任务，并提供 SSH/Telnet、AI、命令库、网络工具、变更任务和设置快捷入口。
- 命令收藏与最近使用：长按收藏内置命令，按全部/收藏/最近筛选；仅保存内置命令标识，不记录终端输入。
- 终端防休眠：可在设置中决定连接与现场操作期间是否保持屏幕常亮。
- USB 设备列表现代化并完成中英文提示，终端收发颜色适配深浅模式。

## 核心能力

- USB 串口：驱动探测、波特率、HEX、换行、流控、控制线和后台接收。
- SSH/Telnet：主机密钥校验、密码/键盘交互、会话私钥、跳板机、Keepalive 和 SFTP；Telnet 默认关闭且每次提示明文风险。
- 交换机命令：H3C Comware、Huawei VRP、Cisco IOS、Ruijie RGOS，按设备、接口、VLAN、三层、路由、环路、聚合、安全、排障、保存分类。
- AI：可配置多个内置厂商或自定义 OpenAI-compatible API；API Key 由 Android Keystore 保护；AI 输出只作为待审核草稿，不自动发送。
- 操作安全：R3 二次确认、R4 输入 `EXECUTE`、生产设备变更任务门禁、目标设备防错和敏感内容脱敏。
- 运维工具：IPv4/IPv6 CIDR、DNS、Ping、Traceroute、TCP、路径 MTU、MAC/OUI、常用端口、配置 Diff 和回滚草稿。
- 中英文：设置内可跟随系统或固定简体中文/English。

## 快速使用

1. 在“设备档案”中确认设备名称、厂商、环境和管理地址。
2. 从首页选择 USB 设备，或打开 SSH/Telnet 远程终端。
3. 使用 Tab、方向键、Ctrl+C 和问号快捷键辅助交互。
4. 从“命令库”按厂商和分类搜索；点击只填入/复制，长按可收藏。
5. 在“AI 设置”添加任意支持的厂商或兼容 API，再让 AI 检查、补全或生成四阶段命令计划。
6. 生产变更先建立变更任务，并在结束后导出脱敏 Markdown/PDF 证据。

## 构建

需要 JDK 17 与 Android SDK。Windows 推荐：

```powershell
.\scripts\build.ps1
```

已初始化生产签名的发布工作站：

```powershell
.\scripts\build.ps1 -Release
```

详细边界与验证见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)、[docs/SECURITY.md](docs/SECURITY.md)、[docs/TEST_REPORT.md](docs/TEST_REPORT.md) 和 [docs/RELEASE.md](docs/RELEASE.md)。

## 现场验收要求

自动化验证不能替代真实设备测试。正式用于生产前，请在授权环境验证目标 Android 版本、USB OTG 芯片、四类真实交换机、SSH 私钥/跳板机/SFTP、系统网络工具和自有 AI 账号。所有 AI、Diff 和回滚内容都必须由具备权限的工程师复核。
