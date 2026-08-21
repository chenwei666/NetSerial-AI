# NetSerial AI V0.4.0 正式版 / Production Release

V0.4.0 将 NetSerial AI 从多协议串口/远程终端升级为完整的移动网络运维工作台。

主要新增：

- 变更任务、维护窗口、防错设备和彩色目标标识。
- 脱敏操作时间线，以及 Markdown/PDF 变更证据导出。
- 配置规范化、SHA-256、保序 Diff 和多厂商回滚草稿。
- IPv4/IPv6、DNS、Ping、Traceroute、TCP、路径 MTU、MAC/OUI 和端口工具。
- SSH 私钥、keyboard-interactive、密码跳板机、Keepalive 与 SFTP。
- AI 预检查/变更/验证/回滚四阶段强制校验、提示注入清理与设备记忆。
- 多行命令逐行风险检查、管理地址精确匹配和发送前维护窗口复核。
- 完整中文/English 界面和说明。

正式验证：105 项单元测试全部通过；Release Lint 0 Error；APK 通过 ZIP 对齐、Android V1/V2/V3 签名和版本元数据检查。

SHA-256：`f4036401ee19d0a6ec8e87008a837f4bfbda23abe703d2d6346108efded13262`

This release turns NetSerial AI into a complete mobile network-operations workstation: governed change tasks, wrong-target protection, redacted Markdown/PDF evidence, ordered configuration diff and rollback drafts, field network diagnostics, private-key/jump-host/SFTP SSH, and mandatory four-phase AI plans. All 105 unit tests pass; Release Lint reports zero errors; ZIP alignment and Android V1/V2/V3 signatures are verified.

Real Android devices, USB adapters, switches, jump hosts, SFTP services, and user-owned AI accounts remain external acceptance items.
