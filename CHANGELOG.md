# Project Changelog

## 2026-08-25 — V0.8.0 开发候选

- 开发人员：chenwei666
- 发布状态：V0.8.0 独立源码归档已建立，正式 Release 尚未发布，GitHub Latest 仍为 V0.7.0。

### 修改内容

- 统一现代 Material 3 任务卡片并重做 AI 设置和网络工具箱信息架构。
- AI 扩展到 18 类入口，新增智谱等厂商以及上游模型动态同步、非秘密缓存和手工回退。
- 增加受控单目标多端口检查、地址摘要、常用端口和结果复制/分享。
- 修复 TFTP 关闭竞态，并以 SHA-256 隔离不同 AI 档案的模型缓存作用域。
- 审查修复：阻止旧 AI 凭据发送到编辑后的新端点；补齐 Qwen 独立模型目录；隔离异步模型回调；网络诊断改为结构化双语结果并支持及时取消。

### 影响与兼容

- 新增模型目录 HTTPS GET 请求和非秘密 SharedPreferences 缓存；数据库、聊天接口和 ProviderProfile 格式不变。
- versionCode 9，versionName 0.8.0；保持 minSdk 21、targetSdk 36。
- 不删除旧功能，不修改 Version/V0.7.0 及更早历史目录。

### 验证与已知限制

- 50 个测试类、163 项测试全部通过；Debug/Release Lint 和 APK 构建、ZIP 对齐、V1/V2/V3 生产签名验证通过。
- 候选 APK SHA-256：`30d02a77efce1f7924edfcb4143517f9d89814ea13821f184e489e199eb6e87a`。
- 真实设备、交换机和付费 AI 厂商模型目录仍需授权现场验收。

完整功能变更保存在各独立版本目录的 `CHANGELOG.md` 中；本文件记录版本发布后的仓库级元数据和文档调整。

## 2026-08-24 — V0.6.0 本地正式版候选

- 开发人员：chenwei666
- 发布状态：完整源码与生产签名候选 APK 已在 `Version/V0.6.0` 归档；尚未推送、创建 GitHub Release 或修改 `Latest`。

### 修改内容

- 新增四厂商识别、HTTPS 优先 Web 开通向导、GitHub 更新检测和 AI 一键故障诊断。
- 新增运维中心、只读剧本、LLDP/CDP 发现、配置合规初筛、金丝雀批次规划、多会话和配置快照。
- 新增 Tab 补全 2.0、自定义命令包和 USB XMODEM-128 文件发送。
- 根目录中英文介绍和版本历史增加 V0.6.0 候选状态，同时明确 GitHub Latest 仍为 V0.5.0。

### 验证

- 125 项单元测试、Debug/Release Lint、Debug/Release APK 构建、ZIP 对齐通过。
- APK 包名 `com.chenwei666.netserial`，versionCode 7，versionName 0.6.0，minSdk 21，targetSdk 36。
- V1/V2/V3 签名通过；证书与既有生产升级链一致。
- APK SHA-256：`1468f7dab82c4caa3ed0729cecbdb66754b1d2bfc38f1627dc5e7778e87687ec`。

### 已知限制

- 尚未完成真实 Android、USB、四厂商交换机、AI 账号和 XMODEM 接收端现场验收。
- 本记录不代表已发布 GitHub Release；远程发布需要用户另行明确授权。

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
