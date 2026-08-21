# 正式发布与签名 / Production Release and Signing

## 安全模型

- Android 后续覆盖升级必须使用与 V0.3.1 相同的生产证书。
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

- 单元测试全部通过。
- Release Lint 为 0 Error。
- `apksigner verify --verbose --print-certs` 验证 V1/V2/V3 和证书指纹。
- `zipalign -c -v 4` 通过。
- `aapt dump badging` 核对 applicationId、versionCode、versionName、minSdk 和 targetSdk。
- 上传后重新下载 GitHub Release 资产并核对 SHA-256。

## English summary

Every future in-place Android update must use the same V0.3.1 production certificate. Keep the `.p12` keystore and its passphrase in separate offline backups. The DPAPI ciphertext is bound to the current Windows user and is not a portable backup. Initialize signing once, build with `scripts\build.ps1 -Release`, verify V1/V2/V3 signatures, ZIP alignment, package metadata, and SHA-256, then download the published asset and verify it again.
