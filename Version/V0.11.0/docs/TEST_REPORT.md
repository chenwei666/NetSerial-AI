# V0.11.0 本地预览测试报告

日期：2026-10-14。维护者：chenwei666。源码：`Version/V0.11.0`；versionCode 13；独立测试包：`com.chenwei666.netserial.preview011`。

## 结论

AI 页面、原生回答排版与确认式加密记忆已实现并通过本地验证。本次是 Debug 预览交付，不是生产签名或 GitHub 发布；未调用真实 AI 服务、未读取真实账号密钥、未连接或下发交换机命令。V0.10.0 源码和正式安装保持不变。

## 可重复验证

- 构建：`scripts/build.ps1 -Tasks @('testDebugUnitTest','lintDebug','assembleDebug','assembleDebugAndroidTest')`。ASCII 镜像 `C:/tmp/NetSerial-v0110-build`，JDK 17、compileSdk 36。
- 67 个 JVM 测试类、275 项测试，0 失败、0 错误；其中新增 Markdown 13 项、确认记忆16项。覆盖渲染边界、真实代码符号保留、范围隔离、到期、容量、秘密拒绝、损坏数据失败关闭、并发写入及删除后不复活。
- Debug Lint：0 错误、169 警告。没有把已有警告当作已清零；主要仍是现有工程静态提示。新聊天布局存在可接受的背景 overdraw 提示。
- API 34 模拟器：1600×900/240dpi 平板窗口与600×1100/240dpi手机窗口。手机中英文 × LIGHT/DARK 四组合，每轮66断言通过；测试结束恢复屏幕尺寸、应用偏好和应用语言。
- 真正启动 `AiCopilotActivity`；检查上下文折叠/展开、聊天空间、设置齿轮可见且可打开厂商设置、更多菜单文字可访问。
- 合成中英回答通过生产渲染器显示；验证实际粗体 span、围栏代码中的星号与尾换行；导出应用自身 View 绘制供视觉复核，没有取消安全窗口标记或截取其他应用。
- 实际显示软键盘，通过 IME insets 确认；输入框、发送按钮仍在可见区域，聊天区高度非零。
- 真正点击记忆确认保存，使用唯一合成内容；新存储实例/控制器解密重读、密文不含原文、编辑重读、删除重读。取消不写入、不触发保存回调；每轮仅清理本轮合成记录，不清空应用数据。

设备测试示例：

```text
adb shell am instrument -w -e appearance LIGHT -e language zh -e screenshot true com.chenwei666.netserial.preview011.test/de.kai_morich.simple_usb_terminal.AiChatUiInstrumentation
```

须查看输出 `PASS` 或 `FAIL`，不能仅依赖 adb 退出码。Android 13+ 测试同时配置应用自身 LocaleManager 状态，模拟真实设置页的操作，不更改设备全局语言。

## 已修正的问题

最终产物：`artifacts/V0.11.0/NetSerial-AI-0.11.0-preview.apk`，9,458,253 bytes，SHA-256 `433883532afb2533d16974e94f1c7246f28437c83c2dd26e25935691ff26463a`。已用包元数据确认 `.preview011`、versionCode 13、versionName `0.11.0-preview`；最终文案精简后重新构建，并对最终 APK 重跑手机四组合，每轮66断言再次通过。预览使用 Debug 签名，不用于覆盖正式版。

- 原回复直接 `setText`，导致 Markdown 标记裸露：改为有界原生块和文本 span，不直接删除代码中的星号。
- AI 工具栏强制深色覆盖层与页面主题不一致：移除覆盖层、按主题给图标着色，设置使用 always-visible action。
- 初次整合有3处非传递 R 属性错误：改为实际定义属性的 AppCompat R，重建成功。
- 初次英文测试只修改应用偏好、未同步 Android 13+ 应用语言：修正测试准备/恢复逻辑，保留对真实 Activity 语言的严格断言，矩阵重跑通过。

## 未验证和限制

- 没有生产签名、覆盖正式版升级、GitHub 发布或实机安装验收；预览不迁移正式版数据。
- 无付费/真实 AI 请求，因此不证明各厂商线上接口、账号、模型返回或计费成功。
- 设备记忆按档案名称匹配，不是硬件身份；默认未命名设备和新手入口只支持个人偏好。改名/同名行为见 SECURITY。
- Markdown 是安全子集，不支持全部 CommonMark/复杂表格和嵌套；未知语法保留文本，无 HTML 执行、远程图片加载或自动链接跳转。
- API 21–22 无明文记忆回退；本次动态验证仅 API34，其他 Android 版本、全部字号与主题颜色尚未遍历。
- 秘密检测是尽力规则，不能识别任何形式的秘密。删除本地记录不能撤回已发送给服务商的数据。
- 记忆最近请求快照仅在当前界面实例有效；不代表上游成功收到，不是持久审计日志。
