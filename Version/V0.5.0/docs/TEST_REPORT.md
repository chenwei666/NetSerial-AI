# V0.5.0 测试报告 / Test Report

日期：2026-08-21  
开发与验证：chenwei666

## 自动化结果

| 门禁 | 结果 | 证据 |
|---|---:|---|
| Debug 单元测试 | 通过 | 32 suites，109 tests，0 failures，0 errors，0 skipped |
| Debug Android Lint | 通过 | `lintDebug` |
| Debug APK | 通过 | `assembleDebug` |
| Release 单元测试 | 通过 | 与 Debug 共用同一业务测试集 |
| Release Android Lint | 通过 | `lintRelease` |
| Release APK | 通过 | `assembleRelease` |
| ZIP alignment | 通过 | Build Tools 36.0.0 `zipalign -c -v 4` |
| APK 签名 | 通过 | V1=true、V2=true、V3=true，1 signer，RSA 4096 |
| 包元数据 | 通过 | `com.chenwei666.netserial`，versionCode 6，versionName 0.5.0，minSdk 21，targetSdk 36 |
| 覆盖升级签名链 | 通过 | V0.4.0 与 V0.5.0 证书 SHA-256 相同 |
| 制品 SHA-256 | 通过 | `ca3b337f24a07c981b95a8c8056be64a1078b46279f3f02c8c6db4cb729a76c8` |

## 新增单元覆盖

- `AppSettingsTest`：安全默认值、外观默认值、语言规范化和非法超时。
- `CommandUsageHistoryTest`：收藏切换、最近记录去重/排序和 30 条上限。
- 原有 AI、连接参数、命令目录、设备档案、变更任务、网络工具、配置 Diff、安全门禁和脱敏测试全部回归。

## 兼容性与安全检查

- 发现并修复 `Collection.removeIf` 需要 API 24 的问题，改为 API 21 可用的倒序过滤。
- 所有 Activity 由 `ThemedActivity` 统一应用外观和语言，避免局部页面遗漏。
- 旧 `AppSettings` 构造器和 SharedPreferences 字段保持兼容；新字段有安全默认值。
- 命令历史只由内置 `CommonCommand` 生成标识，不采集终端输入或连接凭据。
- V1 工具提示部分 `META-INF` 条目不受旧 JAR 签名保护；完整 APK 同时启用 V2/V3，验证通过。

## 制品

- 文件：`artifacts/V0.5.0/NetSerial-AI-v0.5.0-release.apk`
- 大小：5,960,744 bytes
- 证书：`CN=chenwei666, O=chenwei666, C=CN`
- 证书 SHA-256：`6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`

## 未完成的外部验收

- 未连接真实 Android 手机、USB OTG 串口芯片、H3C/Huawei/Cisco/Ruijie 交换机。
- 未执行真实 SSH/Telnet/SFTP、跳板机或真实 AI 厂商账户调用。
- 未在不同 OEM、屏幕尺寸、字体缩放和系统深浅自动切换下进行视觉与无障碍验收。
- 未执行 V0.4.0 到 V0.5.0 的真机覆盖安装；签名链和包元数据已满足覆盖升级前提。

因此，自动化发布门禁已通过；生产现场兼容性仍须在授权设备上验收。
