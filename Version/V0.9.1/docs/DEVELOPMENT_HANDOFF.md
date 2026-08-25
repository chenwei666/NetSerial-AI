# V0.9.1 开发交接 / Development Handoff

- 分支 / Branch: `codex/feature/v0.9.1-live-session-read`
- 版本 / Version: `0.9.1` (`versionCode 11`)
- 开发人员 / Developer: chenwei666
- 合并提交 / Merge commit: `a8191fefe0a7daad304fc42b16a9f33d974c9507`
- Pull Request: `https://github.com/chenwei666/NetSerial-AI/pull/11`
- 状态 / Status: PR #11 与发布文档 PR #12 已合并；V0.9.1 已发布为 GitHub Latest 正式版。
- 正式发布 / Release: `https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.9.1`

## 已完成

- USB、SSH、Telnet 统一的进程内只读活动会话快照。
- 运维中心连接状态卡、一键读取、自动填充与四厂商识别。
- ANSI 清理、敏感字段脱敏、会话/字符上限、断开排除和清屏同步。
- 中英文字符串、README、CHANGELOG、架构、安全、范围、测试和版本历史同步。
- 60 个测试类、186 项测试、Release Lint、Release 构建、ZIP 对齐、V1/V2/V3 签名和包元数据检查通过。

## 构建与产物

- 隔离构建目录：`C:\tmp\NetSerial-v091-build`
- Release 构建输出：`C:\tmp\NetSerial-v091-build\app\build\outputs\apk\release\app-release.apk`
- 本地正式产物：`Version/V0.9.1/artifacts/NetSerial-AI-v0.9.1-release.apk`
- 大小：`6,273,048 bytes`
- SHA-256：`8890591333b84658539bff9ba80a2367a85ca50825578c065d4d4c1e6884e646`
- 证书 SHA-256：`6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`

APK 和 `artifacts/` 按仓库规则不进入 Git。正式 APK 已从验证过的本地文件上传，GitHub 资产摘要和重新下载后的大小、SHA-256 均与本地产物一致。

## 安全边界

- 只读取现有会话已接收的输出，不自动发送版本/配置命令。
- 快照仅进程内保存，断开后不可读取；Store 不暴露连接或发送能力。
- 运维中心生成的所有命令继续经过人工预览、目标校验、维护窗口与 R0–R4 门禁。

## 后续步骤

1. 在授权真实 Android、USB 适配器和四厂商交换机上执行现场验收。
2. 在后续版本中继续消减既有非阻断 Lint Warning，并维护现场兼容矩阵。
3. 发布后状态已完成：GitHub Latest 为 V0.9.1，远程 APK 大小、SHA-256 与生产证书指纹均已校验。
