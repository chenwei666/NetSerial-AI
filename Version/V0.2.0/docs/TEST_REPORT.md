# V0.2.0 测试报告

- 日期：2026-08-21
- 测试人员：chenwei666
- 环境：Windows、JDK 17、Android SDK Platform 36、Build Tools 36.0.0
- 构建目录：`C:\tmp\NetSerial-build`

## 自动化结果

| 门禁 | 结果 |
|---|---|
| `testDebugUnitTest` | 54 项通过，0 失败，0 错误 |
| `lintDebug` | 通过，0 阻断错误 |
| `assembleDebug` | 通过 |
| `git diff --check` | 待最终提交前复核 |
| 敏感信息与发布标识扫描 | 待最终提交前复核 |

覆盖内容包括：

- 四厂商、三种 CLI 模式的离线命令候选和模式隔离；
- TAB、ESC、Ctrl、方向键、退格、删除、问号和管道符字节编码；
- 终端缓冲淘汰、ANSI 清洗和密码/Token/Bearer 脱敏；
- R1-R4 本地风险升级及 AI 不可降级；
- 结构化计划解析、多行命令拒绝、无效/超大响应拒绝；
- HTTPS Endpoint、重定向、超时、取消、错误分类和请求头注入防护；
- Keystore 抽象层的密文隔离、别名认证、异常路径清零和删除；
- AI 配置 JSON、上限、活动配置、损坏文档和未知供应商拒绝；
- 设备档案校验；
- 本地记忆的作用域、过期、密钥拒存和安全导入替换。

## APK

- applicationId：`com.chenwei666.netserial`
- versionName/versionCode：`0.2.0` / `2`
- minSdk/targetSdk：`21` / `36`
- 产物：`C:\tmp\NetSerial-build\app\build\outputs\apk\debug\app-debug.apk`
- SHA-256：`da90b03a06362a70e0278eeaa74ada01b9d1a4ea53246652a1859e0d9151061f`
- 签名：Debug，仅供开发验证

## 未执行的外部验收

- 未连接真实 Android 手机，因此未验证安装、旋转、后台恢复和系统语言切换。
- 未连接 FTDI、CP210x、CH340/341、PL2303 或 CDC ACM 线缆。
- 未连接真实 H3C、华为、Cisco、锐捷交换机，因此命令语法和 TAB 回显仍需按型号/系统版本验收。
- 未提供真实 AI Key，因此未调用任何云端 API，也未验证实际额度、代理、区域和账户权限。
- 未配置正式签名，因此未执行 Release、升级覆盖和应用商店验收。

这些项目属于硬件、账号和发布凭据依赖，不应由自动化结果替代或伪报为完成。
