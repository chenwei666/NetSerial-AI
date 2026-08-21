# V0.3.0 架构 / Architecture

## 模块边界

- `remote`：协议无关连接配置和状态、SSH Shell、Telnet Socket/IAC 解析、流式字符解码。
- `commands`：四厂商十分类离线命令目录、搜索、风险元数据。
- `completion`：从同一命令目录生成按厂商和 CLI 模式隔离的输入候选。
- `settings`：语言、Telnet 开关、超时、字号和字符编码；不允许保存连接凭据。
- `terminal`：控制键编码、受控文本缓冲、ANSI 清洗、敏感文本脱敏。
- `ai`：多厂商配置、Keystore 凭据、HTTPS 传输、结构化命令草稿。
- `safety`：确定性的 R0–R4 分类和发送前确认规则。
- `memory` / `device`：设备上下文和用户显式保存的非敏感记忆。

## 主要调用关系

```text
MainActivity
├── DevicesFragment ── USB ── TerminalFragment ── SerialService
├── RemoteTerminalActivity
│   ├── RemoteConnection
│   │   ├── SshRemoteConnection ── JSch ── SSH shell
│   │   └── TelnetRemoteConnection ── Socket ── TelnetProtocolCodec
│   ├── TerminalControlEncoder
│   ├── CommandLibraryActivity
│   └── AiCopilotActivity
├── AppSettingsActivity ── AppSettingsStore
└── CommandLibraryActivity ── CommonCommandCatalog

CommonCommandCatalog ── OfflineCompletionEngine
Command send ── RuleBasedExecutionGuard ── confirmation ── transport
```

USB 继续使用原有 `SerialService` 生命周期；SSH/Telnet 使用独立 `RemoteConnection`，没有获得 USB 服务引用。这样远程协议失败不会改变原 USB 连接行为，也避免继续扩大 `TerminalFragment` 的职责。

## 安全不变量

1. AI、命令库和补全只生成草稿，不能直接调用任何传输层。
2. SSH 未知主机必须确认，已知主机密钥变化必须拒绝。
3. Telnet 默认关闭并逐次确认；应用不能把明文 Telnet 描述成安全连接。
4. SSH/Telnet 密码没有持久化接口；设置、备份和日志不包含凭据。
5. R3/R4 命令在 USB 和远程终端发送前再次确认；R4 需要输入固定确认词。

## English summary

Remote transports are isolated from the existing USB service behind `RemoteConnection`. SSH and Telnet share terminal controls, the command catalog, AI draft generation, bounded output, and deterministic send guards, but neither AI nor the command library can write directly to a transport. Settings persist only non-sensitive preferences.
