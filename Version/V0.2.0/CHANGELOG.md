# CHANGELOG

## V0.2.0 - 开发中

- 开发人员：chenwei666

### 当前变更

- 从完整 V0.1.0 源码建立独立版本目录，不覆盖历史版本。
- versionCode/versionName 更新为 `2` / `0.2.0`。
- 开始实现 `ProviderProfile` 与 `CredentialVault` 安全 seam。
- `ProviderProfile` 只保存凭据别名，远程端点强制为绝对 HTTPS 地址。

### 数据库、接口与兼容性

- 暂无数据库变更。
- V0.1.0 串口、TAB、补全和风险接口保持兼容。
- 新接口和最终升级说明将在本版本完成时补齐。

## V0.1.0 - 2026-08-21

- 开发人员：chenwei666

### 新增功能

- 从 SimpleUsbTerminal MIT 源码建立可信 USB 串口基础。
- 增加独立 TAB 控制键，原样发送 ASCII `0x09`。
- 增加厂商和 CLI 视图感知的离线 `CompletionEngine`。
- 增加本地 `ExecutionGuard`、R0-R4 风险模型和 AI 风险不可降级规则。
- 增加多 AI 供应商目录和 `SafeAiCopilot` 统一安全入口。
- 增加中文默认、英文系统语言资源。
- 增加根目录和版本目录的中英文双语项目介绍。
- 增加中文路径下的 ASCII 镜像构建脚本。

### 问题修复

- 规避 Android Gradle Plugin 在中文路径下访问 Build Tools 失败的问题。
- 防止 TAB 控制键被普通发送逻辑追加 CR/LF。

### 优化内容

- 将补全、安全判断、AI 供应商和终端控制字节拆分为可独立测试的深模块。
- 关闭 Android 自动备份和明文 HTTP 默认值。

### 删除内容

- 无。

### 主要修改文件

- `app/build.gradle`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/layout/fragment_terminal.xml`
- `app/src/main/java/de/kai_morich/simple_usb_terminal/TerminalFragment.java`
- `app/src/main/java/com/chenwei666/netserial/**`
- `app/src/test/java/com/chenwei666/netserial/**`
- `scripts/build.ps1`

### 影响模块

- USB 串口终端输入区。
- 离线命令补全。
- 命令风险评估。
- AI 供应商扩展 seam。
- Windows Android 构建流程。

### 数据库变更

- 无数据库。

### 接口变更

- 新增 `CompletionEngine.complete`。
- 新增 `ExecutionGuard.evaluate`。
- 新增 `AiProvider.propose` 和 `AiCopilot.propose`。

### 配置变更

- applicationId 改为 `com.chenwei666.netserial`。
- versionName 改为 `0.1.0`。
- Android 备份和明文流量默认关闭。

### 兼容性说明

- minSdk 仍为 21，compileSdk/targetSdk 为 36。
- 保留上游已有 USB 串口、流控、HEX 和后台服务行为。
- 与原 `de.kai_morich.simple_usb_terminal` 安装包使用不同 applicationId，可并存安装。

### 升级方式

- V0.1.0 是首次可信基线，无历史数据迁移。
- 使用 `scripts/build.ps1` 生成 Debug APK 后安装。

### 已知问题

- AI HTTP、Key 管理、记忆和知识库尚未实现。
- 离线命令包仍是最小验证集。
- 尚未完成真机、串口线和交换机验收。

### 备注

- 上游基线和许可证详见 `UPSTREAM.md`、`LICENSE.txt`。
