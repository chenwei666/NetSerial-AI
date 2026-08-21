# V0.5.0 架构说明

## V0.5.0 外观与工作台增量

- `ThemedActivity` 是所有 Activity 的统一入口，在页面创建前应用 `AppAppearanceController` 与语言设置，创建后应用窗口常亮策略。
- `AppSettings`/`AppSettingsStore` 继续作为唯一设置模型，新增 `AppearanceMode`、`AccentTheme` 和 `keepScreenAwake`，旧构造器和旧 SharedPreferences 数据兼容。
- Material 3 DayNight 提供基础设计令牌；四个主题只覆盖主色/容器色，深色资源通过 `values-night` 自动替换，避免页面级硬编码主题。
- `DevicesFragment` 仍保持 USB 列表职责，但列表头作为只读运维摘要与导航入口；设备和变更数据仍由各自 Store 提供。
- `CommandUsageHistory` 是可单元测试的纯 Java 领域对象；`CommandUsageStore` 只负责 Android 持久化，命令库只传入内置 `CommonCommand` 标识。

## V0.4.0 基础架构

## 设计原则

V0.4.0 保持单 APK、离线优先和显式人工确认。连接层只负责传输，终端层负责显示与输入，安全层负责统一分级，变更与配置模块负责留证，AI 层只生产草稿。新增能力通过领域对象和小型服务加入，不改变 V0.3.1 的 USB/SSH/Telnet 基础行为。

## 模块

| 模块 | 主要职责 |
| --- | --- |
| `change` | 变更任务、状态与事件、脱敏证据、Markdown/PDF 导出 |
| `device` | 设备名、厂商、环境、保护标记、管理地址与迁移 |
| `safety` | R0-R4 分类、多行批次检查、受保护目标策略 |
| `config` | 配置规范化、哈希、差异、回滚草稿 |
| `network` | CIDR 计算、受控探测、OUI、端口和标识提取 |
| `remote` | SSH/Telnet 生命周期、认证、跳板、SFTP、主机密钥 |
| `terminal` | 显示缓冲、ANSI 清理、脱敏、目标颜色、控制键 |
| `ai` | 服务商适配、结构化计划、四阶段校验、上下文清理 |
| `memory` | 用户明确保存的设备事实和摘要 |
| `settings` | 语言、确认策略、超时、Keepalive 与设置迁移 |

## 关键数据流

1. 用户先保存设备档案并创建变更任务。
2. USB 或远程连接建立后，目标条持续显示设备/地址/环境/保护状态。
3. 发送前由 `RuleBasedExecutionGuard` 对每一行命令分级，再由 `TargetSafetyPolicy` 检查受保护目标与维护窗口。
4. 命令和返回结果经 ANSI 清理、敏感信息脱敏后写入变更时间线；终端输出本身不因留证而中断。
5. AI 上下文经过敏感信息与提示注入清理；返回计划经结构解析、四阶段完整性检查和风险规则复核，只展示为草稿。
6. 配置对比对规范化文本生成 SHA-256 和逐行差异，回滚生成器仅给出待审草稿。
7. 完成变更后，从任务页导出 Markdown 或 Android `PdfDocument` 生成的 PDF。

## 依赖方向

- Activity 依赖领域服务和 Store；领域对象不依赖 Activity。
- `remote` 不持有 Activity 实例，只通过监听器回传状态、文本、错误与主机密钥确认请求。
- `change` 的证据记录器复用 `terminal` 脱敏器。
- `ai`、`safety`、`config` 之间通过不可变结果对象传递数据，不直接操作连接。

## 持久化

- SharedPreferences：非秘密设置、设备档案、变更任务和已经脱敏的时间线。
- Android Keystore：AI API Key 的加密密钥材料。
- 应用私有文件：SSH `known_hosts`。
- 不持久化：SSH/Telnet 口令、私钥原文、私钥口令、跳板机口令。

## 兼容性

最低 Android API 21。进程等待等实现不使用 API 26 才提供的重载。网络诊断不通过 shell 拼接命令，目标经过格式校验后作为独立参数传入系统程序。
