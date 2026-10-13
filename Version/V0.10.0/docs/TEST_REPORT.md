# V0.10.0 测试报告 / Test Report

- 日期 / Date: 2026-10-13
- 维护者 / Maintainer: chenwei666
- 版本 / Version: 0.10.0 (`versionCode 12`)
- 源码 / Source: `Version/V0.10.0`
- 构建镜像 / Build mirror: `C:/tmp/NetSerial-v0100-build`

## 结论 / Result

本版生产签名安装包通过重新执行的本地发布门禁和 Android API 34 升级/启动冒烟检查；以下结果不沿用 V0.9.1 的测试统计或 APK 哈希。发布与下载见 [v0.10.0 Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0)。远程资产下载校验记录由仓库根版本历史维护，本报告不把本地构建或测试环境升级视为实机验收。

The production build passed fresh local release gates and an API 34 upgrade/startup smoke check. Results are specific to v0.10.0. Remote verification is recorded in the root version history; local checks are not field acceptance.

## 自动化门禁 / Automated gates

| 检查 / Check | 结果 / Result |
| --- | --- |
| `testDebugUnitTest` | 65 classes, 246 tests, 0 failures, 0 errors |
| `lintRelease` | PASS — 0 errors, 158 warnings |
| `assembleRelease` | PASS |
| Bilingual resources / phone and tablet resume-entry checks | PASS |
| `zipalign -c 4` | PASS |
| APK signature V1 / V2 / V3 | PASS / PASS / PASS |
| Production certificate continuity | Same certificate as previous production releases |

Lint 警告尚未清零；单元测试为 JVM 测试，不是全量设备端仪器测试。中英文新手资源键、手机/平板恢复入口均通过脚本检查。源码与实际 Release 构建镜像的 app/src 和 Gradle 输入哈希一致。

The 158 Lint warnings remain. JVM tests are not device instrumentation coverage. Resource checks cover bilingual key parity and both resume layouts; app/src and Gradle inputs match the release build mirror.

## 正式安装包 / Production APK

- File: `NetSerial-AI-0.10.0.apk`
- Size: **6,384,466 bytes**
- SHA-256: `1a081ef60a5208c881d737b389e2289addb36397f1f1f803f1f95c8d679afb16`
- applicationId: `com.chenwei666.netserial`
- versionCode: `12`; versionName: `0.10.0`
- minSdk: `21`; targetSdk / compileSdk: `36`
- Certificate SHA-256: `6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`

正式包保留既有应用身份/证书；独立 `.preview` 包不自动迁移正式版 API Key、连接和历史。两者签名与 APK 哈希不可混用。

## 正式包升级与启动 / Production upgrade and startup

在 Android API 34 测试环境，先下载 GitHub v0.9.1 正式 APK 并核对其已知 SHA-256，再安装该旧版，以 `adb install -r` 覆盖升级到 v0.10.0，不卸载、不清数据。升级成功；正式 MainActivity 冷启动返回 Status ok；系统包信息为 versionCode 12 / versionName 0.10.0；界面层级包含“你想解决什么问题？”的新首页。

On API 34, the GitHub v0.9.1 APK was downloaded and checksum-verified, installed, then upgraded in place to v0.10.0 without uninstalling or clearing data. Cold startup succeeded, package metadata matched, and the new guided home was visible.

这只证明干净测试环境中的升级链和启动，不证明真实用户密钥/历史迁移、物理设备或已连接会话兼容性。

This verifies a clean test-environment upgrade and startup, not real-user key/history migration, physical hardware or connected-session compatibility.

## 功能、边界与异常 / Functional and boundary checks

- 单目标校验、IPv4/IPv6、DNS 超时/并发上限、开始前/执行中取消竞态。
- Ping 无响应和 TCP 可达的解释边界，避免推断长期丢包、带宽或认证成功。
- 空回显、未知厂商、品牌冲突、命令错误、管理关闭/物理 DOWN/协议状态、端口/VLAN 目标限定。
- 参数注入拒绝、只读命令草稿与不自动执行约束。
- 凭据、引号/JSON 键值、带提示符命令、完整/截断私钥脱敏及长度上限。
- 最新回显优先、截断前脱敏、报告语言与场景导航。

Coverage includes bounded probes, cancellation races, cautious interpretation, unknown/conflicting vendors, interface/VLAN boundaries, injection rejection, redaction, recent-output retention and report language.

## API 34 预览包运行检查 / Preview runtime checks

本机 1600×900 API 34 环境已验证：平板首页/搜索，回环 `127.0.0.1` Ping/TCP，报告预览到新 AI 草稿，未配置服务提示，人工 H3C 版本文本识别，只读命令预览，USB 页面往返，未连接远程终端往返，以及中文浅色/英文深色首页。

API 34 preview checks covered home/search, loopback probes, report-to-AI draft, missing-provider guidance, synthetic H3C text, command previews, USB-page and disconnected remote-terminal handoff, and bilingual light/dark home screens. Details and preview checksum: [BEGINNER_VALIDATION.md](BEGINNER_VALIDATION.md).

## 未验证 / Not verified

- 物理手机、真实 USB 芯片、真实 SSH/Telnet 会话与交换机型号/固件端到端兼容性。
- 真实 AI 账号、付费调用、模型答案质量和上游服务可用性。
- Android 5–13/15–16、全部尺寸/大字体/主题、长期后台/低内存/现场网络。
- 不保证完全脱敏；终端快照可能缺页/截断，不保证是完整可恢复备份。

Physical hardware/switches, real SSH/Telnet, owned AI accounts, paid requests and the full Android/display/long-running field matrix remain unverified. Redaction is best effort; truncated snapshots are not guaranteed restorable backups.
