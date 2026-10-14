# 正式发布与签名 / Production Release and Signing

## V0.12.0 当前交付状态 / Current delivery status

V0.12.0 已获用户明确授权正式发布。生产构建、338 项单元测试、Release Lint 0 错误/218 警告、V1/V2/V3 签名、ZIP 对齐与干净模拟器 V0.10.0 → V0.12.0 覆盖升级和冷启动已通过。远程发布最终状态见仓库根版本历史与 [Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.12.0)；不由本地验收推断远程资产已经上传。

- 正式 applicationId：`com.chenwei666.netserial`；versionName `0.12.0`，versionCode `14`。预览 applicationId：`com.chenwei666.netserial.preview012`；versionName `0.12.0-preview`。
- 正式 applicationId 保持 `com.chenwei666.netserial`，不得以预览签名或另建生产证书替代原有升级身份。
- 预览与正式版、旧预览可以共存；不自动导入其 API Key、连接、历史和记忆，不构成真实数据升级验证。
- 当前源码目录 `Version/V0.12.0`；独立构建镜像 `C:/tmp/NetSerial-v0120-build`。验证以 [TEST_REPORT](TEST_REPORT.md) 的实际记录为准，哈希以对应实物的 [SHA256SUMS](SHA256SUMS.txt) 为准，不复用旧 APK 的哈希或签名结论。
- 新增普通权限 `ACCESS_NETWORK_STATE`，需在最终 APK 元数据中核对；本轮无 Root、数据库或凭据迁移。
- 网络工具需按手机/平板、语言/外观和取消/异常流程验证。特别检查 Ping RTT、Trace 受限状态、MTU 未知/下界、DNS 截断与恶意帧、HTTP 不跟随跳转/不泄露头、TLS 校验、私网发现与 WOL 确认。不得将回环/模拟测试扩大表述为真实公网、全部 OEM、物理交换机或唤醒成功。

Production release was explicitly authorized. Local build/signing gates and a clean-emulator in-place upgrade passed. The root version history and GitHub Release record remote publication verification separately. No real-user credential/history migration or physical-network acceptance is claimed.

## 安全模型

- Android 后续覆盖升级必须使用与已发布 V0.3.1–V0.10.0 相同的生产证书；正式构建时仍须比对实际已发布 APK 的证书。
- 密钥库位于仓库外的 `%LOCALAPPDATA%\NetSerial-AI\signing\netserial-release.p12`。
- 口令以 Windows DPAPI CurrentUser 加密，密文仅能由同一 Windows 用户上下文解密。
- Gradle 仅从构建进程环境变量读取签名参数；源码、Git 历史和构建日志不包含口令。
- 发布证书：RSA 4096、SHA256withRSA、PKCS12；应用启用 V1/V2/V3 签名。

## 首次初始化

仅在受控发布机执行一次：

```powershell
powershell.exe -NoProfile -STA -ExecutionPolicy Bypass -File .\scripts\initialize-release-signing.ps1
```

脚本要求两次输入至少 16 位口令，拒绝覆盖现有密钥，并以临时文件后原子落盘。正式构建：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1 -Release
```

## 必须备份

1. 把 `netserial-release.p12` 复制到至少两个加密的离线介质。
2. 把口令记录到企业密码管理器或独立密封介质，禁止与 `.p12` 放在同一位置。
3. 记录证书 SHA-256：`6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`。
4. 定期做只读恢复演练；不得通过聊天、工单、Git、邮件或日志传递明文口令。

DPAPI 密文不是跨机器备份。迁移发布机时，应从离线 `.p12` 和独立口令恢复，并为新机重新生成本地 DPAPI 密文。丢失密钥或口令后，无法再发布可覆盖升级同一应用的数据保留版本。

## 发布验收

- 先确认待发布版本、仓库/分支、生产签名身份和用户授权；本地开发请求不自动授权发布。
- 单元测试全部通过。
- Release Lint 为 0 Error。
- `apksigner verify --verbose --print-certs` 验证 V1/V2/V3 和证书指纹。
- `zipalign -c -v 4` 通过。
- `aapt dump badging` 核对 applicationId、versionCode、versionName、minSdk 和 targetSdk。
- 在隔离测试设备验证上一正式版到本次正式包的覆盖安装、冷启动和关键流程；不未经授权读取真实用户凭据或历史。
- 对发布说明、源码归档和 APK 做敏感信息检查，核对未验证范围；确认正式版本归档不可覆盖。
- 上传后重新下载 GitHub Release 资产并核对 SHA-256。

## English summary

Every future in-place Android update must retain the published V0.3.1–V0.10.0 production signing identity, verified against the actual published APK. Keep the `.p12` keystore and its passphrase in separate offline backups. The DPAPI ciphertext is bound to the current Windows user and is not a portable backup. After explicit release authorization, initialize signing only if legitimately absent, build with `scripts\build.ps1 -Release`, verify V1/V2/V3 signatures, ZIP alignment, package metadata, upgrade behavior and SHA-256, then download the published asset and verify it again. Do not replace an existing production key or treat this checklist as evidence that release tasks have run.
