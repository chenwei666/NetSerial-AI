# V0.1.0 架构

## 深模块与 seam

### CompletionEngine

接口只有 `complete(CompletionRequest)`。厂商、CLI 视图、前缀匹配、限制和排序隐藏在实现中。当前适配器为 `OfflineCompletionEngine`，以后命令包升级不要求 UI 调用方改变。

### ExecutionGuard

接口只有 `evaluate(CommandEvaluationRequest)`。确定性规则给出最低风险，AI 提议风险只能提高最终风险，不能降低。当前实现为 `RuleBasedExecutionGuard`。

### AiProvider 与 AiCopilot

`AiProvider` 是第三方 AI 的真实外部 seam；后续每家厂商提供独立 HTTP 适配器，测试使用内存假适配器。`SafeAiCopilot` 是调用方唯一入口，负责把供应商草案转换为经过本地安全判断的 `CommandPlan`。

### TerminalControlEncoder

把终端控制键转换成原始协议字节。V0.1.0 只开放 TAB，UI 不自行拼接字节或换行。

## 依赖方向

```text
Android UI / TerminalFragment
  -> TerminalControlEncoder
  -> CompletionEngine

Future AI UI
  -> AiCopilot / SafeAiCopilot
       -> AiProvider adapter
       -> ExecutionGuard
```

串口终端和离线补全不依赖 AI 网络。供应商适配器不能直接访问串口写入接口。
