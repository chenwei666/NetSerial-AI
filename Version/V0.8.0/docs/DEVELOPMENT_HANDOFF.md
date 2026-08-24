# V0.8.0 开发交接 / Development Handoff

## 当前状态 / Current Status

- 分支 / Branch: `codex/feature/v0.8.0`
- 基线 / Base: `origin/main` (V0.7.0)
- 状态 / Status: 功能开发与本地发布门禁完成，等待代码审查、推送和 PR/发布。
- Release gate: 50 test classes, 156 tests, Debug/Release Lint and builds all passed.

## 已完成范围 / Completed Scope

- 18 厂商 AI 目录与智谱 GLM 支持。
- 从上游 API 获取模型、缓存回退、取消、清缓存与手动模型输入。
- AI 设置页、首页、功能中心和网络工具箱现代化改版。
- 批量 TCP 端口检测、常用端口、地址分析、结果复制/分享。
- TFTP 关闭竞态修复与压力回归测试。
- 中英文 README、版本历史、安全/架构/发布文档和详细 CHANGELOG。

## 发布候选 / Release Candidate

- APK build output: `C:\tmp\NetSerial-v080-build\app\build\outputs\apk\release\app-release.apk`
- SHA-256: `84724cb4f599ef49e9d7eca41009feba7acbfe42196939678ff8a804ca1ae0ff`
- Production certificate SHA-256: `6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`

签名口令、API Key 和密钥库均未进入源码、文档或 Git 暂存区。

## 下一步 / Next Steps

1. 完成基于 `origin/main` 的需求与工程规范双重审查。
2. 将分支推送至 GitHub 并创建 PR。
3. 在真实手机、USB 串口、交换机和可用 AI 厂商账号上做发布前冒烟测试。
4. 合并后创建 V0.8.0 GitHub Release，上传 APK，并对下载后的文件复核 SHA-256。
