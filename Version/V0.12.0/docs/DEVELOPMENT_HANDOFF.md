# V0.12.0 正式构建交付

- 维护者：chenwei666；日期：2026-10-14。
- 独立源码：`Version/V0.12.0`；不覆盖 V0.11.0 及既有归档。
- 本地预览 APK：`artifacts/V0.12.0/NetSerial-AI-0.12.0-preview.apk`。
- `.preview012`，versionCode 14，versionName `0.12.0-preview`，Debug 签名；不替代正式安装、不迁移正式版密钥和历史。
- 已完成工具箱 16 入口、真实 Ping 数值、免 Root TTL/DF 探测与明确降级、协议检测、离线辅助、分类搜索和手机/平板布局；保留确认式 AI 记忆。
- 338 JVM 测试通过；Lint 0 Error/218 Warning；手机平板 8 组合各 138 断言，AI 回归 64 断言。完整范围、可重复命令及现场待验证项见 `TEST_REPORT.md`。
- 用户已授权 GitHub 更新和正式发布。正式包 `artifacts/V0.12.0/NetSerial-AI-0.12.0.apk`，6,506,868 bytes，SHA-256 `fce9f7d5a4a60fe16d72ab357dabfbd769347767549b22075a6b8d6325f19cf8`；V1/V2/V3 签名、对齐、包元数据和干净模拟器覆盖升级通过。远程发布状态与下载核验记录在根版本历史，不能由本地构建结论推断已上传。
- 不应把模拟器回环成功等同不同 Android 厂商设备、真实网络路径或 WOL 唤醒已经验证。
