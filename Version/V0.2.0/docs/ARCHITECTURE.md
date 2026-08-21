# V0.2.0 架构

## 深模块与 seam

### CompletionEngine

接口只有 `complete(CompletionRequest)`。厂商、CLI 视图、前缀匹配、限制和排序隐藏在实现中。当前适配器为 `OfflineCompletionEngine`，以后命令包升级不要求 UI 调用方改变。

### ExecutionGuard

接口只有 `evaluate(CommandEvaluationRequest)`。确定性规则给出最低风险，AI 提议风险只能提高最终风险，不能降低。当前实现为 `RuleBasedExecutionGuard`。

### AiProvider 与 AiCopilot

`AiProvider` 是第三方 AI 的真实外部 seam；后续每家厂商提供独立 HTTP 适配器，测试使用内存假适配器。`SafeAiCopilot` 是调用方唯一入口，负责把供应商草案转换为经过本地安全判断的 `CommandPlan`。

### CredentialVault

`CredentialVault` 是所有 AI 厂商共享的凭据入口。`ProviderProfile` 仅保存 `credentialAlias`，`ProviderCredentialService` 通过别名选择密钥。`SecureCredentialVault` 负责受控回调和内存清零，`AndroidKeystoreSecretCipher` 负责 AES-GCM 并把别名作为附加认证数据，`SharedPreferencesCredentialRecordStore` 只持久化版本号、随机 IV 和密文。

Android 6.0 以下设备明确拒绝启用 AI 凭据存储，不允许回退到明文、弱加密或可导出的应用内固定密钥。

### OpenAiCompatibleProvider

`OpenAiCompatibleProvider` 实现通用文本兼容层。它将 `AiRequest` 编码为 `model + messages + stream=false`，通过 `ProviderCredentialService` 在受控回调内取得凭据，再调用 `ChatHttpTransport`。响应只接受最多 20 个单行命令步骤，并转换为不可信 `AiDraftPlan`，随后仍必须经过 `SafeAiCopilot` 与本地 `ExecutionGuard`。

`UrlConnectionChatHttpTransport` 只接受 HTTPS，禁止自动重定向，固定连接/读取超时，限制响应为 512 KiB，支持主动取消，并丢弃非成功响应正文。`TerminalContextSanitizer` 在编码请求前遮蔽可能包含 API Key、密码、Token、团体字或共享密钥的整行内容。

### ProviderProfileManager 与设置 UI

`ProviderProfileManager` 通过 `ProviderProfilePersistence` 深接口管理最多 32 个配置和一个活动配置。`ProviderProfilesJsonCodec` 只序列化厂商 ID、HTTPS 地址、模型和凭据别名；`SharedPreferencesProviderProfilePersistence` 以单文档提交，避免多字段部分写入。损坏文档、重复别名、未知厂商和悬空活动别名均被整体拒绝，不做静默部分恢复。

`AiProviderSettingsActivity` 是独立设置页面，通过 `ProviderProfileManager` 和 `ProviderCredentialService` 协调配置与密钥，不依赖串口连接。菜单可从设备页或终端页进入。`AiConnectionTestCoordinator` 独立管理单线程请求、主动取消和生命周期关闭；连接测试只在用户确认后调用通用兼容适配器，并使用空终端上下文。Claude 原生协议和 Ollama 本地模式不会误走这一测试路径。

### TerminalControlEncoder

把终端控制键转换成原始协议字节。V0.1.0 只开放 TAB，UI 不自行拼接字节或换行。

## 依赖方向

```text
Android UI / TerminalFragment
  -> TerminalControlEncoder
  -> CompletionEngine

AiProviderSettingsActivity
  -> ProviderProfileManager
       -> ProviderProfilesJsonCodec
       -> SharedPreferencesProviderProfilePersistence
  -> ProviderCredentialService
       -> CredentialVault
            -> Android Keystore AES-GCM
            -> Encrypted SharedPreferences records

Future AI conversation UI
  -> AiCopilot / SafeAiCopilot
       -> OpenAiCompatibleProvider
            -> OpenAiCompatibleJsonCodec
            -> ChatHttpTransport
       -> ExecutionGuard
  -> active ProviderProfile
```

串口终端和离线补全不依赖 AI 网络。供应商适配器不能直接访问串口写入接口。
