# NetSerial AI v0.13.0 — AI 自动任务 · 高风险确认

维护者 / Maintainer: chenwei666。

## 中文

本版将 AI 建议与当前设备终端接通：描述任务、缺参数追问、逐步执行和读取回显；高风险或风险不明的操作暂停确认。**自动任务仍为受限的新功能，现场使用前需验证目标设备、系统版本及所选模型。**

- 从 USB Console、SSH 或 Telnet 终端打开 AI，进入“AI 自动执行”；绑定当前连接，重连或离页不会自动续跑。普通聊天仍提供建议。
- 精确白名单查询和任务范围内明确 VLAN/描述配置自动执行；高风险/未知分类要求输入完整目标名称。受保护设备保留变更任务与维护窗口门禁。
- 固定使用选定 AI 厂商/模型，严格单动作 JSON；32 步/10 分钟，错误、分页、交互、超时、断线或取消停止，不盲目重试、自动回退或保存。
- 本地 Web 模板支持 H3C Comware 5/7、Huawei VRP、Cisco IOS/IOS XE、Ruijie RGOS。账号密码使用专用本地输入，密码不进入 AI 请求、聊天或记忆；整份模板需高风险确认，可能创建/覆盖管理员及权限。
- 敏感回显持续隐藏至重新连接，避免分块/延迟口令泄露；CLI 提示符返回不证明 Web 页面可达或账号登录成功。Java/系统内存副本无法保证彻底擦除。
- 确认式记忆开关与管理入口，最多 5 条召回，不自动新增长期记忆；手机/平板、中英文、深浅色保留。
- 修复任务上下文丢失、无关/空回显误算验证、VLAN ID/CLI 子视图误判、重复写入与停止竞态；补齐 Cisco privilege/secret 和 Ruijie webmaster 配置脱敏。

### 安装与验证

下载 NetSerial-AI-0.13.0.apk，沿用原正式包名和生产证书；已在 API34 测试模拟器验证 V0.12.0→V0.13.0 覆盖升级与冷启动，不必先卸载。预览版与正式版数据独立，不自动导入。

- 正式版本 0.13.0 / code15；minSdk21、targetSdk36；435 项 JVM 测试通过。
- Debug/Release Lint 0 错误 / 222 警告；V1/V2/V3 和 ZIP 对齐通过。
- 最终同源预览包手机/平板 × 中英文 × 深浅色 8 组，各42项 UI断言通过。
- APK 6,560,459 bytes；SHA-256 c1fadf90e9a2045eedb2aca715f6b2bd32a69e65f6433bd1807a80dc32ffb85d。

尚未进行真实 AI 服务、物理 USB/SSH/Telnet 交换机、全部 OEM/Android 版本或真实用户凭据迁移内容的验收。模型不遵循严格 JSON 时会停止。不要把模板执行或合成测试当作所有固件通用/生产现场成功。

## English

V0.13.0 connects an explicitly started AI task to one current terminal: clarify missing parameters, send one command at a time and collect output. **Autonomous execution remains a bounded new feature requiring model/firmware field validation.**

- Exact USB/SSH/Telnet session binding; no resume after reconnect/leaving. Ordinary chat remains advisory.
- Allowlisted queries and explicitly scoped VLAN/description changes can execute automatically. High/unknown risk requires the exact typed target; protected devices retain change-window gates.
- Selected provider/model only, strict one-action JSON, 32 steps/10 minutes. Stop on errors, paging, interaction, timeout, disconnect or cancellation; no automatic retries, rollback or save.
- Local H3C Comware5/7, Huawei VRP, Cisco IOS/IOS XE and Ruijie RGOS Web templates with a dedicated local password dialog. One high-risk approval covers administrator creation/replacement and privileges. Passwords do not enter AI/chat/memory.
- Sensitive output is withheld until reconnect. CLI completion does not establish Web reachability or login success; immutable Java/system copies are not guaranteed erased.
- Confirmed-memory controls, at most five recalled records; bilingual, theme-aware phone/tablet interface. Fixes task-context loss, false verification, VLAN/view mismatch, repeated writes/cancellation races and credential redaction.

Production package 0.13.0/code15 retains the original identity, minSdk21/targetSdk36. V0.12.0 in-place upgrade/cold start passed on an API34 test emulator. 435 JVM tests passed; Debug/Release Lint has 0 errors and 222 warnings. Eight final-source preview UI cases passed 42 assertions each. Original V1/V2/V3 signature and ZIP alignment passed.

APK: 6,560,459 bytes. SHA-256: c1fadf90e9a2045eedb2aca715f6b2bd32a69e65f6433bd1807a80dc32ffb85d.

Real providers, physical switches, all OEM/Android versions and real credential/history migration content were not tested. Production/preview data are separate. Synthetic tests and templates do not certify every firmware or operational outcome.
