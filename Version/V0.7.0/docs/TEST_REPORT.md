# V0.7.0 测试报告 / Test Report

测试日期：2026-08-24  
开发人员：chenwei666  
环境：Windows、JDK 17、Android Gradle Plugin 9.2.1、compile/target SDK 36

## 自动化结果

| 检查 | 结果 | 证据 |
| --- | --- | --- |
| 单元测试 | 通过 | 48 个测试类，147 个测试，0 failure / 0 error |
| Android Lint Release | 通过 | `lintRelease` 成功，无 error |
| Java/资源/Manifest 编译 | 通过 | Debug/Release Java、资源和 Manifest 编译成功 |
| Release APK | 通过 | `assembleRelease` 成功，6,189,571 bytes |
| 包元数据 | 通过 | `com.chenwei666.netserial`，versionCode 8，versionName 0.7.0，minSdk 21，targetSdk 36 |
| APK 签名结构 | 通过 | 生产证书；v1=true、v2=true、v3=true，RSA 4096 |
| 生产证书 SHA-256 | 通过 | `6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`，与历史正式版一致 |
| ZIP 对齐 | 通过 | `zipalign -c -v 4`：Verification successful |
| Release SHA-256 | 通过 | `962e23209f57b24203a917474f90bda44c250c6071178e84a3d9a1b171504b81` |
| 敏感信息模式扫描 | 通过并人工复核 | 未发现真实 API Key、GitHub Token、AWS Key 或私钥；命中项仅为环境变量名、测试用假密码与脱敏测试输入 |

执行命令：

```powershell
.\scripts\build.ps1 -Release -Tasks clean,testDebugUnitTest,lintRelease,assembleRelease
```

由于 Android Gradle 在当前中文工作区路径触发 `AccessDenied`，正式脚本使用同内容的纯英文临时镜像 `C:\tmp\NetSerial-v060-build`。正式源码仍位于 V0.7.0 目录，未修改历史版本。

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

## 正式制品

- 文件：`NetSerial-AI-v0.7.0-release.apk`
- 已由仓库外 DPAPI 保护的历史生产证书签名；仓库与构建日志不包含口令或密钥库。
- 上传 GitHub 后必须重新下载并再次核对 APK SHA-256、包元数据和生产证书指纹。

## 外部验收未完成

- 真机安装、屏幕旋转、手机/平板导航与 Android 5/8/12/14/16 设备矩阵。
- USB OTG 串口芯片、XMODEM、SSH/Telnet/SFTP、HTTP/TFTP 与 Wi-Fi 隔离/省电场景。
- H3C/Huawei/Cisco/Ruijie 实机命令输出、Web 开通、LLDP/CDP、SNMPv3 Agent。
- 真实 AI 厂商的限流、超时、费用、失败转移与数据合规。

这些项目需要用户授权的实体设备和账号；本报告不将自动化构建冒充真实设备验证。

## English summary

All 147 JVM tests, Release Lint, release compilation, APK assembly, alignment, metadata checks, and V1/V2/V3 production-signature verification passed. Hardware, live-network, and paid-provider validation remain explicitly unverified.
