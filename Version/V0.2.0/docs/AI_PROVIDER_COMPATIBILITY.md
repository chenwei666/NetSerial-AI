# AI 厂商兼容性 / AI Provider Compatibility

## 中文

V0.2.0 当前实现 OpenAI-compatible Chat Completions 文本兼容核心：可配置 HTTPS 基础地址或完整 `/chat/completions` 地址、模型名称以及凭据别名。请求只使用共同字段 `model`、`messages` 和 `stream=false`，鉴权方式为 Bearer。

官方文档确认 DeepSeek、阿里云百炼兼容模式和 Gemini 兼容层均提供这一共同接口：

- OpenAI Chat Completions：https://platform.openai.com/docs/api-reference/chat/create
- DeepSeek Chat Completions：https://api-docs.deepseek.com/api/create-chat-completion
- 阿里云百炼 OpenAI 兼容 Chat：https://help.aliyun.com/en/model-studio/qwen-api-via-openai-chat-completions
- Gemini OpenAI compatibility：https://ai.google.dev/gemini-api/docs/openai

兼容层不代表厂商全部能力等价。图片、文件、联网搜索、工具调用、推理参数、原生 Responses API、Claude 原生协议、Ollama 本地无鉴权模式和企业 AI Gateway 自定义 Header 将使用独立能力适配器，不能把未知参数盲目发送给所有厂商。

当前没有配置或调用任何真实 API Key。真实连接测试必须由用户在 App 内自行保存密钥后显式触发。

## English

V0.2.0 currently implements the common text subset of OpenAI-compatible Chat Completions: a configurable HTTPS base or full `/chat/completions` endpoint, model name, credential alias, Bearer authentication, and the shared `model`, `messages`, and `stream=false` request fields.

Official documentation confirms this common interface for DeepSeek, Alibaba Cloud Model Studio compatible mode, and Gemini's compatibility layer. The links above are the protocol sources used by this implementation.

Compatibility does not imply complete feature parity. Images, files, web search, tools, reasoning options, native Responses APIs, the native Claude protocol, unauthenticated local Ollama, and enterprise gateway headers require dedicated capability adapters. Unknown provider-specific parameters must not be broadcast to every provider.

No real API key has been configured or called. Live connection tests must be explicitly initiated by the user after storing a key inside the app.
