# Project Changelog

## V0.13.0 — 2026-10-14 · AI 自动任务与本地 Web 凭据

- 新增与当前 USB/SSH/Telnet 会话绑定的 AI 自动任务入口：必要参数追问、任务范围内常规操作自动执行、高风险/未知风险确认、逐条回显、分项复核和取消。
- 新增本地 Web 管理模板与专用密码输入；口令不进入 AI 请求，敏感配置后的回显屏蔽至重连，不能把 CLI 返回当作 Web 访问/登录成功。
- 保留确认式记忆，增加自动任务页记忆开关和管理入口。用户已授权正式发布，生产包构建与原签名核验完成；GitHub 发布及下载验证状态由本轮发布记录补齐。
- 435 项 JVM 测试通过，Release Lint 0 错误 / 222 警告；正式包 V1/V2/V3 签名有效，V0.12.0→V0.13.0 覆盖升级保留 firstInstallTime，冷启动通过。真实 AI 服务、物理交换机及 OEM 环境未联调，自动任务仍为受限新功能。

## V0.12.0 — 2026-10-14 · 免 Root 工具箱与确认式 AI 记忆

- 维护者：chenwei666。独立源码归档：`Version/V0.12.0`；生产 versionCode 14 / versionName 0.12.0，保留正式包名、既有生产证书、minSdk 21、targetSdk/compileSdk 36。
- 发布状态：[PR #16](https://github.com/chenwei666/NetSerial-AI/pull/16) 已合并，提交为 `185f995e0ef3c667a1a7421f11cf33b671d5db96`；`v0.12.0` 标签精确指向同一提交。[V0.12.0 Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.12.0) 已发布，GitHub Latest API 返回 v0.12.0，draft/prerelease 均为 false。线上 APK 与 `SHA256SUMS.txt` 已重新下载核验一致。

### 功能与修复

- 16 个常用工具：基础体检、Ping、TCP、Trace、MTU、地址摘要、DNS、HTTP/HTTPS、TLS、本机网络、受限局域网发现、WOL、CIDR、MAC/OUI、端口速查与标识提取。
- 手机/平板自适应布局、用途分类与搜索、按需输入、数值优先、Ping 趋势、折叠原始输出和取消。Ping 改用实际 RTT/统计；Trace 权限错误不再作为正常结果展示。
- 免 Root TTL 路径探测和 IPv4 DF/MTU 明确未知/受限状态；超时不推断准确 MTU 上限，1500 成功只报告已验证下界。
- DNS 常用记录与显式服务器、边界校验/截断 TCP 回退；HTTP 只发无凭据 HEAD、不跟随跳转，TLS 保留信任链和域名验证；报告隐藏 Cookie 与 URL 路径/查询。
- 发现仅接受 RFC1918 `/26`–`/32`、最多 64 地址/4 TCP 端口/40 秒预算，需明确确认；WOL 需确认且“发送”不代表已唤醒。新增普通 `ACCESS_NETWORK_STATE`，无 Root 或凭据格式迁移。
- 并入 V0.11.0 AI 对话布局、常驻设置、原生 Markdown 子集与独立代码复制；确认式长期记忆支持编辑、范围、加密保存、管理和最近请求记录，与聊天历史分离。旧自动记忆不导入。
- 脱敏报告先预览再复制、分享或转为可编辑 AI 草稿，不自动提交 AI、不改变交换机；旧终端和命令风险门禁保留。

### 本地验收与限制

- 338 项 JVM 测试通过；Debug/Release Lint 0 错误 / 218 警告；8 组手机/平板、语言、深浅色 UI 各 138 项断言通过；AI 回归 64 项断言通过。
- 正式 APK `NetSerial-AI-0.12.0.apk`：6,506,868 bytes；SHA-256：`fce9f7d5a4a60fe16d72ab357dabfbd769347767549b22075a6b8d6325f19cf8`。V1/V2/V3 签名、ZIP 对齐通过；证书与重新下载的 V0.10.0 正式包一致。
- API 34 干净模拟器中，真实 V0.10.0 code 12 → V0.12.0 code 14 覆盖安装、首次安装时间不变及冷启动通过。不证明真实用户密钥/历史迁移。
- 远程重新下载 APK 为 6,506,868 bytes，SHA-256 与上述本地产物及线上 `SHA256SUMS.txt` 一致。
- 无全部 Android/OEM、物理交换机、真实公网服务或付费 AI 验收声明。DNSSEC、全部 TLS 协议/吊销扫描、完整资产发现和 WOL 实际开机不在保证范围。正式包 Ping 界面附加复测因模拟器 `system_server` 退出而未完成，未计为通过。

English: Sixteen root-free, bounded network tools with adaptive layouts and honest measurement states; readable AI chat, fixed settings and explicitly confirmed memory. PR #16 is merged, tag v0.12.0 matches its merge commit, and the non-draft/non-prerelease Release is GitHub Latest. The downloaded APK and checksum file match the local artifact. Physical-device and real-network coverage is not implied; an additional production Ping UI retest was interrupted by emulator system_server exit and is not counted as passed.

## V0.11.0 — 2026-10-14 · 开发记录，未单独发布

- 本地独立 AI 预览 `.preview011` / code 13：对话优先排版、常驻设置、Markdown 子集、原始命令保留与代码复制。
- 新增“确认后记住”，加密长期记忆与聊天历史分离，管理/删除、180 天有效期、每次最多选用 5 条；不自动保存每次问答、不导入旧自动记忆。
- 源码见 `Version/V0.11.0`。这是并入 V0.12.0 的开发检查点，**没有独立 GitHub Release 或生产发布**；不以 V0.11.0 Debug 包覆盖正式版。

## V0.10.0 — 2026-10-13 · 新手排障工具箱

- 开发人员：chenwei666。完整独立源码归档：`Version/V0.10.0`；旧版本目录保持不变。
- 历史发布状态：PR [#14](https://github.com/chenwei666/NetSerial-AI/pull/14) 已合并；[V0.10.0](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0) 发布时为非预发布正式版并设为 GitHub Latest，现作为历史稳定版本记录保留。该版本线上 APK 与 SHA256SUMS.txt 已重新下载，大小和 SHA-256 与本地产物一致。

### 新增与优化

- 默认问题驱动首页：10 个场景、症状搜索、真实连接状态、折叠常用入口；专业工具按用途分组，保留既有功能。
- 手机网络向导：单目标 DNS、3 次 Ping、一个指定 TCP 端口，支持进度、取消、超时、结果解释和下一步；短期连通性不等同整网健康。
- 交换机向导：只读命令解释与复制、当前连接最新回显读取、品牌识别、端口/VLAN/体检/备份证据分析；USB 与远程终端均有“继续排障”入口。
- 脱敏报告先预览再分享，可转为全新的 AI 对话草稿；配置快照需显式保存，不自动执行命令或请求 AI。
- 兼容中英文、深浅色、既有主题和平板布局；独立 `.preview` Debug 包与正式版并存，不迁移正式版凭据。

### 修复、兼容与安全

- 修复平板主布局缺少恢复入口导致崩溃、品牌自动选择清除新结果、终端往返丢失向导状态、AI 草稿错误沿用默认 H3C 上下文、长回显遗漏最新输出。
- 增强取消竞态处理、端口/VLAN 参数检查、凭据和私钥脱敏；草稿有界且仅在当前进程保留。
- versionCode 12 / versionName 0.10.0；minSdk 21、targetSdk/compileSdk 36；无数据库、公开 API、连接协议或凭据格式迁移，沿用既有生产证书。
- 影响模块：Home/FeatureHub、新手向导、网络与交换机离线分析、USB/远程终端导航、AI 草稿、双语资源、构建验证脚本。无旧版本删除。

### 验证与已知限制

- 65 个测试类、246 项单元测试，0 失败/0 错误；Release Lint 0 错误/158 警告；Release 构建、ZIP 对齐和 V1/V2/V3 生产签名通过。
- 正式 APK：`NetSerial-AI-0.10.0.apk`，6,384,466 bytes；SHA-256：`1a081ef60a5208c881d737b389e2289addb36397f1f1f803f1f95c8d679afb16`。
- API 34 预览包已验证首页、回环检测、报告/AI 草稿、模拟厂商文本、终端往返和中英文深浅色；不等同真实交换机、USB 芯片、SSH/Telnet 服务或 AI 上游验收。
- 真实设备/付费 AI 账号、完整 Android/手机尺寸/主题矩阵仍需授权现场验收。详见 `Version/V0.10.0/docs/TEST_REPORT.md`。

## 2026-08-25 — V0.9.1 一键读取正式发布

- 开发人员：chenwei666
- 发布状态：PR [#11](https://github.com/chenwei666/NetSerial-AI/pull/11) 与发布文档 PR [#12](https://github.com/chenwei666/NetSerial-AI/pull/12) 已合并；[V0.9.1](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.9.1) 已作为非预发布正式版发布并设为 GitHub Latest。

### 新增功能、问题修复与优化

- 运维中心可一键读取当前已连接 USB、SSH 或 Telnet 会话的最近输出，自动填充并识别 H3C、Huawei、Cisco 或 Ruijie。
- 新增有界、仅内存、脱敏的活动终端会话桥接；断开会话不可读，清屏同步清除，不新增隐藏连接或自动命令执行通道。
- 增加中英文状态与异常提示；versionCode 11、versionName 0.9.1，数据库、外部 API 和配置格式不变。

### 修改文件与影响模块

- 修改 USB/SSH/Telnet 终端、活动会话 Store、运维中心页面、中英文资源、单元测试、构建脚本与 V0.9.1 完整归档；影响终端只读快照和厂商识别，不改变既有连接协议或命令发送路径。
- 删除内容：无；V0.9.0 及更早功能、Release 与独立源码归档全部保留。

### 数据、接口、配置、兼容与升级

- 数据库、外部 API、ProviderProfile 和用户配置格式无变更；新增接口仅为应用进程内只读快照，退出后自动消失。
- 继续使用既有生产证书，可从 V0.3.1–V0.9.0 覆盖升级并保留应用私有数据；安装前应从 V0.9.1 Release 下载并核对 SHA-256。

### 安全、测试与已知限制

- 60 个测试类、186 项测试通过；Release Lint 0 Error、140 Warning；ZIP 对齐、V1/V2/V3 生产签名和包元数据通过。
- 正式 APK 大小为 6,273,048 bytes，SHA-256：`8890591333b84658539bff9ba80a2367a85ca50825578c065d4d4c1e6884e646`；GitHub 远程资产摘要与重新下载校验一致，真实设备现场验收待执行。
- 已知限制：自动化门禁不能替代授权 Android、USB 转串口芯片、SSH/Telnet 服务与四厂商交换机型号/固件的现场兼容性验收。

## 2026-08-25 — V0.9.0 正式发布

- 开发人员：chenwei666
- 发布状态：PR [#9](https://github.com/chenwei666/NetSerial-AI/pull/9) 已合并到 `main`；[V0.9.0](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.9.0) 已作为非预发布正式版发布并设为 GitHub Latest。

### 新增与优化

- AI Copilot 升级为完整应用内多轮对话，支持 18 类厂商、活动档案优先、故障转移、停止、重试、加密历史、历史切换/重命名/删除、复制和脱敏分享。
- 新增 OpenAI-compatible、Anthropic Messages、HTTPS Ollama 的自由文本对话适配，同时保留原结构化四阶段命令提案接口。
- 新增 AI 围栏命令白名单提取、本地 R0–R4 重新分级和 R4 阻断；AI 仍不自动执行设备命令。
- 新增一键故障取证计划、结构化 Expect 风格运行手册、配置漂移等级/敏感变更计数和更完整的安全基线检查。
- 变更任务启动前要求预检查/命令/验证/回退计划，完成前要求命令发送与验证证据。
- AI 页面改为现代聊天布局，保留终端上下文、设备记忆和从终端装载草稿的既有流程。

### 数据、接口、配置与兼容

- 数据库变更：无。
- 持久化变更：新增有界 AI 对话历史，Android 6.0+ 通过既有 Android Keystore/AES-GCM 凭据库加密为单独记录；Android 5.x 不明文落盘。
- 外部接口：继续使用用户配置的 HTTPS 文本接口；ProviderProfile JSON 与模型目录缓存格式不变。
- 版本配置：versionCode 10、versionName 0.9.0，minSdk 21、targetSdk 36。
- 历史归档：未修改 V0.8.0 及更早目录；V0.9.0 保存完整源码、脚本与文档。

### 安全、测试与已知限制

- 对话上下文执行长度限制、ANSI 清理、敏感字段脱敏和提示注入隔离；取消不会触发厂商切换。
- 59 个测试类、181 项测试全部通过；Debug/Release Lint、Debug/Release 构建、ZIP 对齐、V1/V2/V3 签名与 APK 元数据检查通过。
- 正式 APK SHA-256：`b4c4040cfc2f1895404dd0c9b6f8024f3824f4a1baf161621e132e504ee29697`；GitHub 远程资产摘要复核一致。
- 未在真实 Android、USB、交换机或付费 AI 账号上执行未经授权的调用；各厂商账号/区域兼容性仍需现场验收。
- NETCONF/RESTCONF、gNMI、Syslog/SNMP Trap 和主动多设备拓扑采集保留到后续版本，不标记为已实现。

## 2026-08-25 — V0.8.0 开发候选

- 开发人员：chenwei666
- 发布状态：V0.8.0 独立源码归档已建立，正式 Release 尚未发布，GitHub Latest 仍为 V0.7.0。

### 修改内容

- 统一现代 Material 3 任务卡片并重做 AI 设置和网络工具箱信息架构。
- AI 扩展到 18 类入口，新增智谱等厂商以及上游模型动态同步、非秘密缓存和手工回退。
- 增加受控单目标多端口检查、地址摘要、常用端口和结果复制/分享。
- 修复 TFTP 关闭竞态，并以 SHA-256 隔离不同 AI 档案的模型缓存作用域。
- 审查修复：阻止旧 AI 凭据发送到编辑后的新端点；补齐 Qwen 独立模型目录；隔离异步模型回调；网络诊断改为结构化双语结果并支持及时取消。

### 影响与兼容

- 新增模型目录 HTTPS GET 请求和非秘密 SharedPreferences 缓存；数据库、聊天接口和 ProviderProfile 格式不变。
- versionCode 9，versionName 0.8.0；保持 minSdk 21、targetSdk 36。
- 不删除旧功能，不修改 Version/V0.7.0 及更早历史目录。

### 验证与已知限制

- 50 个测试类、163 项测试全部通过；Debug/Release Lint 和 APK 构建、ZIP 对齐、V1/V2/V3 生产签名验证通过。
- 候选 APK SHA-256：`d853750b13919992103f4532a04b816de8947aabcf187c0c1b669d24da329eef`。
- 真实设备、交换机和付费 AI 厂商模型目录仍需授权现场验收。

完整功能变更保存在各独立版本目录的 `CHANGELOG.md` 中；本文件记录版本发布后的仓库级元数据和文档调整。

## 2026-08-24 — V0.6.0 本地正式版候选

- 开发人员：chenwei666
- 发布状态：完整源码与生产签名候选 APK 已在 `Version/V0.6.0` 归档；尚未推送、创建 GitHub Release 或修改 `Latest`。

### 修改内容

- 新增四厂商识别、HTTPS 优先 Web 开通向导、GitHub 更新检测和 AI 一键故障诊断。
- 新增运维中心、只读剧本、LLDP/CDP 发现、配置合规初筛、金丝雀批次规划、多会话和配置快照。
- 新增 Tab 补全 2.0、自定义命令包和 USB XMODEM-128 文件发送。
- 根目录中英文介绍和版本历史增加 V0.6.0 候选状态，同时明确 GitHub Latest 仍为 V0.5.0。

### 验证

- 125 项单元测试、Debug/Release Lint、Debug/Release APK 构建、ZIP 对齐通过。
- APK 包名 `com.chenwei666.netserial`，versionCode 7，versionName 0.6.0，minSdk 21，targetSdk 36。
- V1/V2/V3 签名通过；证书与既有生产升级链一致。
- APK SHA-256：`1468f7dab82c4caa3ed0729cecbdb66754b1d2bfc38f1627dc5e7778e87687ec`。

### 已知限制

- 尚未完成真实 Android、USB、四厂商交换机、AI 账号和 XMODEM 接收端现场验收。
- 本记录不代表已发布 GitHub Release；远程发布需要用户另行明确授权。

## 2026-08-21 — V0.5.0 Latest 正式版

- 开发人员：chenwei666
- 发布状态：V0.5.0 由 prerelease 候选版提升为 GitHub `Latest` 正式版。

### 修改内容

- GitHub Release `v0.5.0` 取消 prerelease 标记并设为 `Latest`，标题更新为正式版 / Production Release。
- 根目录中英文 README 将当前稳定版从 V0.3.1 更新为 V0.5.0。
- 中英文版本历史同步 V0.3.1、V0.4.0、V0.5.0 的最终状态和升级关系。

### 修改文件

- `README.md`
- `README_EN.md`
- `VERSION_HISTORY.md`
- `VERSION_HISTORY_EN.md`
- `CHANGELOG.md`

### 影响范围

- 仅影响 GitHub Release 元数据和公开文档。
- Android 源码、APK、签名证书、数据库、网络接口、AI 接口、应用配置和用户数据均无变化。

### 验证

- GitHub `Latest` 链接解析到 `v0.5.0`。
- V0.5.0 Release 保留原生产签名 APK 与 `SHA256SUMS.txt`。
- APK SHA-256 保持为 `c16a95034a45adbf3c38a9e24a0211aeb9d4197d52132420e47f4c00ab264cf3`。

### 兼容性与已知限制

- V0.5.0 与 V0.3.1/V0.4.0 使用同一生产证书，覆盖升级关系不变。
- 正式发布状态不替代具体现场验收；生产网络部署前仍建议验证目标手机、USB 串口芯片和交换机型号。
