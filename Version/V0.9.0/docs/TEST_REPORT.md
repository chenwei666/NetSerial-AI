# V0.9.0 测试报告 / Test Report

- 测试日期 / Date: 2026-08-25
- 测试版本 / Version: 0.9.0 (`versionCode 10`)
- 开发人员 / Developer: chenwei666
- 构建根目录 / Build root: `C:\tmp\NetSerial-v090-final-build`
- 源码目录 / Source: `Version/V0.9.0`

## 结论 / Result

V0.9.0 候选版本通过 JVM 单元测试、Debug/Release Lint、Debug/Release APK 构建、ZIP 对齐、V1/V2/V3 生产签名、证书和包元数据检查。代码和安装包可以进入授权实体设备、交换机与真实 AI 账号现场验收；本报告不替代现场验收。

The V0.9.0 candidate passed JVM unit tests, Debug/Release lint, Debug/Release APK builds, ZIP alignment, V1/V2/V3 production signing, certificate verification, and package metadata checks. It can proceed to authorized device, switch, and owned-AI-account acceptance testing.

## 自动化测试 / Automated tests

- 测试类 / Test classes: **59**
- 测试项 / Tests: **181**
- 失败 / Failures: **0**
- 错误 / Errors: **0**
- 跳过 / Skipped: **0**

新增覆盖包括：多轮请求编码、OpenAI 内容分片、Anthropic 文本响应、敏感字段脱敏、加密历史往返与损坏失败关闭、瞬时重试/厂商故障转移、取消不转移、AI 命令白名单/R4 分级、一键故障取证、结构化运行手册、配置漂移和变更闭环门禁。

## Android Lint

- `lintDebug`: **PASS**
- `lintRelease`: **PASS**
- Release errors: **0**
- Release warnings: **138**

警告为既有资源、样式、Autofill、国际化候选和依赖版本提示，不阻断构建。新增 AI 对话源码与布局未产生 Lint 警告。后续版本应继续清理全仓库非阻断基线，不应把成功解释为零警告。

## 构建结果 / Build results

- `assembleDebug`: **PASS**
- `assembleRelease`: **PASS**
- Release APK size: **6,265,387 bytes**
- applicationId: `com.chenwei666.netserial`
- versionCode: `10`
- versionName: `0.9.0`
- minSdk: `21`
- targetSdk / compileSdk: `36`

## 签名、对齐与哈希 / Signing, alignment, hash

- `zipalign -c -v 4`: **PASS**
- APK Signature Scheme v1: **true**
- APK Signature Scheme v2: **true**
- APK Signature Scheme v3: **true**
- Signers: **1**
- Certificate: `CN=chenwei666, O=chenwei666, C=CN`
- Certificate SHA-256: `6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`
- APK: `artifacts/NetSerial-AI-v0.9.0-release.apk`
- APK SHA-256: `b4c4040cfc2f1895404dd0c9b6f8024f3824f4a1baf161621e132e504ee29697`

`apksigner` 对 V1 的未保护 `META-INF` 元数据给出标准警告；完整 APK 同时由 V2/V3 方案保护。V3.1/V4 未启用，不影响当前既有生产升级链。

## 未执行的现场测试 / Pending field acceptance

- 真实 Android 5.0–16 设备上的聊天滚动、IME、旋转、后台/前台和 Keystore 行为。
- USB OTG 串口、SSH、Telnet、跳板机、SFTP 和 XMODEM 实体链路。
- H3C、Huawei、Cisco、Ruijie 不同型号/版本的故障取证命令兼容性。
- 用户自有 OpenAI、Anthropic、Gemini、DeepSeek、智谱、Qwen、Doubao 等账号的真实计费请求、限流和模型差异。
- 覆盖升级、卸载重装、系统备份禁用和应用数据清理行为。

未使用真实 API Key、设备密码、Token 或生产配置进行自动化测试。
