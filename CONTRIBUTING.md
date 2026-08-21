# 参与贡献 / Contributing

感谢你帮助改进 NetSerial AI。提交改动前请遵循以下约定。

## 开发要求

- 使用 JDK 17、Android SDK Platform 36 和 Build Tools 36.0.0。
- 保持 Android 5.0 / API 21 兼容，除非变更说明明确提高最低版本。
- 不要提交 API Key、Token、账号、密码、生产签名、现场 IP、真实交换机配置、APK、构建目录或 `local.properties`。
- AI、命令库和补全结果必须保持“只生成草稿、不自动执行”。
- 涉及命令发送的改动必须经过本地 R0–R4 风险规则，不得允许 AI 降低风险等级。
- Telnet 必须保持默认关闭和显式风险确认；SSH 不得绕过主机密钥校验。

## 构建与测试

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.3.1\scripts\build.ps1
```

提交前请确保单元测试、Android Lint 和 Debug 构建通过。涉及真实硬件或厂商 API 的改动，请说明设备、系统版本、测试范围和未覆盖项，但不要提交凭据或现场敏感数据。

## 提交与 Pull Request

- 使用 Conventional Commits，例如 `feat:`、`fix:`、`docs:`、`test:`、`refactor:`。
- 一个 Pull Request 聚焦一个清晰目标，并说明影响模块、兼容性、安全风险和验证结果。
- 同步更新相关中英文文档和版本变更记录。
- 保留上游 MIT 许可证、版权和来源说明。

---

Thank you for improving NetSerial AI. Use JDK 17 and Android SDK 36, preserve API 21 compatibility, keep AI output draft-only, retain SSH host-key verification and Telnet safety gates, and never commit credentials, signing material, production configuration, APKs, build output, or `local.properties`. Run unit tests, Android Lint, and a Debug build before opening a focused Pull Request with Conventional Commit messages and documented security and compatibility impact.
