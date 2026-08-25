# NetSerial AI V0.9.0 Production Release / 正式版

V0.9.0 turns NetSerial AI into a complete in-app network operations assistant while preserving the rule that AI never sends device commands automatically.

## Highlights / 重点功能

- Full multi-turn AI chat across all 18 configured provider entries, including Zhipu GLM and custom OpenAI-compatible gateways.
- Encrypted local conversation history, device memory, terminal context, cancellation, retry, history management, copy, and redacted sharing.
- OpenAI-compatible, Anthropic Messages, and HTTPS Ollama chat adapters with active-provider-first failover.
- Allow-listed fenced command extraction, deterministic R0–R4 reclassification, and hard blocking of R4 loading from AI.
- One-tap read-only incident evidence plans for H3C, Huawei, Cisco, Ruijie, and generic devices.
- Guided runbooks with prompt matching, timeouts, bounded retry, stop-on-error, and explicit stop conditions.
- Configuration drift severity, expanded security posture checks, and evidence-before-change-closure validation.

## Safety / 安全边界

AI output remains advisory. Every command requires human review and continues through the existing terminal target, maintenance-window, and risk gates. Chat history is encrypted with Android Keystore on Android 6.0+, while Android 5.x never falls back to plaintext history. Terminal context and exported conversations are redacted locally.

## Verification / 验证

- 59 test classes / 181 tests: all passed
- Debug and Release Lint: passed, 0 errors
- Debug and production-signed Release APK: built successfully
- ZIP alignment: passed
- V1/V2/V3 signatures: verified
- APK SHA-256: `b4c4040cfc2f1895404dd0c9b6f8024f3824f4a1baf161621e132e504ee29697`

Real Android devices, USB adapters, target switch versions, and owned AI accounts still require authorized field acceptance before production use.
