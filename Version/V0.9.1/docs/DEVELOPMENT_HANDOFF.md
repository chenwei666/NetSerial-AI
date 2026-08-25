# V0.9.1 开发交接 / Development Handoff

- 分支 / Branch: `codex/feature/v0.9.1-live-session-read`
- 版本 / Version: `0.9.1` (`versionCode 11`)
- 开发人员 / Developer: chenwei666
- 合并提交 / Merge commit: `a8191fefe0a7daad304fc42b16a9f33d974c9507`
- Pull Request: `https://github.com/chenwei666/NetSerial-AI/pull/11`
- 状态 / Status: 功能分支已推送且 PR #11 已合并到 `main`；尚未创建 V0.9.1 GitHub Release。

## 已完成

- USB、SSH、Telnet 统一的进程内只读活动会话快照。
- 运维中心连接状态卡、一键读取、自动填充与四厂商识别。
- ANSI 清理、敏感字段脱敏、会话/字符上限、断开排除和清屏同步。
- 中英文字符串、README、CHANGELOG、架构、安全、范围、测试和版本历史同步。
- 60 个测试类、186 项测试、Release Lint、Release 构建、ZIP 对齐、V1/V2/V3 签名和包元数据检查通过。

## 构建与产物

- 隔离构建目录：`C:\tmp\NetSerial-v091-build`
- Release 构建输出：`C:\tmp\NetSerial-v091-build\app\build\outputs\apk\release\app-release.apk`
- 本地正式候选：`Version/V0.9.1/artifacts/NetSerial-AI-v0.9.1-release.apk`
- SHA-256：`fb105b2e9a4973b3e329defdfb8d5d0aaba70ed499aebe596b4b14e36e1c4780`
- 证书 SHA-256：`6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`

APK 和 `artifacts/` 按仓库规则不进入 Git。若用户后续授权发布，应从已验证的本地文件上传，并在上传后重新下载核对哈希。

## 安全边界

- 只读取现有会话已接收的输出，不自动发送版本/配置命令。
- 快照仅进程内保存，断开后不可读取；Store 不暴露连接或发送能力。
- 运维中心生成的所有命令继续经过人工预览、目标校验、维护窗口与 R0–R4 门禁。

## 后续步骤

1. 在授权真实 Android、USB 适配器和四厂商交换机上执行现场验收。
2. 用户明确要求发布后，再创建 V0.9.1 GitHub Release 并决定是否更新 Latest。
3. 发布后校验 GitHub Latest、远程 APK 大小、SHA-256 和证书指纹。
