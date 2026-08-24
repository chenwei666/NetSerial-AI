# CHANGELOG

## V0.7.0 - 2026-08-24

- 开发人员：chenwei666
- 发布状态：正式版；147 项测试、Release Lint、APK 对齐、包元数据与生产签名验证通过，实体设备矩阵仍需现场验收

### 新增功能

- 新增主页、连接、终端、工具箱、设置五区导航；手机使用底部导航，`sw600dp` 平板使用 Navigation Rail。
- 新增全局功能注册表、搜索、最多 4 个收藏及最多 6 条最近使用记录。
- 新增 AI 多厂商失败转移：优先当前厂商，每个厂商最多尝试两次，再切换其他已配置厂商，并返回脱敏尝试统计。
- 新增四厂商健康巡检与输出分析、IP/MAC/接口排障链、LLDP/CDP 拓扑解析、脱敏配置备份与 SNMPv3 受限只读计划。
- 新增安全高级工具：Android Keystore 设备凭据别名库并接入 SSH 登录、随机令牌 HTTP/只读 TFTP 临时共享、RSA/SHA-256 签名运行手册验证和命令预览。
- 新增受控批次执行内核：最多 25 个唯一目标、金丝雀/串行顺序、逐目标审批、四阶段命令、失败即停与回退。

### 问题修复

- 修复批量目标解析正则在字符编码转换后可能损坏的问题。
- 修复 Android 16 预测式返回手势不会调用旧 `onBackPressed()` 的兼容性问题。
- 修复 `java.util.Base64` 在 Android 6/7 不可用的问题，改为受测的严格 Base64 解码。
- 修复健康检查 CPU/内存百分比表达式可能截掉首位数字的问题。
- 补充端口排障类型空值校验。
- 修复受控批次在变更/验证适配器抛异常时只停止而未尝试回退的问题，并确保异常详情不进入记录。
- 修复 SSH 凭据别名读取失败时已创建连接执行器未关闭的问题，并补齐未连接 Telnet 对象的关闭路径。
- 修复 TFTP ACK 未校验客户端地址和端口、可能被同网段伪造数据包推进块号的问题。

### 优化内容

- 终端菜单仅保留清屏、AI 诊断、命令库、Web 开通、会话/文件和终端设置；全局功能统一进入五区导航。
- Fragment 使用 show/hide 保留 USB 终端与工作区状态；从非主页返回时先回主页，再退出应用。
- 高级功能采用规划/验证/执行适配器边界，纯分析模块不直接连接交换机；秘密不写日志、不回显、不写运行手册。
- 中英文功能名称、摘要、提示、错误与安全说明同步更新。

### 删除内容

- 未删除旧功能、用户数据格式或历史版本；仅移除菜单中的重复入口。

### 修改文件

- `app/build.gradle`、`app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/chenwei666/netserial/{ai,automation,config,diagnostics,navigation,runbook,topology,transfer}/**`
- `app/src/main/java/de/kai_morich/simple_usb_terminal/{MainActivity,DevicesFragment,TerminalFragment,HomeFragment,FeatureHubFragment,TerminalHubFragment,FeatureNavigator,FeatureSearchDialog,OperationsCenterActivity,AdvancedToolkitActivity,AiCopilotActivity}.java`
- `app/src/main/res/{layout,layout-sw600dp,menu,drawable,values,values-en}/**`
- `app/src/test/java/com/chenwei666/netserial/**`、`README.md`、`README_EN.md`、`docs/**`

### 影响模块

- 主导航、USB/远程终端入口、AI Copilot、命令库、运维中心、配置快照、临时传输、设备凭据、运行手册、平板布局和返回手势。
- 原连接配置、AI 厂商档案、设备档案、记忆、收藏、变更任务和快照格式保持兼容。

### 数据库变更

- 无数据库读取、连接、脚本或表结构变更。
- 功能收藏/最近使用写入独立版本化 SharedPreferences；凭据密文继续使用 Android Keystore + 私有 SharedPreferences。
- 临时文件写入应用 cache；运行手册只在当前页面内存中验证，不持久化私钥、签名或命令包。

### 接口变更

- 新增内部功能注册、诊断/备份/拓扑规划、AI 失败转移、受控批次、签名验证和临时 HTTP/TFTP 服务边界。
- 无公开服务器 API 或数据库 API 破坏性变更。

### 配置变更

- `versionCode` 从 7 升为 8，`versionName` 从 0.6.0 升为 0.7.0。
- `applicationId`、minSdk 21、targetSdk 36、GitHub Latest 更新源与正式签名加载方式保持不变。

### 兼容性说明

- 支持 Android 5.0–16；Android 16 使用 AndroidX `OnBackPressedDispatcher`。
- 已使用与 V0.6.0 相同的仓库外生产证书签名，可覆盖升级并保留应用数据；密钥和口令未进入仓库或日志。
- SNMPv3 是只读计划器而非主动扫描器；受控批次默认无生产连接适配器，不会后台登录设备。

### 升级方式

1. 使用原生产证书构建并核对 applicationId、版本、证书 SHA-256 与 APK SHA-256。
2. 在实验手机覆盖安装，确认五区导航、语言、主题、旧档案与旧凭据可用。
3. 在授权实验网络验证 USB、SSH/Telnet、AI 失败转移、HTTP/TFTP、签名运行手册和四厂商诊断。
4. 安装后在授权实验网络完成现场矩阵，再用于生产变更。

### 已知问题

- 自动化环境未连接真实 Android 手机、USB OTG、交换机、SSH/Telnet/SFTP、真实 AI 付费账号或 SNMPv3 Agent。
- HTTP/TFTP 受 Android 省电、Wi-Fi 隔离和防火墙影响；TFTP 无加密，仅用于短时只读小文件。
- 拓扑解析与健康分析基于采集文本和启发式规则，不能替代厂商网管平台或对应版本官方手册。

### 备注

- `Version/V0.6.0` 与更早目录保持不可变；V0.7.0 是完整独立源码归档。
- AI、运行手册、批次和诊断结果均须由授权工程师复核，应用不自动绕过执行门禁。

## V0.6.0 - 2026-08-24

- 开发人员：chenwei666
- 发布状态：正式版候选；自动化测试通过后生成 Release APK，真实设备与交换机现场验收仍为发布门禁

### 新增功能

- 新增 H3C Comware、Huawei VRP、Cisco IOS、Ruijie RGOS 离线指纹识别；仅高置信度结果自动更新设备厂商。
- 新增交换机 Web 开通向导，按厂商/平台生成 HTTPS、可选 HTTP、本地管理员及验证/回退命令；密码不持久化，预览和证据使用脱敏命令。
- 新增 GitHub Releases Latest 每日与手动更新检测，版本按语义版本比较，Release/APK URL 限制为 GitHub HTTPS。
- 新增 AI 一键故障诊断入口，自动带入已清洗与脱敏的终端尾部输出，要求原因、只读检查、修复、验证和回退四类结果。
- 新增运维中心：变更前健康检查、接口诊断、VLAN 审计、LLDP/CDP 邻居发现、安全基线采集、配置合规启发式初筛与金丝雀批次规划。
- 新增多会话工作区，最多保存 30 个不含密码/私钥的 SSH/Telnet 连接档案，并以 Android 独立任务打开并行会话。
- 新增配置快照中心，最多保存 20 个应用私有、脱敏、规范化、带 SHA-256 的快照并直接进入 Diff。
- 新增自定义命令包，最多 200 条；拒绝敏感凭据、控制字符、多行脚本和重复命令。
- 新增 USB XMODEM-128 发送，支持 CRC/校验和、ACK/NAK、10 次重试、30 秒协商超时、进度、取消、16 MiB 上限及发送前 SHA-256。

### 问题修复

- 修复命令模糊匹配可能把其他首关键字中的片段错误当作单词前缀的问题；单词补全继续严格按首部匹配。
- 修复远程会话档案打开后协议、地址、端口和用户名未自动带入的问题。
- 修复快照中心无法把历史快照与当前内容直接传递到配置差异页面的问题。

### 优化内容

- `CompletionRequest` 增加可选平台和脱敏上下文，旧构造方法保持兼容；补全改为精确/前缀/多词顺序匹配分级。
- 所有新增规划功能不直接连接或批量执行设备；命令必须回到 USB/SSH/Telnet 终端并经过既有 R0-R4、目标和变更窗口校验。
- Web 密码、AI 上下文、配置快照、终端证据和 XMODEM 文件均设置明确的大小、生命周期或脱敏边界。
- 首页与菜单增加运维中心、多会话和配置快照入口；中英文资源同步。

### 删除内容

- 未删除 V0.5.0 的任何功能、历史版本、用户数据格式或兼容接口。

### 修改文件

- `app/build.gradle`、`scripts/build.ps1`、`app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/chenwei666/netserial/{automation,compliance,completion,commands,config,device,discovery,session,transfer,update,web}/**`
- `app/src/main/java/de/kai_morich/simple_usb_terminal/{AiCopilotActivity,AppSettingsActivity,ConfigDiffActivity,ConfigSnapshotCenterActivity,DevicesFragment,MainActivity,OperationsCenterActivity,RemoteTerminalActivity,SessionWorkspaceActivity,TerminalFragment,UpdateCheckCoordinator,WebAccessWizardDialog}.java`
- `app/src/main/res/{layout,menu,values,values-en}/**`
- `app/src/test/java/com/chenwei666/netserial/**`、`README.md`、`README_EN.md`、`docs/**`

### 影响模块

- USB Console、SSH/Telnet、多会话、命令库与补全、AI Copilot、设备档案、配置 Diff/快照、应用设置、更新检测、运维规划和构建发布。
- 现有连接协议和命令发送入口保持不变；新增功能复用已有安全检查与证据链。

### 数据库变更

- 无数据库连接、查询、脚本或表结构变更。
- 新增私有 `SharedPreferences`：`remote_session_profiles_v1`、`custom_command_pack_v1`、更新检查状态；不保存密码、私钥、API Key 或终端原文。
- 新增应用私有配置快照文件，写入前执行 ANSI 清洗、敏感信息脱敏和规范化，并设置数量与总大小上限。

### 接口变更

- `DeviceProfile` 新增兼容方法 `withVendor`；`CompletionRequest` 新增重载构造器。
- 新增 `DeviceFingerprintEngine`、`WebAccessPlanFactory`、`GitHubUpdateChecker`、`SafePlaybookEngine`、`ConfigComplianceEngine`、`BatchTaskPlanner`、`XmodemSender` 等内部边界。
- 无服务器端、数据库或公开网络 API 破坏性变更。

### 配置变更

- `versionCode` 从 6 升为 7，`versionName` 从 0.5.0 升为 0.6.0。
- 更新源固定为 `chenwei666/NetSerial-AI` 的 GitHub Latest；自动检查默认每日一次，用户可关闭。
- 构建镜像目录改为 `C:\tmp\NetSerial-v060-build`；生产签名材料继续只从仓库外本机安全位置读取。

### 兼容性说明

- 保持 `applicationId=com.chenwei666.netserial`、minSdk 21、targetSdk 36，可使用同一生产证书覆盖升级 V0.5.0。
- 原有 AI 配置、Keystore 凭据、设备档案、记忆、设置、收藏和变更数据格式继续保留；新增存储使用独立命名空间。
- 厂商识别与合规分析是离线启发式辅助，不能替代型号/版本对应的官方命令手册和人工审核。

### 升级方式

1. 从可信 Release 下载 V0.6.0 APK 并核对 SHA-256。
2. 使用与 V0.5.0 相同的生产证书覆盖安装；首次打开检查语言、主题、更新检测与设备厂商。
3. 先在实验交换机验证自动识别、Web HTTPS 命令、AI 诊断、SSH/Telnet 和 XMODEM，再进入生产。
4. 生产目标保留“受保护设备”，建立匹配维护窗口的变更任务后再执行 R3 操作。

### 已知问题

- 自动化环境未连接真实 Android 手机、USB 串口芯片、四厂商交换机或真实 AI 付费账号；协议与 UI 需要现场矩阵验收。
- Web 命令会受型号、软件分支、证书和 Web 软件包差异影响；必须预览并对照设备官方文档。
- 设备发现当前通过 LLDP/CDP 厂商 CLI 采集，不包含完整 SNMPv3 网络扫描器；批次模块只生成计划，不自动登录或执行。
- XMODEM 支持 128 字节块发送，不包含 YMODEM 文件名/批量协议；固件升级语义不自动化。

### 备注

- V0.5.0 与更早历史版本保持不可变；V0.6.0 是完整独立版本目录。
- AI、合规、回退、Web 和命令模板均为待授权工程师审核的草案，不构成自动执行许可。

## V0.5.0 - 2026-08-21

- 开发人员：chenwei666
- 发布状态：正式版候选

### 新增功能

- 新增跟随系统、固定浅色、固定深色三种显示模式。
- 新增海洋蓝、翡翠绿、科技紫、日落橙四套主题，并为深浅模式分别配置对比度。
- 新增首页运维工作台，集中展示当前设备档案、环境和进行中的变更任务。
- 新增 SSH/Telnet、AI、命令库、网络工具、变更任务、设置六个首页快捷入口。
- 新增内置交换机命令收藏、最近使用、范围筛选和清空最近记录。
- 新增终端操作期间保持屏幕常亮开关。
- 新增原创应用图标，以终端提示符、四口交换机和连接节点表达串口、网络运维与智能辅助定位，并生成 Android mdpi 至 xxxhdpi 全套资源。

### 问题修复

- 修复原首页和 USB 设备列表层级弱、点击区域不统一、深色模式不可用的问题。
- 修复 USB 设备、驱动缺失、端口和波特率弹窗仍使用硬编码英文的问题。
- 修复各 Activity 分散应用语言设置、未来主题切换容易出现页面不一致的问题，改由统一基类处理。
- 修复旧构建镜像 Gradle 锁文件可能阻塞新版本构建的问题，V0.5.0 使用独立 ASCII 构建目录。
- 修复无 USB 设备时 ListView 空状态可能隐藏整个运维工作台的问题，改为设备列表内占位卡片。
- 修复主题变更后返回栈页面仍保留旧主题的问题，页面恢复时会检测外观版本并安全重建。
- 修复常亮标志错误覆盖所有页面的问题，仅在可见且已连接的 USB/SSH/Telnet 终端会话启用。
- 修复命令收藏或最近记录持久化失败后仍显示成功并保留内存假状态的问题，改为候选副本原子提交。
- 修复远程终端输出区未适配深浅模式，以及首页设备环境显示内部枚举名称的问题。
- 修复设备厂商选择“通用”时首页摘要误显示为 H3C 的本地化映射问题。

### 优化内容

- 全应用迁移到 Material 3 DayNight 主题，统一状态栏、导航栏、背景、表面、文字和控件样式。
- 设置页按外观、终端、连接与安全分组，使用卡片与 Material 输入控件。
- 命令库改为现代卡片筛选区，并在收藏命令前显示星标。
- 终端收发和状态颜色增加深色模式专用配色。
- 命令使用记录仅保存内置目录标识，不采集终端输入、账号、密码、Token 或设备配置。
- 重写 GitHub 中英文项目定位，新增 V0.1.0 至 V0.5.0 的完整版本演进、发布状态、制品可信边界和升级关系说明。

### 删除内容

- 未删除 V0.4.0 已有功能、历史版本或用户数据。

### 修改文件

- `app/build.gradle`、`app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/chenwei666/netserial/settings/*`
- `app/src/main/java/com/chenwei666/netserial/commands/CommandUsage*`
- `app/src/main/java/de/kai_morich/simple_usb_terminal/{ThemedActivity,AppAppearanceController,DevicesFragment,AppSettingsActivity,CommandLibraryActivity}.java`
- 所有 Activity 的统一外观基类接入
- `app/src/main/res/{layout,values,values-en,values-night}`
- `app/src/main/res/mipmap-*/ic_launcher.png`、`design/app-icon-{master,512}.png`
- `app/src/test/java/com/chenwei666/netserial/{settings,commands}`
- `README.md`、`README_EN.md`、`docs/*`、`scripts/build.ps1`
- 根目录 `VERSION_HISTORY.md`、`VERSION_HISTORY_EN.md`

### 影响模块

- 应用启动、所有 Activity 外观、首页导航、USB 设备列表、应用设置、命令库、终端显示、构建与发布。
- USB/SSH/Telnet 连接协议、AI 请求、变更安全门禁和配置 Diff 核心逻辑不变。

### 数据库变更

- 无数据库连接、查询或结构变更。
- `app_settings_v1` 新增 `appearance_mode`、`accent_theme`、`keep_screen_awake`，旧安装缺失字段时安全使用默认值。
- 新增 `command_usage_v1`，只保存内置命令标识的收藏集合和最多 30 条最近使用顺序。

### 接口变更

- `AppSettings` 新增外观模式、主题和防休眠字段；保留旧构造器兼容现有调用与测试。
- 新增 `CommandUsageHistory`、`CommandUsageStore`、`AppAppearanceController` 和 `ThemedActivity` 内部接口。
- 无对外网络 API、AI 厂商协议或数据库 API 变更。

### 配置变更

- `versionCode` 由 5 升为 6，`versionName` 由 0.4.0 升为 0.5.0。
- 默认显示模式为跟随系统，默认主题为海洋蓝；屏幕常亮默认关闭，由用户按需启用。
- 构建镜像目录调整为 `C:\tmp\NetSerial-v050-build`；签名证书和口令位置不变。

### 兼容性说明

- 最低 Android API 21、targetSdk 36、applicationId 保持不变。
- 可使用同一生产证书覆盖升级 V0.4.0，现有语言、连接、安全、设备档案、AI 和变更数据继续保留。
- 新设置字段和命令历史使用独立默认值，不要求手工迁移。

### 升级方式

1. 备份需要保留的非敏感设备档案与变更记录。
2. 核对 V0.5.0 APK 的 SHA-256，并使用与 V0.4.0 相同的生产签名覆盖安装。
3. 首次打开“应用设置”，确认显示模式、主题、语言和常亮策略。
4. 在授权测试设备验证 USB、SSH/Telnet、命令收藏、AI 草稿和深浅模式后再用于生产。

### 已知问题

- 自动化环境没有连接真实 Android 手机、USB 串口芯片或各厂商交换机，现场兼容性仍需真机验收。
- 未执行真实 AI 账户调用，也未在厂商定制 Android 的省电策略下验证长期常亮与后台行为。
- 主题与布局已通过资源编译和 Lint；不同屏幕尺寸、字体缩放和无障碍服务仍需真机视觉验收。

### 备注

- 所有 AI 结果仍是待审核草稿；主题与快捷入口不会放宽现有命令执行安全门禁。
- `Version/V0.4.0` 保持不变，`Version/V0.5.0` 是完整独立版本归档。

## V0.4.0 - 2026-08-21

- 开发人员：chenwei666
- 发布状态：正式版候选

### 新增功能

- 新增变更任务模块：工单、站点、设备、工程师、维护窗口、目标、前置检查、验证、回滚和事件时间线。
- 新增 Markdown/PDF 脱敏证据导出。
- 新增设备环境、受保护设备、管理地址和常驻彩色目标条。
- 新增受保护设备 R3/R4 维护窗口门禁与远程目标匹配。
- 新增配置规范化、SHA-256、逐行差异、回滚草稿和 Markdown 导出。
- 新增 IPv4/IPv6 CIDR、DNS、Ping、Traceroute、TCP、路径 MTU、MAC/OUI、常用端口和标识提取工具。
- SSH 新增 keyboard-interactive、会话级私钥、密码跳板机、Keepalive 和 SFTP 上传下载。
- AI 计划新增预检查、变更、验证、回滚阶段和本地完整性检查。
- 设置新增 SSH Keepalive 与网络探测超时，并补齐新增界面的中英文资源。

### 问题修复

- 修复多行粘贴只检查整体文本可能被低风险首行掩盖的问题，改为逐行取最高风险。
- 修复 SSH 私钥认证仍回退到 keyboard-interactive 的语义混淆。
- 修复 SFTP 控件在 SSH 未连接时仍可点击的问题。
- 修复 Android API 21 不支持部分日期、Map 和 Process API 的兼容性问题。
- 扩充网络设备 username/enable/SNMP/local-user 凭据语法脱敏。

### 优化内容

- Ping 优先使用 Android ICMP 工具，不可用时回退到 Java reachability。
- 配置差异忽略换行、尾随空格和部分动态时间/计数器噪声。
- SSH 私钥、口令、跳板口令使用临时副本并在使用结束后覆写清理。
- 网络系统工具使用独立参数列表、输入白名单、超时和输出上限。
- AI 设备上下文加入变更任务信息，同时按不可信文本清理提示注入特征。

### 删除内容

- 未删除 V0.3.1 既有功能。

### 修改文件

- `app/build.gradle`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/chenwei666/netserial/{change,config,network,remote,safety,terminal,ai,device,settings}`
- `app/src/main/java/de/kai_morich/simple_usb_terminal/*Activity.java` 与 `TerminalFragment.java`
- `app/src/main/res/layout/*`、`values/strings.xml`、`values-en/strings.xml`、菜单资源
- `app/src/test/java/com/chenwei666/netserial/*`
- `README.md`、`README_EN.md`、`docs/*`

### 影响模块

- USB Console、SSH、Telnet、SFTP、命令安全门禁、AI、设备档案、应用设置、变更留证、网络工具和配置对比。

### 数据库变更

- 无数据库。
- SharedPreferences 增加变更任务、设备环境/保护/管理地址、Keepalive 和探测超时字段；设备备份 schema 升级到 3，并兼容 schema 1/2。

### 接口变更

- `DeviceProfile`、`AppSettings`、`AiDraftStep`、`EvaluatedCommandStep` 增加字段并保留旧构造器兼容。
- `SshRemoteConnection` 增加 `SshConnectionOptions`、`SshCredentials` 和 SFTP 接口。

### 配置变更

- `versionCode` 由 4 升为 5，`versionName` 由 0.3.1 升为 0.4.0。
- 新增 SSH Keepalive 和网络探测超时设置；不新增硬编码密钥或默认凭据。

### 兼容性说明

- 最低 Android API 21 保持不变。
- 保留 V0.3.1 的 USB、SSH 密码、Telnet、命令库、Tab/Ctrl、AI 服务商和设备记忆行为。
- 旧设备档案和旧设置会使用默认值迁移，新版备份仍可导入 schema 1/2。

### 升级方式

1. 备份需要保留的设备档案与 AI 配置。
2. 使用相同签名证书的 V0.4.0 APK 覆盖安装。
3. 首次启动后检查语言、Keepalive、探测超时、设备环境和保护状态。
4. 在授权测试设备上验证 USB、SSH/Telnet、SFTP 与变更导出后再用于生产。

### 已知问题

- 尚未在本次自动化环境连接真实 Android/USB/交换机、真实 AI 账户、真实私钥/跳板机/SFTP 服务。
- Android 厂商对 Ping、Traceroute、DF Ping 的提供情况不同，路径 MTU 可能不可用。
- 内置 OUI 为小型离线目录，不能替代完整 IEEE 数据库。
- PDF 内容已通过编译与逻辑检查，最终分页和中文字体效果仍需真机视觉验收。

### 备注

- 所有 AI 结果和回滚内容均为草稿；执行责任仍由具备授权的网络工程师承担。
- V0.3.1 历史目录保持不变，V0.4.0 为独立完整版本归档。

## V0.3.1 - 2026-08-21

- 开发人员：chenwei666
- 版本状态：正式生产签名 Release

### 新增功能

- 新增独立生产签名初始化脚本，以 RSA 4096、SHA256withRSA、PKCS12 和 10000 天有效期创建长期 Android 更新证书。
- 新增当前 Windows 用户级 DPAPI 口令保护；口令仅通过本机遮罩输入框采集，不写入仓库、构建日志或明文配置。
- 新增 `-Release` 发布门禁，一次执行完整业务单元测试、Release Lint、Release 编译和 V1/V2/V3 签名。
- 新增中英文正式发布、签名备份、恢复、证书指纹和升级约束文档。

### 问题修复

- 修复正式版本缺少生产签名配置、只能生成 Debug APK 的发布阻断问题。
- 修复 PowerShell 安全模块不可加载时无法初始化发布口令的问题，改为直接加载 .NET System.Security 并调用 DPAPI。
- 修复 Android Gradle 插件未生成 `testReleaseUnitTest` 任务导致发布脚本中断的问题；发布门禁改用相同业务源码的完整 `testDebugUnitTest`，并继续单独执行 `lintRelease` 与 `assembleRelease`。
- 签名初始化采用临时文件后原子落盘，失败时自动清理临时材料，避免半成品密钥阻断重试。

### 优化内容

- 发布构建必须同时提供密钥库、别名和口令环境变量；任一缺失时 Gradle 立即拒绝 Release 任务。
- 签名目录移除继承权限并仅授予当前 Windows 用户完全控制。
- `.gitignore` 增加 PKCS12、DPAPI 和签名元数据规则，避免敏感发布材料误提交。

### 删除内容

- 未删除任何已有功能、接口、历史版本或用户数据。

### 修改文件

- `app/build.gradle`
- `scripts/build.ps1`
- `scripts/initialize-release-signing.ps1`
- `.gitignore`
- `README.md`、`README_EN.md`
- `docs/RELEASE.md`、`docs/TEST_REPORT.md`

### 影响模块

- 仅影响构建、签名、版本标识、发布文档和 GitHub Release 流程。
- USB、SSH、Telnet、AI、命令补全、设备记忆、命令库和设置业务逻辑保持 V0.3.0 行为。

### 数据库变更

- 无数据库连接、查询、脚本或结构变更。

### 接口变更

- 无 Android 应用运行时公共接口变更。
- 构建脚本新增 `-Release` 开关和四个仅存在于构建进程的 `NETSERIAL_RELEASE_*` 环境变量。

### 配置变更

- `versionName` 从 `0.3.0` 更新为 `0.3.1`，`versionCode` 从 `3` 更新为 `4`。
- Release 使用专用长期证书；密钥库和受保护口令保存在仓库外的当前用户本机目录。
- 保持 `applicationId=com.chenwei666.netserial`、`minSdk=21`、`targetSdk=36`。

### 兼容性说明

- V0.3.1 与 V0.3.0 的应用 ID 一致，但 V0.3.0 仅为 Debug 签名，因此 Android 不允许直接覆盖安装；需先卸载 Debug 版，之后所有正式升级必须使用同一 V0.3.1 生产证书。
- Android 5.0+、现有应用数据格式和网络功能保持兼容。

### 升级方式

1. 如安装的是 V0.3.0 Debug APK，先备份非敏感配置并卸载 Debug 版。
2. 从 GitHub Release 下载 `NetSerial-AI-v0.3.1-release.apk`，核对 SHA-256 后安装。
3. 后续版本必须用同一生产密钥签名，方可覆盖升级并保留应用数据。

### 已知问题

- 尚未在真实 Android 手机上完成安装、卸载 Debug 后迁移、语言切换和后台恢复验收。
- 尚未使用真实 USB 串口芯片、四厂商交换机和用户自有 AI 测试账户完成现场矩阵。
- SSH 当前仅支持密码认证；Telnet 仍是明文协议并默认关闭。

### 备注

- 生产密钥不在 Git 仓库内。`.p12` 与口令必须分开离线备份；丢失任一项将无法发布可覆盖升级的后续版本。
- 自动化验收结果、APK 哈希和证书指纹见 `docs/TEST_REPORT.md`。

## V0.3.0 - 2026-08-21

- 开发人员：chenwei666
- 版本状态：功能开发检查点；Debug APK；等待真实 Android 手机、SSH/Telnet 交换机和生产签名验收

### 新增功能

- 新增独立 `RemoteConnection` 远程连接边界、状态模型、参数校验和连接生命周期。
- 新增 SSH 交互式 Shell，支持用户名/密码、可配置端口/超时/字符编码、真实终端控制键、长会话受控缓冲。
- 新增 SSH 首次主机密钥人工核验、应用私有 known-hosts 存储和已知密钥变化阻断；设置页支持经确认后清空全部已知主机。
- 新增 Telnet Socket 终端和跨数据块 IAC 解析；Telnet 默认关闭，启用和每次连接均要求显式风险确认。
- 新增 H3C Comware、华为 VRP、Cisco IOS、锐捷 RGOS 四厂商十分类命令库，包含只读、配置和高风险标记。
- 新增命令库按厂商、分类和关键词搜索；设备列表用于复制，USB/远程终端用于填入草稿。
- 新增远程终端 AI Copilot 入口、真实 TAB/ESC/Ctrl+C/方向键/问号快捷键；AI 和命令库均不自动执行。
- 新增应用设置：跟随系统/简体中文/English、Telnet 开关、2–60 秒连接超时、终端字号、UTF-8/GBK/ISO-8859-1。
- 新增 Android 13+ 应用语言声明和运行时 AppCompat 语言切换。
- 新增 USB 与远程终端统一发送保护：R3 二次确认，R4 输入 `EXECUTE` 后才允许发送。

### 问题修复

- 修复离线补全在命令集合扩大后“先截断再排序”导致通用前缀丢失的问题；改为完整匹配、去重、排序后再按上限返回。
- 修复命令目录使用 API 24 `Comparator.comparing` 会阻断 Android 5.x Lint 兼容门禁的问题；改为 API 21 可用比较逻辑。
- 保持原 USB 串口连接、HEX、换行、控制线、流控制、后台服务、AI 配置和设备记忆行为，不把网络连接生命周期并入 USB 服务。

### 优化内容

- SSH 密码框禁用自动填充和实例状态保存，连接启动后立即清空；远程终端启用安全窗口。
- SSH/Telnet 异常只向 UI 返回安全错误信息，不记录密码、数据包或完整凭据。
- 远程字符解码保留跨数据块的不完整多字节序列，减少 UTF-8/GBK 分块乱码。
- 命令库成为离线补全的单一命令数据源，减少四厂商命令重复维护。
- 文档增加中文和 English 的 SSH、Telnet、命令库、设置、构建、安全边界及验收说明。

### 删除内容

- 未删除任何历史版本或已有业务功能。
- 未加入真实账号、密码、API Key、Token、现场 IP 或交换机配置。

### 主要修改文件

- `app/src/main/java/com/chenwei666/netserial/remote/**`
- `app/src/main/java/com/chenwei666/netserial/commands/**`
- `app/src/main/java/com/chenwei666/netserial/settings/**`
- `app/src/main/java/com/chenwei666/netserial/completion/OfflineCompletionEngine.java`
- `app/src/main/java/de/kai_morich/simple_usb_terminal/{RemoteTerminalActivity,CommandLibraryActivity,AppSettingsActivity,CommandSendConfirmation}.java`
- `app/src/main/java/de/kai_morich/simple_usb_terminal/{TerminalFragment,DevicesFragment,MainActivity}.java`
- `app/src/main/res/{layout,menu,values,values-en,xml}/**`
- `app/src/test/java/com/chenwei666/netserial/{remote,commands,settings}/**`
- `README.md`、`README_EN.md`、`docs/**`、`gradle/libs.versions.toml`

### 影响模块

- USB 和网络终端的命令输入、快捷键、风险确认和字号设置。
- SSH/Telnet 连接、安全状态、字符解码和输出生命周期。
- 离线命令搜索和 TAB 前置草稿补全。
- 应用语言、网络兼容和安全设置。

### 数据库变更

- 无数据库连接、数据库读取、数据库脚本或表结构变更。
- 新增私有 SharedPreferences `app_settings_v1`，只保存语言、Telnet 开关、超时、字号和编码。
- 新增应用私有文件 `ssh_known_hosts`，只保存 SSH 主机公钥记录，不保存用户名或密码。

### 接口变更

- 新增 `RemoteConnection`、`RemoteConnectionConfig`、`RemoteConnectionListener`、`RemoteProtocol` 和 `RemoteConnectionState`。
- 新增 `SshRemoteConnection`、`TelnetRemoteConnection`、`TelnetProtocolCodec` 和流式字符解码器。
- 新增 `CommonCommandCatalog`、`CommonCommand` 和 `CommandCategory`。
- `OfflineCompletionEngine` 的公共接口保持不变，内部数据源改为分类命令目录。
- 现有 AI、USB、设备档案和记忆公共接口保持兼容。

### 配置变更

- `versionName` 从 `0.2.0` 更新为 `0.3.0`，`versionCode` 从 `2` 更新为 `3`。
- 新增 `com.github.mwiede:jsch:2.28.7`，版本从 Maven Central 正式元数据核验。
- 保持 `minSdk=21`、`targetSdk=36`、Android 备份关闭和应用 HTTP 明文关闭。

### 兼容性说明

- Android 5.0+ 保持支持；Android 6.0 以下仍不提供 AI Key 明文降级存储。
- V0.2.0 的 AI 配置、凭据、设备档案和记忆命名空间不变，可原位升级。
- Telnet 是显式例外的原始 Socket 协议，不放开 AI 或其他 HTTP 明文访问。
- SSH 当前仅提供密码认证；私钥导入需要独立的密钥生命周期设计，未以不安全方式临时加入。

### 升级方式

1. 备份 V0.2.0 APK 和必要的现场记录，不导出真实凭据。
2. 构建或安装 V0.3.0 Debug APK；正式环境需先配置生产签名。
3. 在“应用设置”选择语言、字号、编码和超时；Telnet 保持关闭，除非确有旧设备需要。
4. 在非生产交换机核验 SSH 指纹、重复连接、密钥变化阻断、TAB、命令库和风险确认。
5. 通过现场变更流程后再在生产环境使用；命令模板必须根据型号和版本复核。

### 已知问题

- 尚未连接真实 Android 手机或真实 H3C/华为/Cisco/锐捷交换机，SSH/Telnet/USB/语言切换属于待外部验收项。
- Telnet 解析器对不支持的选项采取保守拒绝；少数依赖特定 Telnet 子协商的旧设备可能需要专用适配。
- SSH 仅支持密码认证；未加入私钥、跳板机、代理、端口转发和多会话标签页。
- 命令库是安全起点，不是所有型号和版本的完整厂商命令手册。
- 当前 APK 为 Debug 签名，不能替代正式 Release 签名、升级覆盖和应用商店验证。

### 备注

- 完整使用和安全说明见 `README.md`、`README_EN.md`、`docs/REMOTE_CONNECTIONS.md` 和 `docs/SECURITY.md`。
- 上游来源和许可证见 `UPSTREAM.md`、`LICENSE.txt`。

## V0.2.0 - 2026-08-21

- 开发人员：chenwei666
- 版本状态：功能开发检查点；Debug APK，等待真实硬件和真实厂商 API 验收

### 新增功能

- 新增终端内 AI Copilot，可输入自然语言目标或待检查命令，选择是否附带最近脱敏终端上下文。
- 新增 OpenAI、Gemini、DeepSeek、Qwen、Kimi、自定义 OpenAI-compatible、Claude/Anthropic Messages 和 Ollama HTTPS 适配器。
- 新增最多 32 个 AI 配置的创建、编辑、删除、切换、官方预设和显式连接测试。
- 新增 Android Keystore AES-GCM 凭据保险库；API Key 不进入配置 JSON、备份、日志和记忆。
- 新增 H3C Comware、Huawei VRP、Cisco IOS、Ruijie RGOS 四厂商、三种 CLI 模式的离线常用命令候选。
- 新增 ESC、TAB、Ctrl+C、Ctrl+Z、方向键、退格、删除、问号和管道符终端控制键。
- 新增 200,000 字符受控终端缓冲、ANSI/VT 清洗和敏感信息脱敏。
- 新增设备名称、厂商、CLI 模式和波特率档案。
- 新增供应商无关的本地结构化 AI 记忆：设备作用域、来源、可信度、创建/过期时间、删除、最多 500 条。
- 新增安全 JSON 导入导出，仅包含设备档案与已验证记忆，不包含任何 API Key。
- 新增脱敏终端会话日志导出，导出前执行 ANSI 清洗和凭据遮蔽。

### 问题修复

- 修复普通发送逻辑会给 TAB 附加 CR/LF 的问题；专用 TAB 始终发送单字节 `0x09`。
- 修复无限增长的终端 TextView 可能造成长会话内存压力的问题。
- 修复切换供应商或 Endpoint 时旧凭据可能误发到新目标的风险：必须重新输入密钥并轮换别名。
- 修复 AI 声明低风险时可能掩盖本地高风险判定的问题；最终风险只能升高不能降低。
- 修复 API 21-23 不支持 `Collection.removeIf`/`List.sort` 的兼容性问题。

### 优化内容

- AI 输出统一限制为 1-20 条单行结构化命令，禁止控制字符、超长正文和自由文本直接进入串口路径。
- R1 只读、R2 配置、R3 高风险、R4 极高风险规则扩展到查询、VLAN/接口、AAA/路由/停口和重启/擦除等类别。
- R4 命令在 Copilot UI 中禁止载入；其他命令也只载入编辑框，发送仍是独立人工动作。
- 最近终端上下文默认关闭，启用后也只发送 ANSI 清洗、敏感信息脱敏后的最多 12,000 字符。
- 本地记忆只允许显式写入，默认 180 天过期；疑似密码、Token、API Key、私钥和 community 内容被拒绝。
- 保留原有 USB 串口、HEX、换行、流控、控制线、后台服务和通知行为。

### 删除内容

- 无已有业务功能删除。
- 未加入任何真实 API Key、Token、账号、密码或设备配置。

### 主要修改文件

- `app/src/main/java/com/chenwei666/netserial/ai/**`
- `app/src/main/java/com/chenwei666/netserial/completion/**`
- `app/src/main/java/com/chenwei666/netserial/device/**`
- `app/src/main/java/com/chenwei666/netserial/memory/**`
- `app/src/main/java/com/chenwei666/netserial/safety/**`
- `app/src/main/java/com/chenwei666/netserial/terminal/**`
- `app/src/main/java/de/kai_morich/simple_usb_terminal/{TerminalFragment,AiProviderSettingsActivity,AiCopilotActivity,DeviceMemoryActivity}.java`
- `app/src/main/res/layout/**`、`values/**`、`values-en/**`、`menu/**`
- `app/src/test/java/com/chenwei666/netserial/**`

### 影响模块

- USB 串口终端输入、输出和长会话显示。
- 离线命令补全、设备上下文和本地安全策略。
- AI 供应商配置、HTTP 传输、结构化响应解析和凭据生命周期。
- 本地记忆和安全备份。

### 数据库变更

- 无数据库连接或数据库脚本变更。
- 新增私有 SharedPreferences 文档：`ai_provider_profiles_v1`、`ai_memory_v1`、`device_profile_v1`；均为版本化/独立命名空间。

### 接口变更

- 新增 `AiProviderFactory`、`AnthropicProvider`、`OllamaProvider` 和原生凭据头模式。
- 扩展 `TerminalControlEncoder` 支持完整终端控制键。
- 扩展 `OfflineCompletionEngine` 支持四厂商常用命令。
- 新增 `MemoryVault`、`DeviceProfileStore` 和安全备份入口。
- 现有 USB 串口及公开 AI/补全/安全接口保持兼容。

### 配置变更

- 保持 `applicationId=com.chenwei666.netserial`、`versionName=0.2.0`、`versionCode=2`。
- 保持 `minSdk=21`、`targetSdk=36`、Android 备份关闭和明文 HTTP 关闭。
- Android 5.x 可使用串口和离线功能，但 AI Key 存储仍要求 Android 6.0 以上，不进行明文降级。

### 兼容性说明

- 旧版本没有设备档案或记忆时自动使用 H3C、用户视图、9600 的默认档案。
- AI 配置、记忆和设备档案相互独立，任一文档损坏不会自动覆盖其他文档。
- Ollama 仅支持 HTTPS 反向代理；不为局域网模型放开 App 全局明文 HTTP。

### 升级方式

1. 备份旧 APK 和必要的现场记录。
2. 构建或安装 V0.2.0 Debug APK。
3. 在“设备档案与 AI 记忆”中设置厂商与 CLI 模式。
4. 在“AI 设置”中创建并激活供应商配置；Key 必须由用户在本机重新输入。
5. 先在非生产交换机上验证 TAB、常用命令候选和 AI 只读命令，再进入生产现场。

### 已知问题

- 尚未在真实 Android 手机、USB 串口芯片和 H3C/华为/Cisco/锐捷交换机上完成矩阵测试。
- 未提供真实 API Key，未做付费联网验收；厂商可能调整模型名和协议限制。
- 当前 APK 为 Debug 签名，不能替代正式 Release 签名和上架验证。
- 命令包是常用离线集合，不是所有型号、版本和特性的完整厂商命令手册。
- 尚未实现多 USB 会话、SSH、XMODEM、知识库全文索引和自动化脚本执行；这些高复杂度模块仍按完整路线图继续演进。

### 备注

- 完整方案见仓库根目录 `FULL_AI_FEATURE_COMPLETION_PLAN.md`。
- 上游来源与 MIT 许可见 `UPSTREAM.md` 和 `LICENSE.txt`。

## V0.1.0 - 2026-08-21

- 建立独立应用 ID、可信 USB 串口基线、真实 TAB、最小补全和 R0-R4 安全接口。
- 增加中英文资源、ASCII 镜像构建脚本和独立 `Version/V0.1.0` 归档。
- 保留上游 SimpleUsbTerminal MIT 许可和来源记录。
