# NetSerial AI 运维终端 V0.2.0（开发中）

[中文](README.md) | [English](README_EN.md)

NetSerial AI 是面向网络运维工程师的 Android USB 串口终端。V0.2.0 在 V0.1.0 可信基线上增加 App 内 AI 供应商配置、安全凭据保险库和真实供应商适配器；本版本仍在开发中。

## 本版本已经具备

- FTDI、PL2303、CP210x、CH340/CH341 和 USB CDC 串口设备发现与连接。
- 波特率选择、文本/HEX 收发、换行选择、控制线和流控。
- 独立 `TAB` 按钮，向设备原样发送 ASCII `0x09`，不附加回车换行。
- `CompletionEngine` 离线补全接口，首批包含 H3C 用户视图 `display`、系统视图 `interface`，并隔离 CLI 视图。
- `ExecutionGuard` 本地风险接口，能够把 `reboot` 识别为 R4，并把 `display ...` 识别为 R1。
- `AiProviderCatalog`，覆盖 OpenAI、Claude、Gemini、DeepSeek、通义千问、Kimi、OpenAI-compatible 和 Ollama。
- `SafeAiCopilot`，所有厂商返回的命令都必须重新经过本地风险判断；AI 不能降低本地风险等级。
- `CredentialVault`，使用 Android Keystore AES-GCM 加密 API Key，配置对象仅保存凭据别名，明文只在受控回调期间短暂可用并在回调后清零。
- 多厂商凭据按别名隔离，别名同时参与 AES-GCM 认证，复制密文记录也不能串用其他厂商的密钥。
- `OpenAiCompatibleProvider`，支持可配置 HTTPS Endpoint、Model、Bearer 鉴权和 `/chat/completions` 文本命令规划。
- AI 请求具备终端上下文脱敏、超时、响应大小限制、取消、禁止鉴权重定向和安全错误分类。

## 当前限制

- 通用兼容 HTTP 适配器已经完成，但尚未接入 API Key 设置界面，也未使用真实厂商密钥执行联网验收。
- AI 密钥存储要求 Android 6.0 或更高版本；Android 5.x 仍可继续使用串口和离线功能，但不会降级为明文保存密钥。
- 离线命令包目前只有用于验证架构的 H3C 最小集合，华为/Cisco/锐捷命令包尚未导入。
- 尚未在真实 Android 手机、USB 转串口线和交换机上验收。
- UI 仍保留可信上游基线的简单终端布局，完整设备档案、AI 面板和补全候选面板将在后续版本加入。

## 构建

项目位于中文路径时，使用已提供的 ASCII 镜像构建脚本：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

脚本需要：

- `C:\tmp\NetSerial-tools\jdk17`
- `C:\tmp\NetSerial-tools\android-sdk`，包含 Platform 36 和 Build Tools 36.0.0

默认执行 `testDebugUnitTest`、`lintDebug` 和 `assembleDebug`。镜像目录固定为 `C:\tmp\NetSerial-build`，工作区源码是唯一编辑来源。

## 安全约束

- 本版本不包含任何 API Key、Token、账号或设备密码。
- App 默认关闭 Android 备份和明文 HTTP。
- AI 输出只是草案，必须经过 `SafeAiCopilot` 与 `ExecutionGuard`。
- R4 命令不得自动执行；当前版本实际上不提供任何 AI 自动执行入口。

详见 [docs/SECURITY.md](docs/SECURITY.md) 和 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)。

## 上游与许可证

USB 串口基础来自 Kai Morich 的 MIT 项目 [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal)，基线提交为 `7710eb7b1b69fb346f3b715960b5a5b5db08beb3`。原版权与 MIT 许可证保留在 [LICENSE.txt](LICENSE.txt)，变更说明见 [UPSTREAM.md](UPSTREAM.md)。

项目开发负责人：chenwei666。
