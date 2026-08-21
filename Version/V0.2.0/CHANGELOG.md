# CHANGELOG

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
