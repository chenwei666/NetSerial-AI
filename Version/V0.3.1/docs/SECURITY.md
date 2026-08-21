# V0.3.0 安全说明 / Security Notes

## 已实施控制

- SSH 使用严格主机密钥校验。首次连接显示 JSch 提供的主机密钥信息并等待用户核验；已保存密钥发生变化时拒绝继续。
- 已知主机存储在应用私有目录 `ssh_known_hosts`；“忘记全部主机”需要独立确认。
- SSH 密码输入禁用自动填充和实例状态保存；远程终端使用 `FLAG_SECURE`；密码不会写入 SharedPreferences、文件、日志、导出或源码。
- Telnet 默认关闭，启用设置和每次连接各有一次明文风险确认；Telnet 不提供自动凭据登录。
- `usesCleartextTraffic=false` 继续保护应用 HTTP 路径；Telnet 是用户明确启用的原始 Socket，不改变 AI Endpoint 必须使用 HTTPS 的规则。
- AI Key 使用 Android Keystore AES-GCM；AI 设置、远程终端等敏感界面限制截图；Android 自动备份关闭。
- 终端上下文进入 AI 前执行 ANSI 清洗、敏感字段脱敏和 12,000 字符上限，默认不发送。
- 命令库、补全和 AI 均只填入草稿。R3 发送前确认，R4 要求输入 `EXECUTE`。
- 终端输出限制为 200,000 字符，避免长会话无限占用内存。

## 威胁和剩余风险

- 首次 SSH 信任仍依赖用户从独立可信渠道核对指纹；未经核对直接点击信任不能防止中间人攻击。
- Telnet 的账号、密码和配置天然明文，App 无法修复协议本身；隔离网络和尽快迁移 SSH 是唯一合理控制。
- Android 内存、系统键盘、已 Root 设备和恶意无障碍服务超出本应用可完全控制的边界。
- 命令模板和 AI 输出可能不适配具体型号/版本；人工审核、变更审批、配置备份和回退方案仍是生产变更前置条件。
- Debug APK 不能作为生产签名发布物。

## English summary

SSH host-key verification is mandatory, changed keys are blocked, passwords are memory-only, and remote screens block screenshots. Telnet remains plaintext, is disabled by default, and requires two explicit decisions. AI and command catalogs create drafts only; high-risk and critical sends require additional confirmation. Physical-device and real-switch acceptance remains required.
