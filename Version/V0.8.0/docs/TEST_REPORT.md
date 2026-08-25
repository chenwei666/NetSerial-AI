# V0.8.0 测试报告 / Test Report

- 测试日期 / Date: 2026-08-25
- 测试版本 / Version: 0.8.0 (`versionCode 9`)
- 应用包名 / Package: `com.chenwei666.netserial`
- Android SDK: minSdk 21, targetSdk 36, compileSdk 36
- 构建环境 / Build root: `C:\tmp\NetSerial-v080-build`

## 测试结论 / Result

V0.8.0 候选版本通过自动化单元测试、Debug/Release Lint、Debug/Release 构建、APK 对齐与生产签名校验，可以进入设备验收与发布流程。

The V0.8.0 release candidate passed automated unit tests, Debug/Release lint, Debug/Release builds, ZIP alignment, and production-signature verification. It is ready for device acceptance testing and release preparation.

| 检查项 / Check | 结果 / Result |
| --- | --- |
| JVM unit tests | 50 classes, 163 tests, 0 failures, 0 errors, 0 skipped |
| Debug Lint | Passed |
| Release Lint | Passed |
| Debug APK build | Passed |
| Release APK build | Passed |
| ZIP alignment | Passed (`zipalign -c 4`) |
| APK signature | V1/V2/V3 verified |

## 新增覆盖 / New Coverage

- 18 个 AI 厂商预设、默认端点、认证方式及模型目录格式。
- OpenAI-compatible、Anthropic 与 Ollama 上游模型列表解析。
- 模型列表端点推导、Qwen 专用目录格式、缓存作用域隔离与异步回调代次失效逻辑。
- 批量端口表达式的单端口、列表、范围、去重、非法输入与上限边界。
- TFTP 临时服务器连续 10 次真实 UDP 传输/关闭回归测试。

## 缺陷回归 / Regression

完整测试首次运行暴露了 Windows 下 TFTP 临时服务器关闭后文件句柄偶发未及时释放的问题。根因是关闭流程只终止监听套接字，未同步关闭活动传输套接字并等待工作线程退出。修复后，服务器会跟踪并关闭活动连接、停止执行器并等待终止；连续 10 次真实 UDP 传输回归通过。

## APK 验证 / APK Verification

- 文件 / File: `NetSerial-AI-v0.8.0-release.apk`
- 大小 / Size: 6,222,222 bytes
- SHA-256: `d853750b13919992103f4532a04b816de8947aabcf187c0c1b669d24da329eef`
- 签名算法 / Signature: RSA 4096, SHA256withRSA
- 证书 SHA-256 / Certificate SHA-256: `6f6d2063a155a9d252eecf4a84df31281b02d86beb2f0cd55ea7c80a2063f5bd`

## 尚需人工验收 / Manual Acceptance Remaining

当前环境没有连接真实 Android 手机、USB 串口设备、交换机或全部付费 AI 厂商账号，因此未执行以下现场测试：USB OTG 驱动兼容性、真实 SSH/Telnet 会话、厂商设备命令回显、各厂商账户权限/限流差异、不同屏幕尺寸下的最终视觉验收。发布前建议至少用一台 Android 8 和一台 Android 14+ 设备完成冒烟测试。
