# NetSerial AI 运维终端 V0.2.0

[中文](README.md) | [English](README_EN.md)

这是面向网络运维现场的 Android USB 串口终端开发版。V0.2.0 已形成“串口输入 → 离线补全/AI 草案 → 本地风险审核 → 人工载入 → 人工发送”的安全闭环。

## 快速使用

1. 在设备列表选择 USB 串口和波特率并连接。
2. 进入终端菜单“设备档案与 AI 记忆”，设置设备名称、厂商、CLI 模式和波特率。
3. 输入命令时点击离线候选；`TAB` 按钮始终向交换机发送真实 `0x09`。
4. 在终端菜单“AI 设置”新建供应商配置、输入 API Key 并设为当前配置。Ollama 使用 HTTPS 反向代理时无需 Key。
5. 点击终端底部“AI”，输入自然语言目标或待检查命令。最近终端输出默认不发送，只有勾选后才会发送脱敏后的最多 12,000 字符。
6. AI 返回的命令会重新经过本地风险判定。点击非 R4 命令只会载入输入框；再次点击发送按钮才会真正发给设备。

## AI 厂商

- OpenAI、Google Gemini、DeepSeek、通义千问/Qwen、Kimi 和自定义 OpenAI-compatible 网关使用兼容接口；
- Claude/Anthropic 使用 Messages 原生协议和 `x-api-key`；
- Ollama 使用无鉴权的 OpenAI-compatible 协议，但为保持 App 全局禁止明文 HTTP，必须放在 HTTPS 反向代理后；
- Endpoint 和模型均可修改，不依赖硬编码模型清单。

## 安全与隐私

- Key 使用 Android Keystore AES-GCM 加密，不进入配置 JSON、备份、记忆、终端日志或源码；
- Android 备份、明文 HTTP、截图和最近任务预览均受限制；
- 终端上下文经过 ANSI 清洗和凭据脱敏，并且默认不发送；
- 本地记忆只允许用户显式保存，按设备隔离，默认 180 天过期；疑似 API Key、Token、密码、私钥和 community 字段会被拒绝；
- AI 永远没有直接串口发送权限；R4 命令在 UI 中被禁用。

## 构建

中文路径下建议使用现有 ASCII 镜像脚本：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

默认执行 `testDebugUnitTest`、`lintDebug` 和 `assembleDebug`。JDK 17 位于 `C:\tmp\NetSerial-tools\jdk17`，Android SDK 位于 `C:\tmp\NetSerial-tools\android-sdk`。

## 尚需外部验收

- 未提供真实 API Key，因此未产生任何真实厂商调用或费用；
- 未连接 Android 真机、FTDI/CP210x/CH340/PL2303 线缆或真实交换机；
- 当前产物是 Debug APK，不是生产签名 Release；
- 命令包是常用命令集，具体型号/系统版本仍需以厂商文档和真机回显为准。

开发负责人：chenwei666。
