# V0.9.0 开发交接 / Development Handoff

- 分支 / Branch: `codex/feature/v0.9.0`
- 版本 / Version: `0.9.0` (`versionCode 10`)
- 开发人员 / Developer: chenwei666
- 状态 / Status: 本地生产签名候选已通过自动化门禁；尚未提交、推送、创建 PR、合并或发布 GitHub Release。

## 已完成

- 完整应用内 AI 多轮对话、18 类 Provider 复用、厂商故障转移、停止/重试、加密历史、分享和命令安全装载。
- 一键故障取证、结构化运行手册、配置漂移、安全巡检和变更闭环门禁。
- 中英文资源、README、CHANGELOG、架构、安全、兼容性、测试、发布与版本历史同步。
- 59 个测试类、181 项测试、Debug/Release Lint、Debug/Release 构建、ZIP 对齐、V1/V2/V3 签名与包元数据检查通过。

## 构建与产物

- 隔离构建目录：`C:\tmp\NetSerial-v090-final-build`
- Release 构建输出：`C:\tmp\NetSerial-v090-final-build\app\build\outputs\apk\release\app-release.apk`
- 本地候选副本：`Version/V0.9.0/artifacts/NetSerial-AI-v0.9.0-release.apk`
- SHA-256：`b4c4040cfc2f1895404dd0c9b6f8024f3824f4a1baf161621e132e504ee29697`
- 证书 SHA-256：`6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`

APK 和 `artifacts/` 按仓库规则不进入 Git。发布时从已验证的本地文件上传，并在上传后重新下载核对哈希。

## 后续操作

1. 执行源码敏感信息扫描和最终 `git diff --check`。
2. 提交 `Version/V0.9.0` 及根目录文档，确保不包含 `graphify-out/` 或 APK。
3. 推送分支并创建 PR；等待用户明确允许后合并。
4. 在授权 Android、四厂商交换机和自有 AI 账号上执行现场验收。
5. 只有在合并与发布均获授权后创建 `v0.9.0` GitHub Release，并决定是否设为 `Latest`。

## 后续协议路线

NETCONF/RESTCONF、gNMI/YANG-Push、Syslog/SNMP Trap、主动多设备拓扑和实时抓包没有在 V0.9.0 冒充完成。下一阶段应从只读能力发现和真实设备兼容矩阵开始。
