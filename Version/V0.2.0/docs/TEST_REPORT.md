# V0.2.0 测试报告（开发中）

本文件将在 V0.2.0 完整门禁运行后更新。以下结果仅代表复制前的 V0.1.0 基线，不代表 V0.2.0 已验收。

## 当前开发检查点

- `testDebugUnitTest`：16 项通过，0 失败，0 错误。
- 已验证 HTTPS Profile 可保存供应商、模型和凭据别名。
- 已验证远程 Profile 拒绝明文 HTTP 端点。
- 已验证凭据明文只在回调期间可用，并在成功和异常路径结束后清零。
- 已验证保存记录不包含原始凭据，多厂商凭据按别名隔离且删除互不影响。
- 已验证空凭据和空别名会被拒绝。
- 已验证密文记录移动到其他别名后认证失败，不能造成跨厂商凭据替换。
- 当前开发检查点的 Lint 和 Debug APK 构建已通过；硬件验收仍待本版本完成后执行。

- 日期：2026-08-21
- 测试人员：chenwei666
- 构建环境：Windows、Temurin JDK 17、Android SDK Platform 36、Build Tools 36.0.0
- 构建方式：`scripts/build.ps1` 同步至 `C:\tmp\NetSerial-build`

## 自动化结果

| 门禁 | 结果 |
|---|---|
| `testDebugUnitTest` | 16 项通过，0 失败，0 错误 |
| `lintDebug` | 通过，0 阻断错误 |
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

## APK 验证

- applicationId：`com.chenwei666.netserial`
- versionName/versionCode：`0.2.0` / `2`
- minSdk/targetSdk：`21` / `36`
- Debug 签名：v1、v2 验证通过
- 当前开发构建 SHA-256：`53f38ed129324b6a08844262b66e374c54513a62ac1bef20041294924ae72a67`
- 当前构建产物：`C:\tmp\NetSerial-build\app\build\outputs\apk\debug\app-debug.apk`，尚未作为正式版本发布

## 未完成的测试

- 没有连接真实 Android 手机，因此未执行安装、启动和旋转/后台恢复测试。
- 没有连接 USB 转串口线，因此未验证 FTDI/CP210x/CH340/PL2303 权限和收发。
- 没有连接真实 H3C/华为交换机，因此未验证设备原生 TAB 回显和命令语法。
- 当前 APK 是 Debug 签名，不是公开发布用 Release 包。
