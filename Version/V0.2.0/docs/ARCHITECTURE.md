# V0.2.0 架构

## 模块边界

- `terminal`：控制键字节编码、200,000 字符受控缓冲、ANSI 清洗和敏感内容脱敏。
- `completion`：以 `CompletionEngine.complete` 为唯一入口，按厂商和 CLI 模式返回离线候选；不依赖 AI 网络。
- `device`：非敏感设备档案，提供名称、厂商、模式和波特率上下文。
- `ai`：供应商目录、配置持久化、Keystore 凭据、兼容/原生适配器、结构化计划解析和 Copilot 编排。
- `safety`：确定性 R0-R4 规则；最终风险取本地判断与 AI 声明中的较高者。
- `memory`：供应商无关的本地结构化记忆，负责作用域、可信度、过期、密钥拒存、导入和删除。

## 调用关系

```text
TerminalFragment
  ├─ TerminalControlEncoder -> USB SerialService
  ├─ OfflineCompletionEngine -> candidate buttons
  ├─ TerminalTextBuffer -> redacted context
  ├─ DeviceProfileStore
  └─ AiCopilotActivity
       ├─ active ProviderProfile
       ├─ MemoryVault.recall
       ├─ AiProviderFactory
       │    ├─ OpenAiCompatibleProvider
       │    ├─ AnthropicProvider
       │    └─ OllamaProvider
       └─ SafeAiCopilot -> RuleBasedExecutionGuard
              └─ reviewed command -> input editor only
```

## 关键设计

`AiProvider` 隔离第三方协议，`AiProviderFactory` 负责选择适配器。兼容供应商使用 `OpenAiCompatibleProvider`，Claude 使用原生 Messages，Ollama 使用无鉴权 HTTPS 路径。传输层禁止重定向、限制超时和响应大小，并把非成功正文丢弃，避免敏感错误内容上浮。

`SafeAiCopilot` 是所有 AI 命令进入 UI 前的必经入口。供应商只能提出草案，不能获得 `SerialService` 或 `UsbSerialPort` 引用。R4 步骤被禁用，其他步骤也只返回到输入框，实际发送仍由用户单独触发。

`CredentialVault` 的 Profile 只保存别名；AES-GCM 把别名作为附加认证数据。切换厂商或 Endpoint 时生成新别名并要求重新输入 Key，防止旧厂商密钥被发送到新目标。Android 6.0 以下拒绝启用 Key 存储，不做明文降级。

`MemoryVault` 是唯一记忆事实源。只有显式用户写入或导入的已验证记录才会保存；调用 AI 时最多召回当前设备的 5 条记录。备份只包含设备档案和记忆，不包含凭据。

## 兼容性与后续 seam

当前实现保持上游 USB 串口、HEX、流控、控制线和后台服务行为。多 USB 会话、SSH、XMODEM、知识库全文索引和有限步骤自动化将通过新的连接、知识和执行适配器加入，不应绕过现有 `ExecutionGuard` 与人工确认边界。
