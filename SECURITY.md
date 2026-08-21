# 安全策略 / Security Policy

## 支持版本

当前维护 V0.3.x。旧版本仅保留归档，不承诺安全更新。

## 报告安全问题

请使用 GitHub 仓库的 **Security → Report a vulnerability** 私密报告入口。不要在公开 Issue、讨论、Pull Request、截图或日志中发布漏洞细节、API Key、Token、交换机账号、密码、生产签名、设备配置或现场网络信息。

报告建议包含：受影响版本、复现条件、影响范围、最小化复现步骤，以及不含真实凭据的日志或示例。请先等待维护者确认和修复安排，再公开披露。

## 安全边界

- AI 生成的命令不是可信执行结果，必须由工程师复核。
- Telnet 是明文协议，只应在隔离的可信管理网络中临时使用。
- 命令库不能替代设备型号与系统版本对应的官方手册。
- 生产签名、用户 API Key 和现场设备凭据不属于开源仓库内容。

---

V0.3.x is the currently supported line. Report vulnerabilities privately through GitHub's **Security → Report a vulnerability** flow. Never disclose vulnerability details, credentials, signing material, device configuration, or site information in public issues or logs. AI-generated commands require human review, Telnet remains plaintext, and vendor command references must be verified against the exact device and software release.
