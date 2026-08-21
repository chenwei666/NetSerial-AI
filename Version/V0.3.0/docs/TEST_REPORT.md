# V0.3.0 测试报告 / Test Report

- 日期 / Date：2026-08-21
- 测试人员 / Tester：chenwei666
- 环境：Windows、JDK 17、Android SDK Platform 36、Build Tools 36.0.0
- ASCII 构建镜像：`C:\tmp\NetSerial-build`

## 自动化门禁

| 门禁 | 结果 |
|---|---|
| `testDebugUnitTest` | 69 项通过，0 失败，0 错误 |
| `lintDebug` | 通过，0 阻断错误，68 条非阻断警告 |
| `assembleDebug` | 通过 |
| Android 5.0 / API 21 静态兼容 | 通过 Lint 阻断门禁 |

68 条非阻断 Lint 警告主要来自原上游 UI 的可访问性、旧 API、硬编码控制键和 SDK 属性兼容提示；本次新增 SSH/Telnet、命令库、设置和三个新 Activity 没有剩余专属 Lint 警告。没有创建 Lint baseline，也没有隐藏阻断错误。

## 新增测试覆盖

- `RemoteConnectionConfigTest`：SSH 合法参数、URL 误填、空 SSH 用户名、Telnet 交互式登录参数。
- `TelnetProtocolCodecTest`：IAC 协商拒绝、跨数据块状态、转义 IAC、子协商过滤。
- `CommonCommandCatalogTest`：四厂商十分类覆盖、厂商/分类隔离搜索、高风险命令标记。
- `AppSettingsTest`：Telnet 安全默认值、语言标签归一化、超时边界。
- 原 54 项 AI、Keystore 抽象、HTTPS、记忆、补全、终端控制和安全规则测试全部回归通过。

## APK

- applicationId：`com.chenwei666.netserial`
- versionName/versionCode：`0.3.0` / `3`
- minSdk/targetSdk：`21` / `36`
- 产物：`C:\tmp\NetSerial-build\app\build\outputs\apk\debug\app-debug.apk`
- 大小：8,436,125 bytes
- SHA-256：`27fd1624e3e4e0aa48b5e7b424b922e0e4743bfc654c95dccaa6ed3749a4d38b`
- 签名：Debug，仅供开发验收

## 安全检查

- 未使用真实 SSH/Telnet 账号、密码、API Key、Token 或现场 IP。
- SSH 密码没有持久化入口；远程终端关闭截图、自动填充和密码状态保存。
- SSH unknown/changed host 分支已做代码和状态审查；真实指纹交互仍需设备验收。
- Telnet 默认关闭、双重确认和 IAC 解析有单元测试；协议明文风险仍然存在。
- AI、命令库和补全均只填入草稿，R3/R4 发送确认同时接入 USB 和远程终端。

## 尚未执行的外部验收

- 未连接真实 Android 手机，未验证安装、语言切换、旋转、后台恢复和软键盘布局。
- 未连接真实 SSH 服务或 H3C/华为/Cisco/锐捷交换机，未验证首次指纹、重复连接、密钥变化、认证失败和长会话。
- 未在隔离网络连接 Telnet 设备，未验证各厂商特有协商、中文回显和登录提示。
- 未连接 USB 串口芯片或真实交换机，未验证实际 TAB 回显。
- 未提供真实 AI Key，因此未产生任何外部厂商请求或费用。
- 未配置生产签名，因此未执行 Release、覆盖升级和应用商店验收。

这些项目依赖真实硬件、账号或发布凭据，不能由本地自动化结果替代。
