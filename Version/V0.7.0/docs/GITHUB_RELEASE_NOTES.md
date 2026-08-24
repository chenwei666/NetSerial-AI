# NetSerial AI V0.7.0 正式版 / Production Release

NetSerial AI V0.7.0 重新整理为主页、连接、终端、工具箱、设置五区工作台，并增加 AI 多厂商失败转移、高级诊断、安全凭据、临时 HTTP/TFTP 与签名运行手册。

> 147 项测试、Release Lint、APK 对齐、包元数据和 V1/V2/V3 生产签名均已验证。实体设备与真实网络矩阵仍需在授权环境验收。

- 自动识别 H3C、Huawei、Cisco 和 Ruijie。
- 按厂商生成 HTTPS/可选 HTTP Web 开通方案，账号密码仅用于本次发送。
- GitHub Latest 更新检测与一键 AI 故障诊断。
- 只读运维剧本、合规初筛、LLDP/CDP 发现和金丝雀批次规划。
- 多会话、脱敏配置快照、Tab 补全 2.0、自定义命令包和 USB XMODEM-128。
- 保留 Material 3、深色/主题/语言切换、R0-R4 安全门禁、受保护目标与脱敏证据。

安装前请核对 `SHA256SUMS.txt`。Web 与文件传输命令必须先在实验交换机按具体型号和版本验证。

V0.7.0 focuses on a cleaner responsive workspace plus AI provider failover, offline switch diagnostics, Keystore-backed device credential aliases, bounded temporary transfer, and signed runbook verification. Hardware acceptance and production signing are still required.

## 主要更新 / Highlights

- 四厂商自动识别 / Four-vendor automatic detection
- HTTPS 优先 Web 开通向导 / HTTPS-first Web access wizard
- GitHub Latest 更新检测 / GitHub Latest update checks
- AI 一键故障诊断 / One-tap AI fault diagnosis
- 只读剧本、合规初筛与金丝雀批次 / Read-only playbooks, compliance triage, and canary batches
- 多会话、配置快照与自定义命令包 / Multi-session, configuration snapshots, and custom command packs
- USB XMODEM-128 与发送前 SHA-256 / USB XMODEM-128 with preflight SHA-256
- 完整中英文说明 / Complete Chinese and English documentation

USB、SSH、Telnet、SFTP、AI、变更门禁、配置 Diff 和网络工具保持兼容。AI 结果仍只作为草稿，不会自动发送命令。

USB, SSH, Telnet, SFTP, AI, change gates, configuration Diff, and network tools remain compatible. AI output remains a draft and is never auto-sent.

## 安装 / Install

下载 `NetSerial-AI-v0.7.0-release.apk` 并核对 SHA-256：

`962e23209f57b24203a917474f90bda44c250c6071178e84a3d9a1b171504b81`

V0.3.1 至 V0.7.0 必须使用同一生产证书，才能覆盖升级并保留应用数据。生产使用前仍需在授权环境完成真实手机、USB、交换机、传输、AI 与 SNMPv3 验收。
