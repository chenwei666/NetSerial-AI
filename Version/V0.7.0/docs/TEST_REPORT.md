# V0.7.0 测试报告 / Test Report

测试日期：2026-08-24  
开发人员：chenwei666  
环境：Windows、JDK 17、Android Gradle Plugin 9.2.1、compile/target SDK 36

## 自动化结果

| 检查 | 结果 | 证据 |
| --- | --- | --- |
| 单元测试 | 通过 | 48 个测试类，147 个测试，0 failure / 0 error |
| Android Lint Debug | 通过 | `lintDebug` 成功，无 error |
| Java/资源/Manifest 编译 | 通过 | `compileDebugJavaWithJavac`、`processDebugResources` 成功 |
| Debug APK | 通过 | `assembleDebug` 成功，7,689,216 bytes |
| 包元数据 | 通过 | `com.chenwei666.netserial`，versionCode 8，versionName 0.7.0，minSdk 21，targetSdk 36 |
| APK 签名结构 | 通过 | Debug 证书；v1=true、v2=true，RSA 2048 |
| SHA-256 | 通过 | `4ee5de3a4b1f9540ac9e7dceb3fafbf73f124b41524d243a5605f1a81491f2a2` |
| 敏感信息模式扫描 | 通过并人工复核 | 未发现真实 API Key、GitHub Token、AWS Key 或私钥；命中项仅为环境变量名、测试用假密码与脱敏测试输入 |

执行命令：

```powershell
.\gradlew.bat --no-daemon testDebugUnitTest lintDebug assembleDebug
```

由于 Android Gradle 在当前中文工作区路径触发 `AccessDenied`，测试使用同内容的纯英文临时镜像 `C:\tmp\NetSerial-v070-validation-20260824`。正式源码仍位于版本目录，未修改历史版本。

## V0.7.0 新增覆盖

- `FeatureNavigationTest`：功能唯一性、分类数量、搜索、收藏/最近上限与损坏数据降级。
- `SwitchDiagnosticsTest`：多厂商只读计划、CPU/内存/温度与错误计数、IP/MAC/接口路径。
- `ConfigurationBackupEngineTest`：ANSI 清洗、敏感字段脱敏、规范化与 SHA-256。
- `TopologyDiscoveryTest`：LLDP/CDP 文本解析、链路去重、SNMPv3 CIDR/算法/范围边界。
- `ControlledBatchExecutorTest`：金丝雀、逐目标审批、失败即停、验证失败回退、内联密钥拒绝。
- `RunbookPackTest`：JSON 往返、RSA/SHA-256 成功/篡改失败与凭据命令拒绝。
- `FailoverAiCopilotTest`：暂时性故障重试和厂商切换。
- `TemporaryTransferServerTest`：策略边界、HTTP 随机令牌/404/下载限制与 TFTP 只读传输。
- `DeviceCredentialAliasesTest`：SSH/设备凭据命名空间与注入字符拒绝。
- 批次门禁补充覆盖每阶段 R0-R4 风险传递、回退预授权、适配器异常回退和异常详情脱敏；TFTP 补充伪造 ACK 地址/端口拒绝。

## 首轮 Lint 发现并已修复

- Android 16 预测式返回手势：迁移到 `OnBackPressedDispatcher`。
- Android 6/7 Base64 API：移除 API 26 `java.util.Base64` 依赖，使用严格兼容解码器。

修复后重新执行完整单元测试、Lint 和 APK 构建，结果全部通过。

## 制品说明

- 自动化生成的是 Debug 测试包，不是 GitHub 正式发布包，不可冒充生产 Release。
- 正式 Release 必须由已有生产签名工作站通过 `scripts/build.ps1 -Release` 生成，并再次核对与 V0.6.0 相同的证书 SHA-256。

## 外部验收未完成

- 真机安装、屏幕旋转、手机/平板导航与 Android 5/8/12/14/16 设备矩阵。
- USB OTG 串口芯片、XMODEM、SSH/Telnet/SFTP、HTTP/TFTP 与 Wi-Fi 隔离/省电场景。
- H3C/Huawei/Cisco/Ruijie 实机命令输出、Web 开通、LLDP/CDP、SNMPv3 Agent。
- 真实 AI 厂商的限流、超时、费用、失败转移与数据合规。

这些项目需要用户授权的实体设备和账号，属于正式发布前的现场门禁。

## English summary

All 147 JVM tests, Android Lint, debug compilation, resource/manifest processing, APK assembly, metadata checks, and debug signature verification passed. Hardware, live-network, paid-provider, and production-signing validation remain explicit external release gates.
