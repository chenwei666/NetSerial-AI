# NetSerial AI v0.10.0 — 不记命令，也能开始排障

**从“找工具”变成“选问题”。** 上不了网、网络慢、DNS 异常、交换机登不上、端口不通……跟随向导看结果和下一步；熟练用户仍可进入完整专业工具箱。

维护者：**chenwei666** · Android 5.0+（API 21）· 简体中文 / English

## 这次更新了什么

- **10 类问题入口与症状搜索**：网络、交换机、VLAN/网关、设备识别、体检、备份和 AI 帮助；常用入口可折叠。
- **手机网络快速检测**：单目标 DNS、3 次 Ping 和一个 TCP 端口检查，带进度、取消、超时、解释和下一步。
- **交换机新手向导**：按厂商提供有说明的只读命令；连接后读取已有终端最新回显，辅助识别与分析端口、VLAN 和健康信息。
- **切换终端后继续排障**：USB/远程终端的“继续排障”返回当前进程内向导草稿。
- **报告和 AI 帮助**：先预览，再分享或转成新的可编辑 AI 对话草稿，不自动发给 AI。
- **使用体验修复**：平板恢复入口崩溃、选品牌清除结果、终端往返丢状态、AI 草稿误带 H3C 上下文、长输出遗漏最新回显。

USB Console、SSH、受控 Telnet、Tab/控制键、分类命令库、18 类 AI 接入入口及模型目录、对话历史、配置对比、中英文、深色模式和主题均保留。上游模型能力取决于你配置的服务和账号权限。

## 下载与升级

下载附件 **`NetSerial-AI-0.10.0.apk`**。生产包沿用原包名和生产证书，使用 Android 正常覆盖安装流程；不要为了升级先卸载旧正式版。独立 Preview 包不自动迁移到正式包。

- versionName: `0.10.0` · versionCode: `12`
- APK size: **6,384,466 bytes**
- APK SHA-256: `1a081ef60a5208c881d737b389e2289addb36397f1f1f803f1f95c8d679afb16`
- Certificate SHA-256: `6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`

## 验证与使用边界

65 个测试类、246 项单元测试通过；Release Lint **0 错误 / 158 警告**；生产构建、ZIP 对齐和 V1/V2/V3 签名通过。API 34 测试环境已从校验过的 v0.9.1 正式包覆盖升级并启动新首页。API 34 预览包另验证主要向导导航、回环探测、报告/AI 草稿和中英文深浅色首页。

升级检查不证明真实用户密钥/历史迁移；真实 USB 芯片、交换机型号/固件、SSH/Telnet 服务、真实 AI 账号和完整 Android/屏幕矩阵仍需现场验收。网络结果仅代表手机到指定目标的短时观测，向导不会自动执行交换机命令。分享前检查敏感信息；截断快照不是完整恢复备份。

如果它帮你减少了现场排障时间，欢迎给项目一个 Star；也欢迎提交脱敏问题复现和设备兼容结果，让更多网络同行用得放心。

---

## English — Start with a problem, not a command

v0.10.0 adds guided troubleshooting while retaining the professional toolbox:

- Ten searchable scenarios for connectivity, slow networks, DNS, switch login, ports, VLAN/gateway, identification, health, backups and AI help.
- Bounded single-target DNS, three Ping samples and one TCP-port check, with cancellation, explanations and next steps.
- Vendor-aware read-only guidance using existing terminal output; no automatic switch commands.
- Resume process-local troubleshooting after visiting USB or remote terminals.
- Preview reports before sharing or opening a fresh editable AI draft. Nothing is sent automatically.
- Fixes for tablet startup, lost results/state, incorrect H3C AI context and missing recent output.

USB Console, SSH, guarded Telnet, Tab/control keys, command references, 18 AI entry points, upstream model discovery, chat history, configuration comparison, bilingual UI, dark mode and themes remain. Provider/model access depends on your configured service and account.

Install **`NetSerial-AI-0.10.0.apk`** with the normal Android update flow. The package and production certificate are unchanged; do not uninstall first. The Preview app does not migrate into production. Package metadata and checksums appear above.

**Verified:** 65 JVM test classes / 246 tests, Release Lint with 0 errors and 158 warnings, build, alignment and V1/V2/V3 signatures. API 34 passed an in-place upgrade from a checksum-verified v0.9.1 production APK and startup of the new home. Separate preview checks covered guided navigation, loopback probes, report/AI drafts and bilingual light/dark screens.

**Limitations:** the clean test upgrade does not prove real-user key/history migration. Physical adapters/switches, real SSH/Telnet, owned AI accounts and the full Android/display matrix need field acceptance. Phone-side probes do not establish whole-network health. Review sensitive content before sharing; truncated snapshots are not guaranteed restorable backups.

Useful in your daily work? Star the project, report reproducible issues with redacted evidence, or contribute verified compatibility results.
