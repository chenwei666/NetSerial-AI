# V0.3.1 测试报告 / Test Report

- 日期 / Date：2026-08-21
- 测试人员 / Tester：chenwei666
- 环境：Windows、JDK 17、Android SDK Platform 36、Build Tools 36.0.0
- ASCII 构建镜像：`C:\tmp\NetSerial-build`

## 自动化门禁

| 门禁 | 结果 |
|---|---|
| `testDebugUnitTest` | 23 个测试套件、69 项通过、0 失败、0 错误、0 跳过 |
| `lintRelease` | 通过，0 阻断错误，64 条非阻断警告 |
| `assembleRelease` | 通过 |
| `zipalign -c -v 4` | 通过 |
| `apksigner verify --verbose --print-certs` | 通过 |

64 条非阻断 Lint 警告来自既有上游 UI 的可访问性、旧 API、硬编码控制键和 SDK 属性兼容提示；未创建 Lint baseline，未隐藏阻断错误。发布变体没有 Lint Error。

## 正式 APK

- applicationId：`com.chenwei666.netserial`
- versionName/versionCode：`0.3.1` / `4`
- minSdk/targetSdk：`21` / `36`
- 文件名：`NetSerial-AI-v0.3.1-release.apk`
- 大小：5,823,803 bytes
- SHA-256：`f4c410c3bf0016ecc5532bcdc27aa3ed109539d352983246a8bfd90c14972c76`
- V1 签名：通过
- V2 签名：通过
- V3 签名：通过
- 签名者：`CN=chenwei666, O=chenwei666, C=CN`
- 证书 SHA-256：`6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`
- RSA 公钥：4096 bits

`apksigner` 对旧式 V1 JAR 签名中的依赖元数据给出未受 V1 条目保护的提示；同一 APK 已同时通过覆盖整个 APK 的 V2/V3 校验，因此不构成签名失败。

## 安全检查

- 生产密钥、DPAPI 密文和签名元数据均位于仓库外；仓库忽略 `*.p12`、`*.dpapi` 和签名元数据文件。
- 签名目录已移除继承权限并仅授权当前 Windows 用户。
- 构建完成后清除所有 `NETSERIAL_RELEASE_*` 环境变量和口令字节缓冲区。
- 未使用真实 SSH/Telnet 账号、密码、API Key、Token 或现场 IP。
- USB、SSH、Telnet、AI、记忆、补全、命令库和 R0–R4 风险规则的 69 项单元测试全部回归通过。

## 尚未执行的外部验收

- 未连接真实 Android 手机，尚未验证正式 APK 安装、Debug 版卸载迁移、语言切换、旋转、后台恢复和软键盘布局。
- 未连接真实 SSH 服务或 H3C/华为/Cisco/锐捷交换机，尚未验证首次指纹、重复连接、密钥变化、认证失败和长会话。
- 未在隔离网络连接 Telnet 设备，尚未验证各厂商特有协商、中文回显和登录提示。
- 未连接 USB 串口芯片或真实交换机，尚未验证实际 TAB 回显。
- 未提供真实 AI Key，因此未产生任何外部厂商请求或费用。

这些项目依赖真实硬件、账号或人工交互，不能由本地自动化结果替代。
