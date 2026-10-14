# V0.13.0 正式构建与预览验证报告

日期：2026-10-14。维护者：chenwei666。用户本轮已授权 GitHub 正式发布；生产构建、签名、覆盖升级和冷启动已验证，GitHub 发布/下载结果另记于根版本历史。V0.12.0 及更早源码归档不改。

## 已验证结果

| 范围 | 结果与实际证据 |
| --- | --- |
| 全量 JVM 回归 | 79 个测试类、435 项测试，0 失败、0 错误、0 跳过；读取 Gradle testDebugUnitTest 的 TEST-*.xml 汇总 |
| Android 构建 | testDebugUnitTest、lintDebug、assembleDebug、assembleDebugAndroidTest、lintRelease、assembleRelease 成功 |
| Lint | Debug / Release 均为 0 错误、222 项 Warning；警告仍保留，没有写作全部清除 |
| 自动执行引擎 | 39 项引擎测试 + 12 项任务范围测试：识别/预检查、任务范围、逐步执行、确认/拒绝、取消、断线、错误、重复写入和逐域/逐 VLAN 复核 |
| 协议与策略 | 17 项严格 schema / 本地风险测试；拒绝重复字段、Markdown、尾随动作、类型强转、命令串联、缩写自动放行、内联密码 |
| 终端桥 | 22 项测试：独占 lease、旧连接令牌、人工写入互斥、提示符和静默期、分页/错误/超时、取消、敏感回显和注册表上限 |
| Web 模板与隐私 | 1 项循环回归覆盖 10 个组合：Comware 5/7、Huawei、Cisco、Ruijie × HTTPS-only / HTTPS+HTTP；3 字符分片回显，检查实际命令顺序及秘密不进入 UI/历史/返回值 |
| 任务请求与取消 | 6 项测试：长任务原始目标保留、真实凭据脱敏而保留“本地设置密码”的任务语义、独立任务协议、立即标记取消和一次资源清理 |
| Android UI | API 34 测试环境，独立 preview013；手机/平板 × 中英文 × LIGHT/DARK 共 8 次，每次 42 项断言通过，共 336 次断言；不是 336 个不同测试 |
| APK | 正式 com.chenwei666.netserial，code15/name0.13.0，minSdk21/targetSdk36；V1/V2/V3、原证书与 ZIP 对齐有效；无 debug/testOnly/instrumentation |
| 覆盖升级 / 冷启动 | API34 测试模拟器 V0.12.0(code14) → V0.13.0(code15) 成功，firstInstallTime 保持；MainActivity Status ok / COLD；没有读取用户 API Key、历史或验证实际迁移内容 |

可复现构建：

```powershell
.\Version\V0.13.0\scripts\build.ps1 -Tasks @(
  'testDebugUnitTest','lintDebug','assembleDebug','assembleDebugAndroidTest',
  '-PtestRunner=de.kai_morich.simple_usb_terminal.AiAgentUiInstrumentation'
)
```

测试使用模拟模型、合成 CLI 输出和内存传输；没有调用真实 AI、读取真实凭据、连接物理交换机或修改生产设备。模型服务可用性、厂商实际命令和硬件兼容不能由上述测试推出。

## 手机 / 平板矩阵

密度均为 240 dpi。手机 600×1100 px（约 400 dp 宽）；平板 1600×900 px（约 1067 dp 宽）。测试只临时调整既有模拟器显示，finally 恢复原来的 1600×900 / 240；每次仪器测试恢复隔离应用的语言、外观、记忆偏好和应用 LocaleManager 设置。

| 布局 | 中文浅色 | 中文深色 | 英文浅色 | 英文深色 |
| --- | --- | --- | --- | --- |
| 手机 | 42 / 42 | 42 / 42 | 42 / 42 | 42 / 42 |
| 平板 | 42 / 42 | 42 / 42 | 42 / 42 | 42 / 42 |

断言包含：未绑定设备不能启动、任务 Activity 不导出、设置入口、记忆开关/管理与共享偏好、实际语言/主题、追问取消、精确目标确认、密码遮罩/复制阻止/清空/char[] 交付、密码不进事件卡片、最终回执不丢失和过期事件拦截。UI 用合成问答，不会发起网络或设备命令。该矩阵不是 OEM 真机兼容矩阵，也未做真实 USB/SSH/Telnet 端到端联调。

本地生成证据位于忽略的 work/agent-ui-matrix/results.json 和各 case 日志；Gradle 原始结果位于 C:/tmp/NetSerial-v0130-build/app/build/test-results/testDebugUnitTest。

## 本轮修复并回归的问题

- 模型动作大小写与严格解析不一致；普通说明文字不能变成执行命令。
- 自动任务长上下文丢掉原始目标；追问参数固定保留并有容量上限。
- 无关时钟、只返回提示符、启动配置或错误 VLAN ID 被误当作运行配置的验证。
- 不同变更域需要分别回读，后续写入重新标记待验证。
- CLI 模式必须来自实际回显；接口/AAA 子视图不当作系统视图，描述自动执行必须绑定本任务进入的 VLAN。
- 模型给出的 VLAN/描述必须符合明确任务范围；只读/禁止修改要求不能变成自动变更许可。
- 重复写入不能靠改变大小写或空格绕过；错误/交互/分页/超时停止，不盲目重试。
- 停止立即标记取消，正常关闭不丢最后 UI 回执；旧连接或旧回调不能转向新设备。
- Web 密码只走本地模板，分片/延迟口令回显在传输入口屏蔽至重连。
- 补齐 Cisco privilege/algorithm-type/secret 和 Ruijie webmaster 账号脱敏；任务设备回显仍保留保守的整行凭据清理。
- 中文“检查 VLAN 配置”中的“配置”名词不能被误作变更授权。

## 安装包与使用入口

正式包 com.chenwei666.netserial，versionCode15 / versionName0.13.0，6,560,459 bytes，SHA-256 c1fadf90e9a2045eedb2aca715f6b2bd32a69e65f6433bd1807a80dc32ffb85d。V1/V2/V3 证书 SHA-256 6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd，与 GitHub V0.12.0 相同。

独立预览包 com.chenwei666.netserial.preview013 / 0.13.0-preview 与正式版数据分开，不导入正式版连接、AI Key、聊天或记忆。两包哈希见 [SHA256SUMS](SHA256SUMS.txt)。

交付 APK：NetSerial-AI-0.13.0-preview.apk，9,698,481 bytes；SHA-256 a806d323f4f0037aee099b762e5c21e6c98cd6b21fb3dda56105f9623008c345。预览使用 Android Debug 签名，V1/V2 有效；不是生产证书。8 场 UI 矩阵之后补充固定追问上下文、账号脱敏和中文任务范围规则，重跑 435 项 JVM/构建/Lint。上一构建再次通过中文浅色 UI 42 项；前次开发交付时模拟器曾断开；本轮模拟器恢复连接后，最终 preview013 再次安装，8 组合各42项均通过。

入口：连接并登录设备 → 从该终端打开 AI → AI 自动执行 → 描述任务 → 开始。普通聊天仍只给建议。自动任务需要明确选择的厂商/模型，不能稳定返回严格 JSON 的模型会停止，不自动换服务商。Android 6.0 以下禁用加密记忆/API Key 功能，不降级为明文。

## 未验证范围与产品限制

- 没有实付 API / 真实模型联调；不能保证所有上游模型稳定遵循单动作 JSON。现有厂商适配器复用，包括 OpenAI-compatible、Anthropic 和 HTTPS Ollama。
- 没有物理 USB 串口芯片、真实 SSH/Telnet 设备、OEM 手机、Android 21–23 运行验证或生产用户数据迁移。
- 自动配置白名单目前主要为任务内明确指定 VLAN 和受限描述；端口、管理链路、账号、HTTP/HTTPS、安全、保存、未知语法等需要高风险/未知风险确认，不承诺所有交换机命令可自动完成。
- 设备识别基于版本输出的厂商/系列证据，不是硬件身份认证；H3C 未明确识别 Comware 5/7 不猜测 Web 模板。
- 本地 Web 模板可能创建或覆盖管理员/权限，整体确认一次；逐条等待回显，遇错停止。敏感阶段之后输出持续隐藏至重连，结束仅报告已发送和提示符返回，不宣称网页登录、服务可达或完整配置生效。无自动保存/回退。
- 密码输入数组、临时副本及字节尽力清理；Java String、IME/系统副本无法保证物理内存擦除。恶意输入法、被攻陷系统及 Root 不在 UI 屏幕保护的防护能力内。
- 停止不能撤回已经交给传输的当前命令。部分变更需要人工检查后处理。
- GitHub PR/发布、重新下载与 Latest 的完成状态见根版本历史及 Release，源码归档记录本地验证而不预先认证远程状态。真实用户凭据/历史迁移内容未被读取或验证。

English: After the emulator became available, final-source preview installation and all eight UI cases passed again. Production V0.12.0→V0.13.0 upgrade/cold start and original V1/V2/V3 identity passed; real user credentials/history were not inspected. 435 JVM tests passed; Debug build/Lint has 0 errors and 222 warnings. Eight API-34 phone/tablet, zh/en, light/dark UI runs passed 42 assertions each. These are synthetic/local checks, not real provider, OEM or physical-switch acceptance. V0.13.0 production publication was authorized; remote publication verification is recorded in root history. Local Web credentials never enter model requests; sensitive output is withheld until reconnection, so CLI completion is not proof of service reachability or login success.
