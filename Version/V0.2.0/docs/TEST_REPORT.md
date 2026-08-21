# V0.2.0 测试报告（开发中）

本文件将在 V0.2.0 完整门禁运行后更新。以下结果仅代表复制前的 V0.1.0 基线，不代表 V0.2.0 已验收。

## 当前开发检查点

- `testDebugUnitTest`：9 项通过，0 失败，0 错误。
- 已验证 HTTPS Profile 可保存供应商、模型和凭据别名。
- 已验证远程 Profile 拒绝明文 HTTP 端点。
- 完整 Lint、APK 构建和硬件验收仍待本版本完成后执行。

- 日期：2026-08-21
- 测试人员：chenwei666
- 构建环境：Windows、Temurin JDK 17、Android SDK Platform 36、Build Tools 36.0.0
- 构建方式：`scripts/build.ps1` 同步至 `C:\tmp\NetSerial-build`

## 自动化结果

| 门禁 | 结果 |
|---|---|
| `testDebugUnitTest` | 7 项通过，0 失败，0 错误 |
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

## APK 验证

- applicationId：`com.chenwei666.netserial`
- versionName/versionCode：`0.1.0` / `1`
- minSdk/targetSdk：`21` / `36`
- Debug 签名：v1、v2 验证通过
- SHA-256：`f8129d2c7663ae5f2c762f689a4167ddc0b173996be4db57049cd7c6404d3763`
- ASCII 构建产物与交付副本：哈希一致

## 未完成的测试

- 没有连接真实 Android 手机，因此未执行安装、启动和旋转/后台恢复测试。
- 没有连接 USB 转串口线，因此未验证 FTDI/CP210x/CH340/PL2303 权限和收发。
- 没有连接真实 H3C/华为交换机，因此未验证设备原生 TAB 回显和命令语法。
- 当前 APK 是 Debug 签名，不是公开发布用 Release 包。
