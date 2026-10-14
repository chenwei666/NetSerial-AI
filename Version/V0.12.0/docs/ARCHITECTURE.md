# V0.12.0 架构说明

## V0.12.0 网络工具箱

- `NetworkToolsActivity` 负责 16 项工具目录、分类/搜索、按需参数、手机/平板布局、确认窗口、执行状态与结构化展示。每次运行从 UI 捕获不可变 `ToolRequest`，切换/退出取消活动任务；代次标识防止过期回调覆盖当前结果。展示数值、逐项明细和 Ping 曲线，原始输出默认折叠。
- `network.toolbox.ToolboxService` 是明确工具 ID 到领域探针的路由，构造不运行探测。它通过 `ConnectivityManager`/`LinkProperties` 获取当前默认网络信息与 DNS，受 `ACCESS_NETWORK_STATE` 和系统版本限制；基础体检组合目标 DNS、Ping 和指定 TCP 端口，字面 IP 跳过无意义的 DNS 查询。
- `ToolRequest` 捕获目标、选项、超时、次数与语言；`ToolResult` 含 OK/PARTIAL/FAILED/UNSUPPORTED/CANCELLED、标题、说明、明细、原始文本与 RTT 样本。丢失样本为 `NaN`，不是 0；导出与界面使用相同事实。`ToolFailures` 把异常归为安全可读结果，不依赖原始错误文案推断成功。
- `ProbeSession` 统一单任务 deadline、进度及可关闭资源。页面取消任务时关闭资源并中断执行器任务，任务结束也释放资源。`ToolboxResolver` 复用 `guided.network.BoundedDnsResolver` 的有界共享线程池，避免另建无限 DNS 工作线程。
- `CoreProbes`、`BoundedPingProcess` 与 `PingOutput` 分离测量编排、有限系统进程和可单测输出解析。Ping 从回包获取 RTT；Trace 以 TTL 递增进行免 Root 探测；MTU 只使用 IPv4 DF 证据，未收到证据不推定准确上限。进程权限与工具能力不足返回明确限制，不要求 Root。
- `ConnectionProbes` 处理单目标 TCP 与受限私网发现；发现目标由 RFC1918 `/26`–`/32` 校验生成，最多 64 个地址/4 个端口/40 秒预算，不动态扩网。WOL 的确认属于 UI，单次报文编码/发送在 `AdvancedProbes`，不混入“设备已经唤醒”的推断。
- `AdvancedProbes` 提供 DNS/HTTP/TLS/WOL；`DnsWire` 是纯 Java 有界协议编解码器。DNS 使用系统提供或显式字面服务器，验证问题和事务、限制压缩指针与长度，UDP 截断只对同目标做 TCP 回退。HTTP 是无凭据 HEAD 的有界 Socket 交换，不共享 Cookie/认证、不跟随跳转。HTTPS/TLS 依靠系统信任与目标身份校验，不引入自签名放行或全信任逻辑。
- `OfflineTools` 复用既有 IPv4/IPv6 计算、MAC/OUI、端口目录和文本提取领域模块，不访问网络。内置 OUI 不完整时返回未知，不联网猜测厂商。
- 报告路径为 `ToolResult → SensitiveTextRedactor → 预览 → 用户复制/分享或 AI 草稿`。HTTP 报告在进入统一脱敏器前已排除 Cookie 和 URL 路径/查询等字段。AI 草稿仍需用户主动发送，既有上下文清理、确认式记忆和命令风险门禁不变。
- 版本为 `0.12.0` / code `14`，Debug 身份 `.preview012`，构建镜像 `C:/tmp/NetSerial-v0120-build`。本次无数据库、凭据格式或生产身份迁移，无生产发布；V0.11.0 AI 功能继续作为本版本基础。

关键数据流：`用户选工具 → 校验输入/必要时确认 → 不可变请求 → 有界会话与具体探针 → 结构化事实/能力限制 → 界面与报告预览 → 用户决定下一步`。

历史章节描述各版本当时设计；当前网络工具箱行为以 V0.12.0 章节为准，既有向导仍使用它自己的单目标边界。

## V0.11.0 AI 聊天与确认记忆

- `AiCopilotActivity` 继续负责聊天请求与页面状态；长期记忆开关和聊天历史各自独立。用户通过“记住”或管理入口打开 `ChatMemoryController`，编辑草稿、选择个人偏好或当前设备范围，再明确确认保存。没有自动提取、自动保存长期记忆或新增后台 AI 请求。
- `ConfirmedMemoryStore` 是可独立测试的纯 Java 领域服务，负责 GLOBAL/DEVICE 范围校验、180 天到期、秘密拒绝、100 条/单条 1024 字符/总内容 32768 字符限额、原子替换更新，以及同进程多实例的统一互斥。列表和召回返回不可变结果；读取不执行清理写入。
- `EncryptedConfirmedMemoryPersistence` 仅承担加密持久化，复用 `CredentialVaultFactory` 和 Android Keystore AES-GCM，通过独立存储 alias/AAD `confirmed-chat-memory-v1` 隔离用途。独立的是记录与 schema，不是重新创建的 Keystore 密钥。初始化标记只保存布尔状态，用于防止已初始化但缺失/损坏的密文被误认作新空库。
- 新记忆不迁移旧 `MemoryVault` 的自动记录或导入数据，也不以旧记录作为失败回退；旧存储仍服务其原有兼容页面。Android API 21–22 不提供明文长期记忆降级，管理界面说明需要 Android 6.0+。
- 设备作用域取自当前设备档案名称，按名称精确隔离，**不是验证后的硬件 ID**；同名档案无法区分实体设备，改名也不会自动迁移记忆。默认 `Default switch` 或新手向导带入的通用上下文使用空设备作用域，只显示和保存个人偏好。
- `ChatMemoryController.recall()` 的领域调用最多取得 10 条适用记录；`AiCopilotActivity` 在发送边界进一步只选最近 5 条完整记录，给 `AiChatRequest` 的上下文上限预留空间。页面保存这次选择的不可变快照，并通过 `showUsed(snapshot)` 展示；编辑记忆不会改写已经发出的请求记录。请求层仍执行既有安全清理。
- 记忆管理窗口继承安全显示要求，保存前保留可编辑草稿，敏感输入禁用实例状态保存和自动填充。管理和召回仅访问本地存储；只有用户点击聊天发送才进入既有厂商请求流程，可能产生服务商 API 费用。

新增确认流程：`用户选择记住 → 编辑内容与范围 → 明确确认 → 领域校验 → 单次加密写入`。

发送流程：`用户点击发送 → 检查记忆开关 → 获取当前范围有效记录 → 选取最多 5 条并冻结快照 → 既有上下文清理 → 用户所选 AI 服务商`。读取异常时当前启用记忆的发送失败关闭，不静默继续使用空记录或旧记录。

下面保留早期版本架构背景；AI 聊天长期记忆以 V0.11.0 的独立确认存储为准，聊天历史仍沿用 V0.9.0 的有界加密实现。

## V0.10.0 问题驱动排障

- `BeginnerTaskCatalog` 定义十个场景和搜索关键词；首页展示真实活动会话状态，专业工具仍复用既有导航入口。
- `BeginnerGuideActivity` 编排界面，`guided.network` 负责单目标、限时、可取消的 DNS/Ping/TCP 检测；`guided.switches` 从用户提供的文本生成离线结论和只读命令草稿，不直接持有终端连接。
- `BeginnerResultRenderer` 将诊断结果转换成中英文解释与下一步。网络结果只代表手机到指定目标的短时观测，离线厂商推断不构成设备身份认证。
- `BeginnerGuideDraftStore` 只在进程内保存有界、脱敏的继续排障草稿。USB 和远程终端保持原有连接与发送路径，返回向导时才读取已有活动会话的最新输出。
- 报告先预览再分享；AI 入口创建新的可编辑草稿，不自动发送；配置快照复用专门页面并要求显式保存，不把截断输出当作完整备份。
- 无新增运行依赖、数据库、公开 API、连接协议或凭据格式迁移。Debug 采用 `.preview` 独立包名，Release 保持生产升级身份。

## V0.9.1 活动终端只读桥接

`TerminalFragment` 与 `RemoteTerminalActivity` 只把已经显示给用户的 USB/SSH/Telnet 文本副本送入 `ActiveTerminalSessionStore`。Store 使用有界 `TerminalTextBuffer`，在写入时清除 ANSI 并脱敏，通过不可变 `ActiveTerminalSnapshot` 向运维中心提供最近仍在线的会话。

该桥接层是进程内单例，不持久化、不持有 Socket/串口对象、不提供 send 回调。`OperationsCenterActivity` 只读取快照并复用 `DeviceFingerprintEngine` 和 `DeviceProfileStore`，所以识别功能不会形成第二套连接层或绕过现有终端安全门禁。

## V0.9.0 AI 对话与运维闭环

- `AiChatProvider` 是自由文本对话接口；`OpenAiCompatibleChatProvider`、`AnthropicChatProvider` 和 `OllamaChatProvider` 复用现有 HTTPS Transport、厂商档案、凭据回调与端点解析，不建立第二套密钥系统。
- `AiChatRequest` 只保留最近 24 条上下文，并保证以用户消息开始、以用户消息结束；Codec 合并连续同角色消息，兼容 Anthropic 与 OpenAI-compatible 请求约束。
- `AiChatJsonCodec` 注入稳定系统安全规则，把设备记忆和终端输出标为不可信数据，并在发送前执行长度限制、敏感字段脱敏和提示注入隔离。
- `FailoverAiChatService` 继承活动厂商优先、最多两次瞬时错误重试和取消不转移的规则；UI 不接触 API Key。
- `AiChatHistoryState`、`AiChatConversation` 与 Codec 是纯 Java 有界领域对象；`EncryptedAiChatHistoryRepository` 把完整历史作为单个 Android Keystore/AES-GCM 记录保存。Android 5.x 只保留内存会话，并通过 fail-closed 凭据库仅允许无密钥 HTTPS Ollama 对话。
- `AiCommandExtractor` 只解析显式 Markdown 围栏中的网络 CLI 白名单命令，再交给 `RuleBasedExecutionGuard` 重新分级；R4 不可从 AI 页面装载。
- `IncidentCollectionEngine`、`GuidedRunbookEngine`、`ConfigurationDriftEngine` 与 `ChangeReadinessValidator` 分别负责只读取证、Expect 执行合同、漂移分级和变更生命周期完整性，不直接持有连接。

关键数据流：`用户消息 → 本地脱敏/裁剪 → 厂商适配与故障转移 → 回复脱敏 → 加密历史 → 命令白名单提取 → 本地风险分级 → 人工装载 → 终端既有门禁`。

## V0.8.0 模型目录与任务界面

- AiProviderCatalog 与 AiProviderPresetCatalog 仍是厂商及默认端点的单一来源；已有 ProviderProfile JSON 结构不变，避免破坏旧配置。
- AiModelCatalogService 负责认证模式、目录地址和响应格式编排；端点解析、JSON 解码与 HTTPS Transport 可独立测试。
- 模型目录只缓存模型标识与获取时间。API Key 继续通过短生命周期字符数组或 Android Keystore 回调进入请求，不写入模型缓存。
- OpenAI-compatible 与 Anthropic 读取 data[].id，Ollama 读取 models[].name；上游不可用时 UI 保留缓存和手工模型输入。
- FeatureButtonFactory 统一主页、功能中心与搜索结果的任务卡片，功能注册表和导航逻辑保持不变。
- NetworkToolsActivity 只编排任务与展示；PortBatchParser 承担端口列表边界，NetworkProbeService 执行单目标受控检查。

## V0.7.0 导航与高级模块

- `navigation` 是可发现功能的单一注册表；UI 只解析描述并交给 `FeatureNavigator`，收藏/最近使用经版本化适配器保存。
- `MainActivity` 保留五个 Fragment 工作区并使用 show/hide，USB 终端单独保留；手机与平板共享导航状态。
- `diagnostics`、`config`、`topology` 只处理证据和计划，不连接设备；Activity 负责输入、脱敏与展示。
- `ai.FailoverAiCopilot` 只暴露脱敏尝试结果；凭据仍通过短生命周期回调进入请求适配器。
- `automation.ControlledBatchExecutor` 依赖执行与审批接口，因此生产连接不能被纯逻辑层意外开启。
- `transfer` 将策略与 HTTP/TFTP 服务分离；`runbook` 分离 JSON 编解码与公钥签名验证。

## V0.6.0 运维模块

V0.6.0 继续将传输、规划和执行明确分离。`device` 负责从终端证据识别平台；`web`、`automation`、`compliance`、`discovery` 只生成有边界的离线计划；`update` 只读访问 GitHub Latest；`session` 与 `config` 只保存各自范围内的私有状态；`transfer` 实现 XMODEM 帧与协商。界面可以把草案带回终端，但既有终端安全接缝仍是唯一命令发送路径。

运维中心不持有凭据，也不创建网络连接。批次规划采用金丝雀优先、逐台确认、首错即停，并刻意不提供执行能力，防止规划页面成为意外的批量自动化后门。

厂商识别必须发现明确的产品/平台证据且置信度至少 80% 才会更新设备档案；含糊的通用提示符保持未识别状态。

## V0.5.0 外观与工作台增量

- `ThemedActivity` 是所有 Activity 的统一入口，在页面创建前应用 `AppAppearanceController` 与语言设置；页面恢复时比较外观快照，保证返回栈页面也使用最新主题。
- `AppSettings`/`AppSettingsStore` 继续作为唯一设置模型，新增 `AppearanceMode`、`AccentTheme` 和 `keepScreenAwake`，旧构造器和旧 SharedPreferences 数据兼容。
- Material 3 DayNight 提供基础设计令牌；四个主题只覆盖主色/容器色，深色资源通过 `values-night` 自动替换，避免页面级硬编码主题。
- `DevicesFragment` 仍保持 USB 列表职责，但列表头作为只读运维摘要与导航入口；设备和变更数据仍由各自 Store 提供。
- `CommandUsageHistory` 是可单元测试的纯 Java 领域对象；`CommandUsageStore` 只负责 Android 持久化，命令库只传入内置 `CommonCommand` 标识。收藏/最近使用先在副本上变更，持久化成功后才替换界面状态。
- 屏幕常亮不属于 Activity 全局外观。USB 与远程终端只在页面可见且连接状态为 `CONNECTED` 时申请窗口常亮，暂停或断开立即清除。

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
