# V0.9.1 测试报告 / Test Report

- 测试日期 / Date: 2026-08-25
- 测试版本 / Version: 0.9.1 (`versionCode 11`)
- 开发人员 / Developer: chenwei666
- 构建根目录 / Build root: `C:\tmp\NetSerial-v091-build`
- 源码目录 / Source: `Version/V0.9.1`

## 结论 / Result

V0.9.1 候选版本通过 JVM 单元测试、Release Lint、Release APK 构建、ZIP 对齐、V1/V2/V3 生产签名、证书和包元数据检查，可进入授权实体设备现场验收。本报告不替代 USB/SSH/Telnet 与交换机实机验收。

## 自动化测试 / Automated tests

- 测试类 / Test classes: **60**
- 测试项 / Tests: **186**
- 失败 / Failures: **0**
- 错误 / Errors: **0**
- 跳过 / Skipped: **0**

V0.9.1 新增 5 项领域测试，覆盖最近活动会话选择、断开会话排除、容量淘汰、非法时间拒绝、有界缓冲、ANSI 清理与敏感字段脱敏。原 V0.9.0 AI、连接、命令、变更、配置、网络工具和安全门禁回归测试全部通过。

## Android Lint

- `lintRelease`: **PASS**
- Release errors: **0**
- Release warnings: **140**

警告属于既有资源、样式、Autofill、国际化候选和依赖版本基线，不阻断构建。新增会话 Store、快照和终端集成源码没有新增 Lint 错误；后续继续逐步消减非阻断警告。

## 构建结果 / Build results

- `assembleRelease`: **PASS**
- Release APK size: **6,273,048 bytes**
- applicationId: `com.chenwei666.netserial`
- versionCode: `11`
- versionName: `0.9.1`
- minSdk: `21`
- targetSdk / compileSdk: `36`

## 签名、对齐与哈希 / Signing, alignment, hash

- `zipalign -c -P 16 -v 4`: **PASS**
- APK Signature Scheme v1: **true**
- APK Signature Scheme v2: **true**
- APK Signature Scheme v3: **true**
- Signers: **1**
- Certificate: `CN=chenwei666, O=chenwei666, C=CN`
- Certificate SHA-256: `6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`
- APK: `artifacts/NetSerial-AI-v0.9.1-release.apk`
- APK SHA-256: `fb105b2e9a4973b3e329defdfb8d5d0aaba70ed499aebe596b4b14e36e1c4780`

`apksigner` 对 V1 的未保护 `META-INF` 元数据给出标准警告；完整 APK 同时由 V2/V3 方案保护。V3.1/V4 未启用，不影响既有生产升级链。

## 边界与异常验证 / Boundary and failure checks

- 无连接：返回可操作提示，不读取历史断开会话。
- 已连接但无输出：提示先按 Enter 或主动执行只读 version 命令。
- 多连接：选取最近产生活动的在线会话。
- 断开/清屏：断开会话不可读；清屏后快照为空。
- 大输出：仅保留末尾有界文本，避免无限内存增长。
- 隐私：ANSI 控制符被清理，password 等敏感字段不保留明文。

## 未执行的现场测试 / Pending field acceptance

- 真实 Android 5.0–16 设备上从 USB、SSH、Telnet 终端切换到运维中心的一键读取流程。
- CH340、CP210x、FTDI、CDC/ACM 适配器和后台/前台连接状态。
- H3C、Huawei、Cisco、Ruijie 不同型号与系统版本的指纹准确率。
- 进程回收、屏幕旋转、低内存和超长持续输出场景。

未使用真实 API Key、设备密码、Token 或生产配置进行自动化测试。
