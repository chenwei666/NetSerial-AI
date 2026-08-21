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

### 数据库、接口与兼容性

- 暂无数据库变更。
- V0.1.0 串口、TAB、补全和风险接口保持兼容。
- 新增 `CredentialVault`、`CredentialOperation`、`ProviderCredentialService`、`SecretCipher` 和 `CredentialRecordStore` 接口。
- 新增 `ChatHttpTransport` seam，专用厂商适配器可复用安全传输策略而不依赖 UI。
- AI 凭据功能要求 Android 6.0 或更高版本，应用整体最低版本仍保持 Android 5.0。
- 最终升级说明将在本版本完成时补齐。

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
