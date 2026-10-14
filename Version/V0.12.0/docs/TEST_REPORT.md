# V0.12.0 正式版与预览验证报告

日期：2026-10-14。维护者：chenwei666。独立源码 `Version/V0.12.0`；versionCode 14；预览包 `com.chenwei666.netserial.preview012`，versionName `0.12.0-preview`。

## 结果

已完成免 Root 网络工具箱修复、16 工具入口与手机/平板布局。预览验证后，用户明确授权 GitHub 更新和正式发布，现已完成下述生产构建门禁。V0.11.0 和旧版本源码不覆盖；远程发布和重新下载证据单独记录于仓库根版本历史。

### 正式构建验收（2026-10-14）

- `scripts/build.ps1 -Release` 成功；338 项单元测试通过，Release Lint 0 错误/218 警告。
- `NetSerial-AI-0.12.0.apk`：6,506,868 bytes；SHA-256 `fce9f7d5a4a60fe16d72ab357dabfbd769347767549b22075a6b8d6325f19cf8`。
- applicationId `com.chenwei666.netserial`、versionCode 14、versionName `0.12.0`，minSdk 21/targetSdk 36；release Manifest 无 debuggable/testOnly/instrumentation。
- V1/V2/V3 签名验证、ZIP 4-byte 对齐通过；证书 SHA-256 `6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`，与重新下载且核对 APK 哈希的 V0.10.0 正式资产一致。
- API 34 干净模拟器安装公开 V0.10.0（code 12），再使用 `install -r` 升级到 V0.12.0（code 14），安装成功且 firstInstallTime 保留。正式包冷启动成功，AI 聊天/设置/记忆与新工具目录入口正常。该测试不读取或证明真实用户数据迁移。
- 追加的正式包 UI Ping 复测没有完成：随后模拟器 `system_server`（PID 342）处于 zombie 状态，crash buffer 记录 crash_dump helper 失败，UI 采集阻塞。本应用已读取日志中没有对应 Java fatal 记录，但不能据此排除所有应用问题或宣称该复测通过。只终止本任务的阻塞采集，未重启设备或清除数据；已完成的升级/入口检查与前轮预览真实回环测试单独计入，不扩大为正式包 Ping 此轮验证。

### 预览功能回归

- 72 个 JVM 测试类、338 项测试：0 失败、0 错误。
- Debug Lint：0 错误、218 警告。保留已有工程及新增静态提示，未隐藏或声称全部清零。
- API 34 模拟器，240dpi：手机 600×1100、平板 1600×900，各自中文/英文 × 浅色/深色，共 8 组合，全部 PASS，每轮 138 断言。
- AI 手机中文深色回归：64 断言 PASS；真实 Markdown 渲染、设置入口、软键盘适配、确认/取消记忆、加密重读、编辑和删除。未调用 AI；合成记忆已清理。
- APK 已成功安装于隔离模拟器；包信息与 Debug V1/V2 签名验证通过。不声称有生产 V3 签名或正式版覆盖升级测试。

## 问题与回归证据

原路由追踪依赖当前 App 无权限使用的系统 traceroute 路径；新实现使用有限次系统 Ping TTL，不申请 Root、不尝试提权。解析器/执行器测试验证权限错误是 UNSUPPORTED、未知跳不填虚假地址或 RTT。应用沙箱内真实回环 TTL 到达目标且显示真实 RTT，但没有因此宣称公网中间路由器都会返回逐跳信息。

Ping 从实际 ICMP 回包解析数值，而非使用进程运行耗时；丢包为缺失样本，不伪造 0ms。测试覆盖 time=、time<、异常文本、丢包、统计、进度和取消；设备测试通过真实 UI 点击开始、等待数值与趋势出现。

MTU 只使用 IPv4 DF 证据：普通丢包不作为报文过大依据；1500 字节探测成功标为下限而非精确上限。测试覆盖明确分片错误、超时、不支持与 IPv6 拒绝；模拟器实际 DF 探测通过。

基础体检对字面 IP 跳过 DNS 并明确说明；本地监听端口验证整条链路。总时限与用户取消保持区别。TCP 实际开放/拒绝，HTTP HEAD 的 200/302 与不跟随跳转，均由回环测试服务验证。

## 其他覆盖

- DNS 七种记录、事务 ID/问题校验、压缩指针、截断后同服务器 TCP 回退、500 个固定种子随机数据帧；本机 UDP/TCP 测试，不查询公网。
- HTTP 仅 HEAD、无 Cookie/Auth、不下载正文、不自动跳转；响应白名单不导出 Set-Cookie 和请求查询参数。
- TLS 参数保留系统信任和 HTTPS 主机名验证，不允许明文降级；未使用真实公网证书服务器做成功连接验证。
- WOL 校验 102 字节格式，未实际广播或唤醒设备。
- 发现功能拒绝公网和过大网段，私网范围最多 64 地址、4 端口；没有扫描任何真实局域网。
- IPv4/IPv6 子网、连续掩码、/31 和 /32、无效地址提取、MAC 本地随机地址与有限 OUI 数据说明。
- 16 工具入口、搜索、对应表单、取消后旧任务不得覆盖新结果、网络公开 API、当前设置读取/恢复。
- 平板参数/结果双栏、手机纵向、目录自适应；中文和英文浅/深色截图已人工查看。图片仅用本应用 View 绘制，不解除 FLAG_SECURE，不截取其他应用。

## 可重复命令

```powershell
& .\scripts\build.ps1 -Tasks @('testDebugUnitTest','lintDebug','assembleDebug','assembleDebugAndroidTest')
# 安装生成的预览 APK 和 androidTest APK 后，保存合成报告与图片：
& .\scripts\test-toolbox-device.ps1 -Serial emulator-5554 -OutputDirectory '<absolute-output-directory>'

# 独立 AI 回归测试包；不改变主应用生产逻辑：
& .\scripts\build.ps1 -Tasks @('assembleDebugAndroidTest','-PtestRunner=de.kai_morich.simple_usb_terminal.AiChatUiInstrumentation')
```

工具箱 runner：`de.kai_morich.simple_usb_terminal.ToolboxInstrumentation`；AI runner 同名专用类。查看输出 PASS/FAIL，不只依赖 adb 退出码。测试恢复应用语言、外观、超时和模拟器屏幕尺寸；网络仅访问回环地址。

## 产物与未验证范围

`artifacts/V0.12.0/NetSerial-AI-0.12.0-preview.apk`：9,612,457 bytes；SHA-256 `834938dbf59ebf5b42c8ae8baaa72d89bb09818843bc3fde5dca8794c9d2f05d`。归档 APK 与实际安装测试的构建 APK 哈希相同。构建时 482 个源码及 app Gradle 文件与 ASCII 镜像一致；之后仅修正 AI 测试日志版本标签及文档，不修改 APK 代码。

尚未验证 Android 21–23 实机、多厂商手机/平板、真实交换机跨网段路径、实际 WOL 唤醒、LAN 资产发现、有效公网 TLS 连接、外部 AI 与真实用户数据迁移。生产覆盖升级已在上述干净模拟器验证。系统可能限制 Ping/TTL/DF，应用会明确说明；TCP 可用于补充证据但不冒充 Ping 或完整路由追踪。低版本网络信息按能力降级，不申请 Root、VPN、定位或 Wi-Fi 标识权限。

本次不包含抓包、网络测速服务器、SNMP 管理和完整 OUI 数据库。仅交付已约定且有明确边界的常用工具，不声称涵盖所有网络运维协议。
