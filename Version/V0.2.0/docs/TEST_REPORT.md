# V0.2.0 测试报告（开发中）

以下结果代表 V0.2.0 当前开发检查点，不代表物理设备、真实厂商 API 或正式 Release 已验收。

## 当前开发检查点

- `testDebugUnitTest`：41 项通过，0 失败，0 错误。
- 已验证 HTTPS Profile 可保存供应商、模型和凭据别名。
- 已验证远程 Profile 拒绝明文 HTTP 端点。
- 已验证凭据明文只在回调期间可用，并在成功和异常路径结束后清零。
- 已验证保存记录不包含原始凭据，多厂商凭据按别名隔离且删除互不影响。
- 已验证空凭据和空别名会被拒绝。
- 已验证密文记录移动到其他别名后认证失败，不能造成跨厂商凭据替换。
- 已验证通用兼容请求包含 Model、厂商、CLI 视图和脱敏后的终端上下文。
- 已验证完整 `/chat/completions` 地址不会重复拼接，Endpoint 查询参数和用户信息被拒绝。
- 已验证 401、空 choices、隐藏换行命令、超大响应、请求头注入和预取消请求均安全失败。
- 已验证厂商错误正文不会进入异常消息。
- 已验证配置 JSON 往返、活动配置切换、更新不重复、删除回退、悬空活动别名和未知厂商拒绝。
- 已验证配置文档不包含 API Key 字段，Profile 数量限制为 32，Model 长度限制为 256。
- 已验证 OpenAI、Gemini、DeepSeek、通义千问和 Kimi 兼容预设，以及 Claude/Ollama 专用适配隔离。
- 已验证厂商/Endpoint 变更使用新凭据别名替换，并保持活动配置一致，不会复用旧厂商密钥。
- 当前开发检查点的 Lint 和 Debug APK 构建已通过；硬件验收仍待本版本完成后执行。

- 日期：2026-08-21
- 测试人员：chenwei666
- 构建环境：Windows、Temurin JDK 17、Android SDK Platform 36、Build Tools 36.0.0
- 构建方式：`scripts/build.ps1` 同步至 `C:\tmp\NetSerial-build`

## 自动化结果

| 门禁 | 结果 |
|---|---|
| `testDebugUnitTest` | 41 项通过，0 失败，0 错误 |
| `lintDebug` | 通过，0 阻断错误；本阶段新增文件 0 告警 |
| `assembleDebug` | 通过 |
| `git diff --check` | 通过 |
| 敏感信息模式扫描 | 0 命中 |

覆盖行为包括：

- H3C 用户视图离线补全 `dis -> display`。
- H3C 系统视图补全 `int -> interface`，且不会泄漏到用户视图。
- `reboot` 被本地规则提升为 R4，AI 不能降低风险。
- `display current-configuration` 被识别为 R1。
- TAB 编码严格为单字节 `0x09`，不附加换行。
- 默认 AI 目录覆盖八种供应商/兼容模式。
- `SafeAiCopilot` 会重新评估不可信供应商返回的命令。
- `CredentialVault` 在回调成功和失败后清零明文缓冲区。
- `ProviderCredentialService` 按 Profile 凭据别名隔离多个 AI 厂商。
- AES-GCM 附加认证数据阻止密文记录跨别名替换。
- OpenAI-compatible Chat Completions 请求、响应和 Markdown JSON 围栏解析。
- 终端上下文敏感行遮蔽、单行命令约束、取消和 HTTP 安全策略。
- Profile 配置版本化 JSON、单文档持久化、活动项一致性、未知厂商和损坏数据拒绝。

## APK 验证

- applicationId：`com.chenwei666.netserial`
- versionName/versionCode：`0.2.0` / `2`
- minSdk/targetSdk：`21` / `36`
- Debug 签名：v1、v2 验证通过
- 当前开发构建 SHA-256：`14d69c301d5c39f02d365e1bb7ef78beb8379b731bdd4b7280602e4d1e0ee151`
- 当前构建产物：`C:\tmp\NetSerial-build\app\build\outputs\apk\debug\app-debug.apk`，尚未作为正式版本发布

## 未完成的测试

- 没有连接真实 Android 手机，因此未执行安装、启动和旋转/后台恢复测试。
- 没有在真机上操作 AI 设置页，因此菜单入口、双语布局、防截屏、Key 清空和生命周期取消仍需人工验收。
- 没有提供真实厂商密钥，因此没有执行可能产生费用的联网连接测试。
- 没有连接 USB 转串口线，因此未验证 FTDI/CP210x/CH340/PL2303 权限和收发。
- 没有连接真实 H3C/华为交换机，因此未验证设备原生 TAB 回显和命令语法。
- 当前 APK 是 Debug 签名，不是公开发布用 Release 包。
