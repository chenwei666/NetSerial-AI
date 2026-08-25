# Project Changelog

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
