# NetSerial AI V0.8.0 Release Candidate

V0.8.0 focuses on a modernized operations workflow: broader AI connectivity, upstream model discovery, clearer network diagnostics, and safer background-task handling.

## 中文亮点

- AI 厂商扩展至 18 个，新增智谱 GLM、腾讯混元、百度千帆、月之暗面 Kimi、MiniMax、SiliconFlow、Groq、Mistral、xAI、OpenRouter、OpenAI-compatible 与 Ollama 等。
- 模型不再依赖应用内硬编码清单：可从厂商 API 上游刷新，支持本地缓存、缓存回退、取消请求和手动输入模型。
- AI 设置页重新设计为现代化分区卡片，厂商、模型、凭据、连接状态更直观。
- 网络工具箱重做为“快速诊断—结果—CIDR—离线工具”的任务流，支持批量端口探测、常用端口、地址属性分析、结果复制与分享。
- 首页和功能中心使用统一现代卡片与分类图标；中英文文案同步更新。
- 修复 TFTP 临时服务器关闭时的资源释放竞态。

## English Highlights

- Expanded the AI provider catalog to 18 entries, including Zhipu GLM and major global and Chinese providers.
- Added upstream model discovery with local cache fallback, cancellation, manual model entry, and provider-specific model-list parsing.
- Redesigned AI settings and the network toolbox around clear task-focused Material cards.
- Added bounded multi-port probing, common-port presets, address classification, and copy/share actions.
- Unified the home and feature-hub presentation and completed bilingual UI copy.
- Fixed a TFTP shutdown race that could temporarily retain a file handle on Windows tests.

## Verification

- 50 test classes / 163 tests / 0 failures
- Debug and Release Lint passed
- Debug and production-signed Release builds passed
- V1/V2/V3 signatures and ZIP alignment verified
- SHA-256: `d853750b13919992103f4532a04b816de8947aabcf187c0c1b669d24da329eef`

## Compatibility and Notes

- Android 5.0+ (`minSdk 21`), targetSdk 36.
- Existing AI profiles remain compatible. Model refresh requires network access and a valid provider credential when the upstream API requires authentication.
- This is a release-candidate document. The APK must be attached to GitHub Release and downloaded once for final checksum verification before marking the release as stable.
