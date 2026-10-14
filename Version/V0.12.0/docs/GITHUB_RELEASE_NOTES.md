# NetSerial AI V0.12.0 · 免 Root 工具箱与 AI 记忆 / Root-free toolkit & confirmed AI memory

维护者 / Maintainer: chenwei666。

正式安装包：`NetSerial-AI-0.12.0.apk`。本版合并 V0.11.0 开发阶段的 AI 改进，V0.11.0 不另行发布正式包。

## 中文

- 网络工具箱重整为 16 个入口：基础体检、Ping、TCP、路由追踪、MTU、地址摘要、DNS、HTTP/HTTPS、TLS、本机网络、局域网发现、WOL、CIDR、MAC/OUI、端口速查、标识提取。
- 手机/平板自适应、搜索与分类、按工具展示参数；数值优先、Ping 趋势、原始输出折叠、可取消任务与错误分类。
- Ping 解析真实回包与统计；免 Root 路由追踪及 MTU 明确能力限制，不把系统错误、未知跳或超时包装为成功。
- DNS 支持常见记录和显式服务器；HTTP 只发 HEAD、不跟随重定向；TLS 保持信任链和域名验证。
- 私网发现限定 RFC1918 `/26`–`/32`、最多 64 地址/4 TCP 端口/40 秒，需确认；WOL 同样需确认，“发送”不等于“唤醒成功”。
- 报告先脱敏预览，再手动复制、分享或交给 AI 草稿；V0.11.0 AI 排版、设置和确认式记忆保持。
- 新增普通 `ACCESS_NETWORK_STATE` 权限以读取当前网络/DNS；不要求 Root。
- AI 对话优先布局、右上角常驻设置、原生 Markdown 子集排版和独立代码复制；长期记忆先编辑、选范围、确认再保存，可管理、编辑、删除，与聊天历史分开。

## English

- Sixteen focused tools, searchable categories, adaptive phone/tablet layouts, numerical results, Ping trends and collapsible raw output.
- Actual Ping replies/statistics; root-free trace and MTU report unsupported or unknown conditions honestly.
- DNS record queries, credential-free non-redirecting HTTP HEAD diagnostics and verified TLS inspection.
- Explicitly confirmed private-LAN discovery: RFC1918 `/26`–`/32`, at most 64 addresses, four TCP ports and 40 seconds. WOL reports a packet sent, not confirmed delivery or wake-up.
- Redacted report preview with manual sharing/AI draft handoff. V0.11.0 chat formatting, settings and confirmed memory remain.
- Adds normal `ACCESS_NETWORK_STATE` permission; no Root requirement.
- Conversation-first AI chat, a permanent settings action, readable native Markdown and code copying; long-term memories require explicit confirmation and support management, editing and deletion separately from chat history.

## 安装与升级 / Install & upgrade

- 正式包 / Production: `com.chenwei666.netserial`, versionCode `14`, versionName `0.12.0`, Android 5.0+；加密记忆需要 Android 6.0+。
- 沿用原生产证书，可直接覆盖上一正式版；先备份重要信息，不要先卸载。预览版是不同应用，不自动迁移其数据。旧自动记忆不自动转为确认记忆。
- Retains the production signing identity. Back up important information and install over the previous production app without uninstalling. Preview data is separate and not imported. Legacy automatic memories are not implicitly converted.

## 验证 / Verification

- 338 JVM tests; Release Lint: 0 errors / 218 warnings.
- Phone/tablet × Chinese/English × light/dark: 8 preview test combinations, 138 assertions per run. AI regression: 64 assertions. Loopback/synthetic data only; no paid AI requests.
- Production V1/V2/V3 signatures and ZIP alignment passed; certificate matches the verified public V0.10.0 APK. API 34 clean-emulator V0.10.0 → V0.12.0 in-place upgrade and cold start passed.
- APK size: **6,506,868 bytes**.
- SHA-256: `fce9f7d5a4a60fe16d72ab357dabfbd769347767549b22075a6b8d6325f19cf8`.

完整边界见 [测试报告 / Test report](https://github.com/chenwei666/NetSerial-AI/blob/main/Version/V0.12.0/docs/TEST_REPORT.md)。不宣称完整 Android/OEM 矩阵、真实交换机、公网服务、实际唤醒或真实用户凭据/历史迁移验收；DNSSEC、全部 TLS 协议/吊销扫描、精确全路径 MTU、完整局域网资产发现不在保证范围。Root-free probes remain device-dependent; unsupported capabilities are reported honestly. Field validation is still required.
