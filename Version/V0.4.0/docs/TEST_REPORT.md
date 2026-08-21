# V0.4.0 测试报告 / Test Report

- 日期 / Date：2026-08-21
- 测试人员 / Tester：chenwei666
- 环境：Windows、JDK 17、Android SDK Platform 36、Build Tools 36.0.0
- ASCII 构建镜像：`C:\tmp\NetSerial-build`

## 自动化门禁

| 门禁 | 结果 |
| --- | --- |
| `testDebugUnitTest` | 31 个测试套件、105 项通过、0 失败、0 错误、0 跳过 |
| `lintRelease` | 通过，0 阻断错误，64 条非阻断警告 |
| `assembleRelease` | 通过 |
| `zipalign -c 4` | 通过 |
| `apksigner verify --verbose --print-certs` | V1/V2/V3 通过 |
| `aapt dump badging` | 包名、版本、SDK 与语言资源核对通过 |

64 条非阻断 Lint 告警主要来自上游 UI 可访问性、旧 API、SDK 属性和未使用的历史资源；未建立 Lint baseline，也未隐藏阻断错误。

## 正式 APK

- applicationId：`com.chenwei666.netserial`
- versionName/versionCode：`0.4.0` / `5`
- minSdk/targetSdk：`21` / `36`
- 文件名：`NetSerial-AI-v0.4.0-release.apk`
- 大小：5,931,284 bytes
- SHA-256：`f4036401ee19d0a6ec8e87008a837f4bfbda23abe703d2d6346108efded13262`
- V1/V2/V3 签名：通过
- 签名者：`CN=chenwei666, O=chenwei666, C=CN`
- 证书 SHA-256：`6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`
- RSA 公钥：4096 bits

`apksigner` 对旧式 V1 JAR 签名中的依赖元数据给出未受 V1 条目保护的提示；同一 APK 已同时通过覆盖 APK 内容的 V2/V3 校验，因此不是签名失败。

## 覆盖范围

- 变更任务生命周期、维护窗口和 Markdown 脱敏证据。
- 受保护目标、管理地址精确匹配及多行命令风险门禁。
- 配置规范化、重复行、保序差异、哈希和厂商回滚语法。
- IPv4/IPv6、目标校验、MAC/OUI、端口与网络标识提取。
- SSH 配置的防御性拷贝与凭据清理契约。
- AI phase 严格解析、四阶段完整性、R4 阻断和提示注入清理。
- V0.3.1 的 USB、SSH/Telnet、命令库、补全、设置、记忆和服务商回归测试。

## 安全检查

- 生产密钥、DPAPI 密文和签名元数据位于仓库外。
- 未使用真实设备账号、密码、API Key、Token 或现场地址。
- 发布内容已检查常见密钥/私钥/口令模式和开发人员标识。
- SFTP 上传和 R3/R4 命令在实际发送前重新验证目标与维护窗口。

## 尚未执行的外部验收

- 未连接真实 Android 手机、USB 串口芯片或交换机。
- 未验证真实私钥、keyboard-interactive、跳板机、SFTP 权限/中断和长会话。
- 未验证各 Android 厂商系统上的 Ping、Traceroute、DF Ping/路径 MTU。
- PDF 已通过编译和逻辑检查，未做真机分页与中文字体视觉验收。
- 未使用真实 AI Key，因此没有外部 API 请求或费用。

上述项目依赖用户授权的硬件、网络与账号，不能由本地自动化替代。
