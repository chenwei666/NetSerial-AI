# NetSerial AI V0.7.0 开发交接

日期：2026-08-24  
分支：`codex/feature/v0.7.0`  
基线：`origin/main` / `feda18149adf8e8666ff5bfc591186d004dc82ef`

## 已完成

- 五区手机/平板导航、功能搜索、收藏和最近使用。
- AI 厂商失败转移、健康/端口/拓扑/备份/SNMPv3 计划。
- SSH 凭据别名接线、临时 HTTP/TFTP、签名运行手册。
- 受控批次 R0-R4 风险传递、逐阶段授权、回退预授权、失败即停。
- 中英文 README、CHANGELOG、架构、安全、范围与测试报告。

## 自动化证据

- clean 构建命令：`gradlew --no-daemon clean testDebugUnitTest lintDebug assembleDebug`
- 结果：48 个测试类、147 个测试、0 failure、0 error；Lint 通过；Debug APK 构建通过。
- 包：`com.chenwei666.netserial`，versionCode 8，versionName 0.7.0，minSdk 21，targetSdk 36。
- 本地测试包：`Version/V0.7.0/artifacts/NetSerial-AI-v0.7.0-debug.apk`（按仓库规范不提交 Git）。
- SHA-256：`4ee5de3a4b1f9540ac9e7dceb3fafbf73f124b41524d243a5605f1a81491f2a2`。

## 仍需外部授权环境

- 真实 Android 手机/平板与 USB OTG 芯片。
- H3C/Huawei/Cisco/Ruijie、SSH/Telnet/SFTP、HTTP/TFTP 和 SNMPv3 Agent。
- 真实 AI 厂商限流/超时/费用/失败转移。
- 使用历史生产证书构建 Release APK，并完成覆盖安装与证书链核对。

## 下一动作

1. 在实验网络执行 `docs/TEST_REPORT.md` 的现场矩阵。
2. 现场通过后再由生产签名工作站执行 `scripts/build.ps1 -Release`。
3. 用户明确授权后再推送分支、创建/合并 PR 或发布 GitHub Release；当前未执行远程发布。
