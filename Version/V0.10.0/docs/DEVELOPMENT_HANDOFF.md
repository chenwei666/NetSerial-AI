# V0.10.0 正式构建交付 / Production Build Handoff

- 维护者 / Maintainer: chenwei666
- 版本 / Version: 0.10.0 (versionCode 12)
- 完整独立源码 / Source archive: `Version/V0.10.0`
- 发布与下载 / Release: https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0

## 已完成 / Completed

- 十场景新手首页、症状搜索、单目标手机网络检测、只读交换机向导、终端继续排障、报告预览及新 AI 草稿。
- 修复平板启动、自动品牌选择、终端往返恢复、AI 默认上下文和最新回显保留问题。
- 65 类 / 246 项测试通过，Release Lint 0 错误 / 158 警告，正式构建、ZIP 对齐、V1/V2/V3 签名及包元数据检查通过。
- API 34 测试环境从校验过的 v0.9.1 正式包覆盖升级到 v0.10.0，冷启动和新首页通过。源码 app/src 与 Gradle 输入和实际构建镜像哈希一致。
- 独立预览包的向导、回环探测、报告/AI 草稿、终端导航和中英文深浅色首页已验证，详细范围见 TEST_REPORT.md。

## 产物 / Artifact

- `NetSerial-AI-0.10.0.apk`: 6,384,466 bytes
- SHA-256: `1a081ef60a5208c881d737b389e2289addb36397f1f1f803f1f95c8d679afb16`
- Certificate SHA-256: `6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`
- Build mirror: `C:/tmp/NetSerial-v0100-build`

APK 不进入源码 Git；发布后远程下载校验与最终发布状态记录在仓库根版本历史，独立版本归档保留本次构建事实。

## 边界与后续 / Boundaries and follow-up

不会自动下发交换机命令、发送 AI 请求或扫描子网；报告需要人工检查，配置回显不保证完整备份。生产升级保持既有包名/证书，独立 Preview 不迁移凭据。

真实手机、USB 芯片、交换机固件、SSH/Telnet 会话、AI 账号和完整 Android/显示矩阵仍需授权现场验收；干净测试升级不等于真实用户密钥与历史迁移验收。非阻断 Lint 警告另行消减。
