# NetSerial AI 运维终端 V0.4.0

NetSerial AI 是面向网络运维工程师的 Android 终端工具，支持 USB Console、SSH 和 Telnet，并将命令补全、多厂商命令库、AI 运维草稿、变更管控、配置对比和现场网络工具整合在一个离线优先的工作流中。

开发人员：chenwei666

## V0.4.0 新能力

- 变更任务闭环：工单、站点、设备、维护窗口、前置检查、验证、回滚和脱敏时间线；支持 Markdown/PDF 留证。
- 防错设备：实验/测试/生产环境、受保护设备、管理地址和常驻彩色目标条；受保护目标的 R3/R4 操作必须有匹配且有效的变更任务。
- 配置对比：配置规范化、SHA-256、逐行差异和 H3C/Huawei/Cisco/Ruijie 回滚草稿。
- 网络工具：IPv4/IPv6 CIDR、DNS、Ping、Traceroute、TCP、路径 MTU、MAC/OUI、端口参考、网络标识提取。
- SSH 增强：密码/键盘交互、会话级私钥、跳板机、Keepalive、SFTP 上传下载；主机密钥变化默认阻断。
- AI 四阶段计划：预检查、变更、验证、回滚；提示注入清理、本地完整性校验和设备记忆。
- 中英文完整覆盖，并新增 SSH Keepalive、网络探测超时设置。

## 快速使用

1. 在“设备档案”设置设备名、厂商、环境、管理地址；生产设备建议启用“受保护设备”。
2. 进入“变更任务”登记工单与维护窗口，并填写验证和回滚方案。
3. 选择 USB、SSH 或 Telnet 建立连接；连接前核对彩色目标条。
4. 使用 `Tab`、方向键和 Ctrl 组合键操作命令行；也可从分类命令库插入草稿。
5. 需要 AI 时，在“AI 服务商”添加任意受支持厂商或 OpenAI-compatible 端点；密钥进入 Android Keystore 保护存储。
6. 变更前后将配置送入“配置对比”，检查差异和回滚草稿。
7. 完成后在变更任务中导出 Markdown 或 PDF 证据。

## SSH 与文件传输

- 密码模式支持 password 与 keyboard-interactive。
- 私钥从 Android 文档选择器载入，仅存在当前进程内存，断开或退出后清除；不写入配置、日志或备份。
- 跳板机当前使用密码/键盘交互认证，并通过 SSH direct-tcpip 转发目标连接。
- SFTP 下载为读取操作；上传按 R3 高风险动作处理，受保护设备需有效变更任务并再次确认。
- 首次连接需人工确认主机指纹；已保存指纹发生变化时连接被阻断。

## AI 接入

内置 OpenAI、Anthropic、Gemini、DeepSeek、Qwen、Kimi、Ollama 预设，也支持自定义 OpenAI-compatible API 地址、模型和密钥。AI 只生成结构化草稿，不直接执行命令；R4 命令不会进入 AI 执行链。终端上下文会先做凭据脱敏和提示注入清理。

## 安全边界

- Telnet 明文传输，仅建议用于隔离管理网或实验环境。
- 应用不持久化 SSH/Telnet 口令、私钥或跳板机口令。
- 导出前会脱敏常见 API Key、Token、密码、私钥及网络设备 secret/community 语法。
- OUI 为离线小型参考表；未知厂商不代表设备异常。
- Ping、Traceroute、路径 MTU 结果受 Android 系统工具、ACL 和 ICMP 策略影响。
- AI、回滚和配置差异均为辅助结果，执行前必须由工程师审核。

## 构建

要求 JDK 17 与 Android SDK。Windows 推荐：

```powershell
.\scripts\build.ps1
```

已初始化生产签名的发布机：

```powershell
.\scripts\build.ps1 -Release
```

测试、Lint、签名和发布结果见 [docs/TEST_REPORT.md](docs/TEST_REPORT.md)，架构与边界见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) 和 [docs/SECURITY.md](docs/SECURITY.md)。

## 外部验收

发布前仍应在授权环境验证：USB OTG、各品牌真实交换机、私钥/键盘交互/跳板机/SFTP、Android 不同版本上的 Ping/Traceroute/MTU、PDF 视觉效果及真实 AI 账户调用。
