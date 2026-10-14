# NetSerial AI V0.11.0 · AI 对话与确认式记忆

<img src="design/app-icon-512.png" width="144" alt="NetSerial AI 应用图标">

Android 网络运维工具箱：USB Console、SSH、Telnet、多厂商命令、Tab/控制键、AI 对话、新手排障、配置对比和现场网络检测。开发人员：chenwei666。

[English](README_EN.md) · [变更记录](CHANGELOG.md) · [测试报告](docs/TEST_REPORT.md)

## 当前状态

V0.11.0 为本地开发预览，未推送或发布到 GitHub，未生成生产签名正式包。已发布版本见 [v0.10.0 Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0)。旧版源码归档不变。

## 本次更新

- 以对话为主的 AI 页面：设备与上下文默认折叠，输入区精简，设置齿轮固定在右上角；菜单跟随深浅色。
- 原生显示标题、粗体、列表、引用、行内代码和代码块。常见 Markdown 标记正确排版，代码中的真实星号、空格和换行保留。复杂表格与嵌套语法降级为文本。
- 支持复制原文、单独复制代码和人工审阅命令。AI 不会自行执行交换机命令，R4 高危命令继续阻断。
- “记住这条”先编辑内容、选择个人偏好或已命名设备档案，再确认保存。可查看、新增、编辑、删除，不自动记住每条问答。
- 聊天历史与长期记忆分开；删除聊天不会误删记忆。Android 6.0+ 加密保存，可关闭记忆使用、查看最近请求选用的条目。最多带入 5 条，180 天到期。
- 保留既有厂商接入和模型选择、新手向导、网络检测以及主题/语言切换。本次没有请求任何付费 AI。

详见 [记忆使用与边界](docs/SECURITY.md)。设备记忆按设备档案名称隔离，不等于验证了硬件身份；未命名的默认设备和新手排障只允许个人偏好。旧自动记忆不会自动导入新确认记忆。

## 构建与预览安装

运行 `scripts/build.ps1`，执行单元测试、Lint 和 Debug 构建。工具链沿用 `C:/tmp/NetSerial-tools`，独立构建镜像为 `C:/tmp/NetSerial-v0110-build`。

Debug 包名 `com.chenwei666.netserial.preview011`，名称 NetSerial AI Preview，版本代码 13。可与正式版及旧预览共存，不自动迁移它们的 API Key、连接或历史。生产包名和签名配置保留，但本次没有执行生产发布。

Android 5.0+ 可使用一般工具；持久化加密记忆需要 Android 6.0+。实际验证和未验证范围见测试报告。物理交换机、完整 Android 版本矩阵和真实 AI 上游仍需现场验收。

## 安全边界

发送问题前检查上下文。启用记忆时，所选记忆会随请求交给配置的 AI 服务商；故障转移也遵循现有厂商配置。脱敏和秘密检测不能识别所有自由文本秘密，勿保存密码、API Key 或私钥。删除本地记忆无法撤回已发给服务商的内容。

本项目包含上游串口终端代码，许可证与署名见 [LICENSE](LICENSE.txt) 和源文件。更多安全说明见 [SECURITY](docs/SECURITY.md)。
