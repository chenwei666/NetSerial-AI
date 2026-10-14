# 参与贡献 / Contributing

感谢你帮助改进 NetSerial AI。提交改动前请遵循以下约定。

## 开发要求

- 使用 JDK 17、Android SDK Platform 36 和 Build Tools 36.0.0。
- 保持 Android 5.0 / API 21 兼容，除非变更说明明确提高最低版本。
- 不要提交 API Key、Token、账号、密码、生产签名、现场 IP、真实交换机配置、APK、构建目录或 `local.properties`。
- 普通 AI 聊天、命令库和补全保持人工审阅；V0.13.0新增用户主动启动的自动任务，必须绑定当前连接，只自动执行任务范围内的本地白名单操作，高风险或风险不明操作需要确认。执行边界见 `Version/V0.13.0/docs/SECURITY.md`。
- 涉及命令发送的改动必须经过本地 R0–R4 风险规则，不得允许 AI 降低风险等级。
- Telnet 必须保持默认关闭和显式风险确认；SSH 不得绕过主机密钥校验。

## 构建与测试

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Version\V0.13.0\scripts\build.ps1
```

提交前请确保单元测试、Android Lint 和 Debug 构建通过。V0.13.0 默认生成独立的 `com.chenwei666.netserial.preview013` 包，不覆盖正式版，也不迁移正式版凭据。脚本适用于既有 Windows 工具链；其他环境可在 `Version/V0.13.0` 配置自己的 Android SDK 后运行 Gradle 的 `testDebugUnitTest lintDebug assembleDebug`。贡献者不需要生产签名密钥。涉及真实硬件或厂商 API 的改动，请说明设备、系统版本、测试范围和未覆盖项，但不要提交凭据或现场敏感数据。

网络工具改动需保留免 Root、参数化调用、可取消/有界资源与真实数值语义；没有回包不能写作零延迟，未知/受限不能写作成功。测试优先使用合成数据和本机回环，不默认扫描真实网络。私网发现与 WOL 需显式确认，TLS 不得绕过证书或域名验证，HTTP 不自动跟随跳转或发送凭据。新增记忆必须保留用户确认、范围隔离和加密边界。

## 提交与 Pull Request

- 使用 Conventional Commits，例如 `feat:`、`fix:`、`docs:`、`test:`、`refactor:`。
- 一个 Pull Request 聚焦一个清晰目标，并说明影响模块、兼容性、安全风险和验证结果。
- 同步更新相关中英文文档和版本变更记录。
- 保留上游 MIT 许可证、版权和来源说明。

---

Thank you for improving NetSerial AI. Use JDK 17 and Android SDK 36, preserve API 21 compatibility, keep ordinary AI chat draft-only, retain SSH host-key verification and Telnet safety gates, and never commit credentials, signing material, production configuration, APKs, build output, or `local.properties`. V0.13.0 adds explicitly started, session-bound autonomous tasks with local allowlists and high/unknown-risk confirmation; see its security document. Run unit tests, Android Lint, and a Debug build before opening a focused Pull Request with Conventional Commit messages and documented security and compatibility impact.
