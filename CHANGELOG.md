# Project Changelog

完整功能变更保存在各独立版本目录的 `CHANGELOG.md` 中；本文件记录版本发布后的仓库级元数据和文档调整。

## 2026-08-21 — V0.5.0 Latest 正式版

- 开发人员：chenwei666
- 发布状态：V0.5.0 由 prerelease 候选版提升为 GitHub `Latest` 正式版。

### 修改内容

- GitHub Release `v0.5.0` 取消 prerelease 标记并设为 `Latest`，标题更新为正式版 / Production Release。
- 根目录中英文 README 将当前稳定版从 V0.3.1 更新为 V0.5.0。
- 中英文版本历史同步 V0.3.1、V0.4.0、V0.5.0 的最终状态和升级关系。

### 修改文件

- `README.md`
- `README_EN.md`
- `VERSION_HISTORY.md`
- `VERSION_HISTORY_EN.md`
- `CHANGELOG.md`

### 影响范围

- 仅影响 GitHub Release 元数据和公开文档。
- Android 源码、APK、签名证书、数据库、网络接口、AI 接口、应用配置和用户数据均无变化。

### 验证

- GitHub `Latest` 链接解析到 `v0.5.0`。
- V0.5.0 Release 保留原生产签名 APK 与 `SHA256SUMS.txt`。
- APK SHA-256 保持为 `c16a95034a45adbf3c38a9e24a0211aeb9d4197d52132420e47f4c00ab264cf3`。

### 兼容性与已知限制

- V0.5.0 与 V0.3.1/V0.4.0 使用同一生产证书，覆盖升级关系不变。
- 正式发布状态不替代具体现场验收；生产网络部署前仍建议验证目标手机、USB 串口芯片和交换机型号。
