# 安卓串口运维终端：APK 分析与功能补全方案

- 分析日期：2026-08-21
- 分析人：chenwei666
- 分析对象：`汉Serial USB Terminal_1.57.apk`
- 分析方式：APK 静态分析、签名校验、资源与 DEX 结构检查、公开上游源码对照
- 未执行事项：未安装 APK、未连接 USB 串口设备、未连接交换机、未读取任何设备或数据库数据

> AI、多供应商 API、结构化记忆、TAB 补全和完整产品路线已经扩展为独立方案：
> [FULL_AI_FEATURE_COMPLETION_PLAN.md](FULL_AI_FEATURE_COMPLETION_PLAN.md)

## 1. 结论摘要

该 APK 的串口基础能力较完整，适合作为“通用串口收发工具”，但离“交换机现场运维终端”还有明显距离。它已经具备 USB 串口驱动、常用串口参数、流控、控制线、宏、日志、文件上传等能力；最需要补的不是再做一个发送框，而是终端交互、厂商配置模板、会话留痕、安全保护和自动化流程。

不建议直接反编译并修改这份 APK：

1. 文件使用 Android 通用调试证书重新签名，不是适合生产发布的签名。
2. 界面资源中存在“Cancel / Protocol / File / Select / Refresh”等中英混杂，且所有语言环境的应用名都被替换为中文，符合第三方汉化或重打包特征。
3. 商业版完整源码并未公开；直接修改和分发反编译产物存在知识产权、升级和安全审计风险。
4. 作者公开了 MIT 许可的 `SimpleUsbTerminal` 简化源码，可作为合法、可审计的开发基线。

推荐新建独立应用，例如“NetSerial 运维终端”，基于公开源码和 `usb-serial-for-android` 重构，不沿用原包名，不覆盖原应用，使用自有生产签名和可复现构建。

## 2. APK 基本信息

| 项目 | 静态分析结果 |
|---|---|
| 包名 | `de.kai_morich.serial_usb_terminal` |
| 版本 | `1.57`，versionCode `102` |
| Android 范围 | minSdk 21，targetSdk 36，compileSdk 36 |
| 文件大小 | 2,633,260 字节 |
| SHA-256 | `2fc7a44bfc5ab58d02afad106ec3b50b749ba3d9f81129946a424ce433f4f9e8` |
| DEX/Native | 单个 `classes.dex`；无原生 `.so` |
| 主界面 | `MainActivity`，响应 USB 设备接入事件 |
| 后台能力 | `SerialService` 前台服务，可在切屏、旋转、后台时维持接收 |
| 签名 | v1、v2 校验通过；证书为 Android 通用调试证书；无 v3/v4、无 SourceStamp |
| 网络权限 | `INTERNET`、`ACCESS_NETWORK_STATE`；还包含 Google Play Billing 和 Data Transport 组件 |
| USB Host 声明 | Manifest 未声明 `android.hardware.usb.host` feature，但代码使用 USB Host API |

签名通过只表示文件自签名后未再次损坏，不代表它来自原作者。调试证书不应作为企业生产分发证书。

## 3. 已确认的现有功能

### 3.1 USB 串口连接

- 自动响应 USB 设备插入并申请权限。
- 支持 FTDI、Prolific PL2303、Silabs CP210x、Qinheng CH340/CH341 和 USB CDC 类设备。
- 设备列表显示 Vendor ID、Product ID、厂商名、产品名、驱动和端口信息。
- 支持自定义/手动驱动映射的资源与逻辑入口。
- 前台服务维持串口连接和接收缓冲。

### 3.2 串口参数

- 波特率：2400、9600、19200、38400、57600、115200 和自定义值。
- 数据位：5、6、7、8。
- 校验：无、奇、偶。底层驱动还定义 Mark/Space，但当前 UI 未开放。
- 停止位：1、1.5、2。
- 流控：无、RTS/CTS、DTR/DSR、XON/XOFF。
- 显示控制线；支持设置 RTS、DTR 和发送 BREAK，BREAK 时长可配置。

### 3.3 收发与终端显示

- 文本和十六进制发送。
- 接收显示模式：文本、终端、十六进制。
- 接收换行：CR+LF、CR、LF、STX/ETX、NUL、无。
- 发送换行：自动、CR+LF、CR、LF、STX/ETX、NUL、无。
- 字符集、字体、字号、自动滚动、连接消息、时间戳和时间戳格式。
- 发送本地回显、发送后清空输入。
- 字符延迟、行延迟，适合慢速设备和多行命令。
- 接收缓冲区：2 KB、10 KB、50 KB、200 KB、无限制。

### 3.4 宏与文件

- 0 至 5 行宏按钮。
- 宏支持名称和值。
- 宏编辑模式：文本、十六进制、多行文本。
- 宏支持发送或插入、按次数重复或无限重复、重复延迟、多行行延迟。
- “发送但不附加换行”控件在 1.57 布局中存在，但被隐藏；公开新版 1.58 才正式开放。
- 保存、分享、数据日志、配置导入/导出/重置。
- 文件上传：原始数据、行模式、XMODEM、XMODEM CRC、XMODEM 1K。

### 3.5 当前局限

- 主输入区是单行 `EditText + Send`，缺少面向网络 CLI 的专用键盘和会话交互。
- 没有厂商/型号配置档案、命令库、提示符识别、运维任务流程。
- 没有看到多会话、多端口并行、结构化会话归档或敏感内容脱敏能力。
- “无限制”显示缓冲可能造成长时间会话内存增长；需要环形缓冲和流式日志。
- 汉化不完整，部分资源直接硬编码英文；翻译值与内部枚举混用会增加升级和语言切换风险。
- Manifest 未显式关闭备份；宏、历史和会话内容需要更明确的数据保护策略。
- 未发现 APK 中硬编码的业务 HTTP URL，但这不能证明运行时完全不会联网；该重签名包仍不宜用于输入生产设备口令。

## 4. 面向交换机运维的补全优先级

### P0：可投入现场使用的最小版本

1. **可信构建与安全基线**
   - 基于 MIT 开源项目重新开发，独立包名和生产签名。
   - 默认移除 Billing、Google Data Transport 和互联网权限；需要联网能力时按模块显式开启。
   - `allowBackup=false`，日志默认保存在应用私有目录；用户主动导出时再使用系统文件选择器。
   - 不保存口令；日志显示和导出支持密码输入段、SNMP community、密钥等敏感内容脱敏。
   - 建立 SBOM、依赖锁定、静态扫描和签名校验流程。

2. **真正可用的网络设备终端**
   - VT100/ANSI 基础终端解析：光标移动、清屏、擦除行、颜色、退格、CR/LF。
   - 运维快捷键栏：`Esc`、`Tab`、`Ctrl`、方向键、Backspace、Delete、空格、`?`、`|`。
   - `Ctrl+A` 至 `Ctrl+Z` 组合键和外接键盘映射。
   - 长按连续发送、按键可编辑、横屏和分屏适配。
   - 自定义高性能终端 View，环形显示缓冲与磁盘流式日志分离。

3. **连接配置档案**
   - 用户可建立品牌、型号、现场、线缆、USB VID/PID、端口号和串口参数档案。
   - 内置常见参数模板，但所有值都允许修改，避免把“某品牌一定是 9600 8N1”写死。
   - 同一 USB 线缆记忆上次配置；未知设备提供驱动和参数向导。
   - 波特率探测只做“候选值轮询 + 可打印字符/提示符评分”，明确标记为启发式，不宣称可靠自动识别。

4. **会话与日志**
   - 会话开始/结束、设备档案、串口参数、收发方向、毫秒时间戳、断线事件。
   - 全文搜索、复制、书签、重要行标记、只看发送/只看接收。
   - 导出 TXT、带元数据的 JSON 和脱敏报告。
   - 单次会话大小、保留天数和磁盘空间上限；异常退出后可恢复未完成会话。

5. **完整中文化**
   - `values/` 与 `values-zh-rCN/` 分离显示文本和内部值。
   - 消除所有硬编码字符串，中文术语统一，保留英文可选界面。

### P1：显著提升运维效率

1. **厂商命令库和可编辑宏组**
   - 华为、H3C、Cisco、锐捷等按厂商/系统/模式组织，但命令内容由用户审核后启用。
   - 支持变量占位符，例如 VLAN、接口、IP、描述；发送前预览实际命令。
   - 危险级别：只读、配置变更、高风险。配置变更默认二次确认。
   - 命令历史、收藏、最近使用和二维码/文件导入模板。

2. **安全自动化步骤引擎**
   - 有限步骤：`Send`、`WaitForPrompt`、`Delay`、`Confirm`、`Capture`、`Assert`、`StopOnError`。
   - 支持提示符正则、分页提示识别、超时、取消和逐步执行。
   - 不执行 Android Shell，不允许模板绕过应用权限，不在模板中保存口令。

3. **文件传输增强**
   - 保留 Raw、Line、XMODEM 发送。
   - 补充 XMODEM 接收，并评估 YMODEM/ZMODEM；传输前后校验大小与 SHA-256。
   - 固件上传属于高风险流程，必须有设备电量、串口参数、文件哈希和中断风险提示。

4. **多会话与多端口**
   - 多端口 USB 串口设备选择。
   - 多会话标签页、独立参数和日志；后台会话状态清晰可见。

### P2：可选扩展，不应阻塞串口主线

- SSH 终端作为独立模块；主机指纹必须校验，凭据使用 Android Keystore。
- Telnet 默认禁用并明确标注不安全，仅在隔离管理网显式启用。
- 工单/资产系统导出接口、二维码资产标签、团队模板签名与分发。
- 任何云同步、CMDB 或数据库接入都应另行授权、最小权限并经过数据分类评审。

## 5. 推荐技术架构

```text
app-ui
  ├─ 设备与连接向导
  ├─ 终端会话与快捷键
  ├─ 配置档案/命令库/日志
  └─ 设置与安全中心

core-serial
  ├─ USB 发现与权限
  ├─ Driver/Port 适配
  ├─ 串口参数与控制线
  └─ 前台服务、重连、收发队列

core-terminal
  ├─ ANSI/VT 状态机
  ├─ 编码与换行
  ├─ 环形屏幕缓冲
  └─ 特殊键编码

core-automation
  ├─ 宏与变量
  ├─ Prompt/分页识别
  ├─ 有限步骤执行器
  └─ 风险确认与审计

data-security
  ├─ Room/DataStore
  ├─ 私有日志与导出
  ├─ 脱敏规则
  └─ Keystore（仅可选凭据）
```

建议使用 Kotlin、Coroutines/Flow、ViewModel、Room/DataStore 和 `usb-serial-for-android`。终端正文使用专用 View 和受控缓冲，不用不断向普通 TextView 追加无限文本。UI 可使用 Compose，但高频终端渲染层应独立，避免把串口接收、解析和界面线程耦合。

## 6. 核心数据模型

```text
ConsoleProfile
  id, name, vendor, modelPattern, usbVid, usbPid, port
  baudRate, dataBits, parity, stopBits, flowControl
  charset, sendNewline, terminalType, promptRegex

CommandTemplate
  id, profileId, name, mode, template, variables
  riskLevel, appendNewline, waitForPrompt, timeoutMs

Session
  id, profileSnapshot, startedAt, endedAt, result
  logPath, byteCountRx, byteCountTx, disconnectReason

AutomationPlan
  id, name, profileId, steps[], version, checksum
```

档案必须保存连接时的快照，后续修改模板不能篡改历史会话记录。

## 7. 版本里程碑

### V0.1.0：可信基线

- 导入并审计 MIT 开源基线。
- 独立包名、中文/英文资源、自有图标、生产签名配置。
- 移除非必要联网和支付组件。
- 建立单元测试、Lint、依赖扫描、可复现 Debug/Release 构建。

### V0.2.0：网络终端核心

- 完整串口参数、控制线、断线恢复。
- ANSI/VT 基础解析、快捷键栏、外接键盘。
- 高性能环形缓冲和流式日志。
- 设备/线缆连接档案。

### V0.3.0：运维效率

- 命令库、宏组、变量预览、风险确认。
- 搜索、书签、结构化导出与脱敏。
- Prompt 等待和分页处理的有限自动化。

### V0.4.0：文件与多会话

- XMODEM 收发与传输校验。
- 多端口、多会话、后台状态管理。

### V1.0.0：生产验收

- 真机、线缆、品牌矩阵验收。
- 安全审查、性能长稳、异常恢复、升级/回滚文档。
- 完整 README、CHANGELOG、测试报告、SBOM、签名和独立 `Version/V1.0.0` 归档。

每个正式版本均创建不可覆盖的 `Version/Vx.y.z` 完整归档，并按企业规范记录修改文件、影响模块、接口/配置兼容性、升级方式和已知问题。

## 8. 测试与验收矩阵

| 类别 | 最低验收范围 |
|---|---|
| Android | 至少 Android 8、10、12、14、16 真机；如需兼容 Android 5 再单独保留 minSdk 21 |
| USB 芯片 | FTDI、CH340/341、CP210x、PL2303、CDC ACM |
| 物理接口 | USB-C OTG、USB-A OTG、常用 USB 转 RS232 控制台线 |
| 串口参数 | 9600/115200、自定义波特率、5-8 数据位、奇偶校验、1/1.5/2 停止位、各类流控 |
| 交换机 | 用户实际使用的华为、H3C、Cisco、锐捷等具体型号，每品牌至少一台 |
| 异常 | 拒绝权限、拔线、重插、旋转、锁屏、后台、低电量、存储空间不足、发送超时 |
| 性能 | 115200 持续收发、10 MB 以上日志、长时间会话、无限制显示配置保护 |
| 安全 | 无硬编码密钥、无默认联网、日志脱敏、备份策略、签名校验、依赖漏洞和 SBOM |

Android 不是实时操作系统，高波特率连续数据仍可能受调度和垃圾回收影响。验收必须同时统计串口层字节数、日志字节数和设备端发送计数，不能只看界面是否“看起来完整”。

## 9. 开发前需要确认的业务输入

开始编码前需要确定以下信息，以便选择 V0.2.0 的真实验收目标：

1. 你最常接触的交换机品牌、系统版本和具体型号。
2. 现有 USB 转串口线的芯片、VID/PID，以及手机/平板型号和 Android 版本。
3. 必须优先实现的五个现场动作，例如查看配置、备份配置、批量执行只读命令、BootROM 传文件、日志留痕。
4. 是否允许保存命令历史；是否必须禁止保存口令；日志需要保存多久。
5. 仅内部 APK 分发，还是计划公开发布；这会影响签名、隐私政策和许可证文档。

## 10. 公开基线资料

- 上游简化源码：https://github.com/kai-morich/SimpleUsbTerminal
- USB 串口驱动：https://github.com/mik3y/usb-serial-for-android
- 原应用 Google Play 页面：https://play.google.com/store/apps/details?id=de.kai_morich.serial_usb_terminal

本报告的 APK 功能结论以本地 1.57 文件静态分析为准；公开源码仅用于确认可采用的合法开发基线，不把简化源码中不存在的功能误认为商业版实现。
