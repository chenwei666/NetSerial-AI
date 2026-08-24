# V0.6.0 测试报告 / Test Report

日期：2026-08-24  
开发与验证：chenwei666

## 自动化结果

| 门禁 | 结果 | 证据 |
|---|---:|---|
| Debug 单元测试 | 通过 | 40 suites，125 tests，0 failures，0 errors，0 skipped |
| Debug Android Lint | 通过 | `lintDebug` |
| Debug APK | 通过 | `assembleDebug` |
| Release 单元测试 | 通过 | 与 Debug 共用同一业务测试集 |
| Release Android Lint | 通过 | `lintRelease` |
| Release APK | 通过 | `assembleRelease` |
| ZIP alignment | 通过 | Build Tools 36.0.0 `zipalign -c -v 4` |
| APK 签名 | 通过 | V1=true、V2=true、V3=true，1 signer，RSA 4096 |
| 包元数据 | 通过 | `com.chenwei666.netserial`，versionCode 7，versionName 0.6.0，minSdk 21，targetSdk 36 |
| 应用图标资源 | 通过 | mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi 分别为 48/72/96/144/192 px，均为 32 位 ARGB PNG；48 px 与 192 px 已完成视觉检查 |
| 覆盖升级签名链 | 通过 | V0.5.0 与 V0.6.0 证书 SHA-256 相同 |
| 制品 SHA-256 | 通过 | `1468f7dab82c4caa3ed0729cecbdb66754b1d2bfc38f1627dc5e7778e87687ec` |

## 新增单元覆盖

- `DeviceFingerprintEngineTest`：四厂商识别、高置信度与通用提示符不误判。
- `WebAccessPlanFactoryTest`：四厂商 Web 命令、HTTPS 默认值、账号/密码注入拒绝与脱敏预览。
- `SemanticVersionTest`、`GitHubReleaseParserTest`：版本比较、Latest JSON 边界与 GitHub URL 限制。
- `SafePlaybookEngineTest`、`ConfigComplianceEngineTest`：只读剧本、参数边界、金丝雀批次与启发式合规规则。
- `XmodemSenderTest`：XMODEM-128 帧、块号补码、校验和与 CRC-16 已知向量。
- 原有 AI、连接参数、命令目录、设备档案、变更任务、网络工具、配置 Diff、安全门禁和脱敏测试全部回归。

## 兼容性与安全检查

- 新增 Activity 全部保持 `ThemedActivity` 外观/语言基类并在 Manifest 中禁止导出。
- Web 密码不持久化且预览/证据脱敏；会话档案不保存密码/私钥；快照写入前清洗、脱敏、规范化。
- 更新检测禁用重定向并限制 GitHub HTTPS 域名、响应大小和超时。
- 批次规划没有执行能力；XMODEM 使用 R3 目标门禁、发送前哈希、大小/超时/重试上限。
- Lint 新增文件告警完成核查；发现的 Web 预览拼接本地化告警已修复。历史上游资源仍有非阻断警告。
- V1 工具提示部分 `META-INF` 条目不受旧 JAR 签名保护；完整 APK 同时启用 V2/V3，验证通过。

## 制品

- 文件：`artifacts/NetSerial-AI-v0.6.0-release.apk`
- 大小：6,093,278 bytes
- 证书：`CN=chenwei666, O=chenwei666, C=CN`
- 证书 SHA-256：`6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`

## 未完成的外部验收

- 未连接真实 Android 手机、USB OTG 串口芯片、H3C/Huawei/Cisco/Ruijie 交换机。
- 未执行真实 SSH/Telnet/SFTP、跳板机、GitHub 更新网络、XMODEM 接收端或真实 AI 厂商账户调用。
- 未在不同 OEM、屏幕尺寸、字体缩放和系统深浅自动切换下进行视觉与无障碍验收。
- 未执行 V0.5.0 到 V0.6.0 的真机覆盖安装；签名链和包元数据已满足覆盖升级前提。

因此，自动化发布门禁已通过；生产现场兼容性仍须在授权设备上验收。
