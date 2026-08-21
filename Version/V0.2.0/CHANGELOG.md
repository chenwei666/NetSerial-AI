# CHANGELOG

## V0.2.0 - 开发中

- 开发人员：chenwei666

### 当前变更

- 从完整 V0.1.0 源码建立独立版本目录，不覆盖历史版本。
- versionCode/versionName 更新为 `2` / `0.2.0`。
- 开始实现 `ProviderProfile` 与 `CredentialVault` 安全 seam。
- `ProviderProfile` 只保存凭据别名，远程端点强制为绝对 HTTPS 地址。
- 新增 `SecureCredentialVault`，凭据明文只在受控回调期间可用，成功或异常退出后立即清零临时缓冲区。
- 新增 `AndroidKeystoreSecretCipher`，使用 Android Keystore AES-GCM 加密厂商 API Key。
- 新增 `SharedPreferencesCredentialRecordStore`，只持久化格式版本、随机 IV 和密文。
- 新增 `ProviderCredentialService`，按照 `ProviderProfile.credentialAlias` 隔离不同 AI 厂商凭据。
- 将凭据别名加入 AES-GCM 附加认证数据，阻止密文记录跨厂商替换。
- Android 6.0 以下保留原有串口和离线功能，但明确禁用 AI 凭据存储，不进行明文降级。
- 新增 `OpenAiCompatibleProvider` 与 `OpenAiCompatibleJsonCodec`，支持可配置 Endpoint、Model 和 Chat Completions JSON 命令方案。
- 新增 `UrlConnectionChatHttpTransport`，提供 HTTPS、Bearer 鉴权、超时、512 KiB 响应限制、取消和禁止重定向。
- 新增 `TerminalContextSanitizer`，发送前遮蔽潜在凭据行并限制上下文长度。
- 新增 `AiProviderError` 与 `AiProviderException`，按鉴权、限流、超时、TLS、网络、服务端、响应过大、无效响应和取消分类。
- Endpoint 新增用户信息、查询参数和片段拒绝规则；鉴权值新增请求头注入防护。
- 非成功 HTTP 响应正文不进入错误模型，不输出到日志或异常消息。
- 新增 `INTERNET` 权限和 Gson 2.14.0 JSON 依赖。
- 新增兼容请求、响应解析、脱敏、错误分类、取消、大小限制和输入攻击测试；当前共 30 项单元测试通过。
- 新增双语 `AiProviderSettingsActivity`，可从设备列表和终端菜单进入。
- 新增最多 32 个 AI 配置的新增、编辑、删除和活动配置切换；首次配置自动成为活动配置。
- 新增 `ProviderProfileManager`、版本化 `ProviderProfilesJsonCodec` 和单文档 SharedPreferences 持久化，配置不包含密钥明文。
- 新增 OpenAI、Gemini、DeepSeek、通义千问和 Kimi 的官方默认地址/模型；自定义兼容网关仍可完全覆盖地址和模型。
- 新增用户确认后的最小连接测试、费用提示、后台线程执行、主动取消和本地化错误分类；测试不发送真实终端历史。
- AI 设置页启用防截屏，API Key 输入禁用自动填充和实例状态保存，并在离开页面时清空。
- Claude 与 Ollama 可保存配置，但在专用适配器完成前禁止误走通用连接测试。
- 更换厂商或 API 地址时启用密钥别名重新绑定：必须输入新密钥，配置原子替换后再清理旧密文，防止旧厂商密钥被发送到新地址。
- 新增配置 JSON、活动配置、未知厂商、密钥别名替换、长度边界和预设目录测试；当前共 41 项单元测试通过。

### 数据库、接口与兼容性

- 暂无数据库变更。
- V0.1.0 串口、TAB、补全和风险接口保持兼容。
- 新增 `CredentialVault`、`CredentialOperation`、`ProviderCredentialService`、`SecretCipher` 和 `CredentialRecordStore` 接口。
- 新增 `ChatHttpTransport` seam，专用厂商适配器可复用安全传输策略而不依赖 UI。
- 新增 `ProviderProfilePersistence` seam，后续可替换配置存储而不改变 UI 和领域规则。
- AI 凭据功能要求 Android 6.0 或更高版本，应用整体最低版本仍保持 Android 5.0。
- 最终升级说明将在本版本完成时补齐。

### 修改文件与影响模块

- 新增/修改 `app/src/main/java/com/chenwei666/netserial/ai/**`：厂商预设、配置状态、JSON 编解码、持久化和管理器。
- 新增 `AiProviderSettingsActivity`、设置布局和中英文字符串；修改设备/终端菜单和 Manifest。
- 新增/修改 `app/src/test/java/com/chenwei666/netserial/ai/**`：配置管理、编解码、预设和边界测试。
- 影响 AI 配置管理与联网测试入口；USB 串口收发、TAB、离线补全和风险接口保持原行为。

### 配置、接口与升级

- 新增应用内 SharedPreferences 文档 `ai_provider_profiles_v1`，仅包含非敏感 Profile 元数据。
- 无数据库变更，无既有网络接口破坏；新增配置为全新命名空间，不迁移或覆盖 V0.1.0 数据。
- 从 V0.1.0 升级后可直接打开“AI 设置”创建配置；Android 5.x 可保存非敏感 Profile，但不能保存密钥或联网测试。

### 已知问题与验收状态

- 未使用真实厂商密钥执行联网验收；连接测试代码仅通过假传输和自动化边界测试。
- 尚无 Android 真机 UI、旋转、后台恢复、Keystore 和 USB 设备联合验收。
- Claude 原生协议、Ollama 明文本地地址、AI 对话记忆和终端内 AI 面板尚未完成。

## V0.1.0 - 2026-08-21

- 开发人员：chenwei666

### 新增功能

- 从 SimpleUsbTerminal MIT 源码建立可信 USB 串口基础。
- 增加独立 TAB 控制键，原样发送 ASCII `0x09`。
- 增加厂商和 CLI 视图感知的离线 `CompletionEngine`。
- 增加本地 `ExecutionGuard`、R0-R4 风险模型和 AI 风险不可降级规则。
- 增加多 AI 供应商目录和 `SafeAiCopilot` 统一安全入口。
- 增加中文默认、英文系统语言资源。
- 增加根目录和版本目录的中英文双语项目介绍。
- 增加中文路径下的 ASCII 镜像构建脚本。

### 问题修复

- 规避 Android Gradle Plugin 在中文路径下访问 Build Tools 失败的问题。
- 防止 TAB 控制键被普通发送逻辑追加 CR/LF。

### 优化内容

- 将补全、安全判断、AI 供应商和终端控制字节拆分为可独立测试的深模块。
- 关闭 Android 自动备份和明文 HTTP 默认值。

### 删除内容

- 无。

### 主要修改文件

- `app/build.gradle`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/layout/fragment_terminal.xml`
- `app/src/main/java/de/kai_morich/simple_usb_terminal/TerminalFragment.java`
- `app/src/main/java/com/chenwei666/netserial/**`
- `app/src/test/java/com/chenwei666/netserial/**`
- `scripts/build.ps1`

### 影响模块

- USB 串口终端输入区。
- 离线命令补全。
- 命令风险评估。
- AI 供应商扩展 seam。
- Windows Android 构建流程。

### 数据库变更

- 无数据库。

### 接口变更

- 新增 `CompletionEngine.complete`。
- 新增 `ExecutionGuard.evaluate`。
- 新增 `AiProvider.propose` 和 `AiCopilot.propose`。

### 配置变更

- applicationId 改为 `com.chenwei666.netserial`。
- versionName 改为 `0.1.0`。
- Android 备份和明文流量默认关闭。

### 兼容性说明

- minSdk 仍为 21，compileSdk/targetSdk 为 36。
- 保留上游已有 USB 串口、流控、HEX 和后台服务行为。
- 与原 `de.kai_morich.simple_usb_terminal` 安装包使用不同 applicationId，可并存安装。

### 升级方式

- V0.1.0 是首次可信基线，无历史数据迁移。
- 使用 `scripts/build.ps1` 生成 Debug APK 后安装。

### 已知问题

- AI HTTP、Key 管理、记忆和知识库尚未实现。
- 离线命令包仍是最小验证集。
- 尚未完成真机、串口线和交换机验收。

### 备注

- 上游基线和许可证详见 `UPSTREAM.md`、`LICENSE.txt`。
