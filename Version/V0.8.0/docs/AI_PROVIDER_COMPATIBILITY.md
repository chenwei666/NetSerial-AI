# AI 厂商兼容性 / AI Provider Compatibility

## V0.8.0 支持矩阵

V0.8.0 提供 18 个厂商/接入类型入口。预设用于减少配置工作，不会锁死端点或模型；用户仍可覆盖 HTTPS Endpoint、模型名和安全凭据别名。

| ID | 厂商 / Provider | 聊天协议 / Chat API | 模型目录 / Model catalog |
| --- | --- | --- | --- |
| `openai` | OpenAI | OpenAI-compatible, Bearer | OpenAI `data[].id` |
| `anthropic` | Claude / Anthropic | Native Messages, `x-api-key` | Anthropic `data[].id` |
| `gemini` | Google Gemini | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `deepseek` | DeepSeek | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `zhipu` | 智谱 GLM / Zhipu | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `qwen` | 通义千问 / Alibaba Cloud | OpenAI-compatible, Bearer | DashScope `/api/v1/models`, `output.models[].model` |
| `doubao` | 豆包 / Volcano Ark | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `hunyuan` | 腾讯混元 / TokenHub | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `baidu` | 百度千帆 / Qianfan | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `kimi` | Kimi / Moonshot | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `minimax` | MiniMax | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `siliconflow` | 硅基流动 / SiliconFlow | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `groq` | Groq | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `mistral` | Mistral AI | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `xai` | xAI | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `openrouter` | OpenRouter | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `openai-compatible` | 自定义兼容网关 | OpenAI-compatible, Bearer | OpenAI-compatible `data[].id` |
| `ollama` | Ollama / Local | OpenAI-compatible, no key | Ollama `/api/tags`, `models[].name` |

## 上游模型同步

- “刷新模型”会根据当前档案端点推导模型目录，并由用户主动发起 HTTPS GET 请求。
- 响应最多读取 1000 个唯一模型 ID，每个 ID 最多 256 字符；列表去重并排序。
- 目录结果只缓存模型 ID 和获取时间，不缓存 API Key、响应头或账号信息。
- 请求支持超时、响应大小上限、取消和禁止重定向。
- 厂商账户、区域、代理网关或权限可能不开放模型目录。刷新失败时保留已缓存结果，并始终允许手工输入模型名。
- Ollama 必须通过可信 HTTPS 反向代理接入；应用不为本地地址放宽全局明文 HTTP 限制。

## 安全边界

- 云端 API Key 通过 Android Keystore 凭据保险库读取，不写入模型缓存、日志或版本文档。
- 厂商兼容不代表能力完全一致；当前只使用文本命令规划所需的共同字段。
- 图片、文件、工具调用、厂商联网搜索及厂商侧会话持久化不属于 V0.8.0 范围。
- AI 返回必须解析为最多 20 条单行命令，并继续经过本地 `ExecutionGuard`；模型不会绕过人工确认直接执行高风险命令。
- 自动化测试只使用本地假对象，本次开发未配置或调用真实 API Key。

## 官方协议参考 / Official References

- [OpenAI Models](https://platform.openai.com/docs/api-reference/models/list)
- [Anthropic Models](https://docs.anthropic.com/en/api/models-list)
- [Gemini OpenAI compatibility](https://ai.google.dev/gemini-api/docs/openai)
- [DeepSeek API](https://api-docs.deepseek.com/)
- [智谱开放平台 / Zhipu Open Platform](https://docs.bigmodel.cn/)
- [Qwen OpenAI compatibility](https://help.aliyun.com/en/model-studio/qwen-api-via-openai-chat-completions)
- [Ollama OpenAI compatibility](https://docs.ollama.com/api/openai-compatibility)

## English Summary

V0.8.0 exposes 18 provider or gateway entries. Sixteen cloud presets and custom gateways use the OpenAI-compatible text interface, Anthropic uses its native Messages authentication, and Ollama uses the OpenAI-compatible chat interface plus `/api/tags` for local model discovery. Upstream discovery is bounded, cancellable, HTTPS-only, redirect-free, and backed by a non-secret local cache. Manual model entry remains available whenever an account or gateway does not expose a model-list endpoint.
