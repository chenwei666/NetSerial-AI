# AI 厂商兼容性 / AI Provider Compatibility

## 已实现

| 类型 | 厂商 | 协议与鉴权 |
|---|---|---|
| OpenAI-compatible | OpenAI、Gemini、DeepSeek、Qwen、Kimi、自定义网关 | `POST /chat/completions`，Bearer |
| Native | Claude / Anthropic | `POST /v1/messages`，`x-api-key` + `anthropic-version` |
| Local via HTTPS | Ollama | OpenAI-compatible，无鉴权；必须使用 HTTPS 反向代理 |

所有配置均允许覆盖 HTTPS Endpoint 和模型名。兼容层只使用文本命令规划所需的共同字段，不会把某家厂商的未知参数广播给其他厂商。图片、文件、工具调用、联网搜索和厂商侧会话持久化不在 V0.2.0 范围内。

安全约束：

- 云端 API Key 只通过 Android Keystore 保险库取得；
- Ollama 不读取或发送 Key，但 App 仍保持全局明文 HTTP 禁止；
- 无论使用哪家厂商，模型返回都必须解析为最多 20 条单行命令，再经过本地 `ExecutionGuard`；
- 本次开发没有配置或调用任何真实 API Key，自动化测试全部使用本地假对象。

协议参考：

- [OpenAI Chat Completions](https://platform.openai.com/docs/api-reference/chat/create)
- [Anthropic Messages](https://docs.anthropic.com/en/api/messages)
- [Gemini OpenAI compatibility](https://ai.google.dev/gemini-api/docs/openai)
- [DeepSeek API](https://api-docs.deepseek.com/api/create-chat-completion)
- [Qwen OpenAI compatibility](https://help.aliyun.com/en/model-studio/qwen-api-via-openai-chat-completions)
- [Ollama OpenAI compatibility](https://docs.ollama.com/api/openai-compatibility)

## English

V0.2.0 supports OpenAI-compatible text chat for OpenAI, Gemini, DeepSeek, Qwen, Kimi, and custom gateways; the native Anthropic Messages protocol; and unauthenticated Ollama through an HTTPS reverse proxy. Endpoints and model names are configurable.

Provider compatibility does not imply feature parity. V0.2.0 intentionally excludes image/file input, tools, web search, and provider-side conversation persistence. Every response must decode to at most 20 single-line commands and pass the local `ExecutionGuard`. No real API key was configured or called during development.
