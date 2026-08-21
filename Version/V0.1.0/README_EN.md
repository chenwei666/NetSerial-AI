# NetSerial AI Operations Terminal V0.1.0

[中文](README.md) | [English](README_EN.md)

NetSerial AI is an Android USB serial terminal for network operations engineers. This version delivers the first buildable vertical baseline for trusted source provenance, offline completion, deterministic execution safety, and pluggable AI providers.

## Included in this version

- USB discovery and serial connections for FTDI, PL2303, CP210x, CH340/CH341, and USB CDC devices.
- Baud-rate selection, text/HEX transmission, newline modes, modem control lines, and flow control.
- A dedicated `TAB` button that sends ASCII `0x09` without appending CR or LF.
- An offline `CompletionEngine` with initial H3C user-view `display` and system-view `interface` completions, isolated by CLI mode.
- A local `ExecutionGuard` that classifies `reboot` as R4 and `display ...` as R1.
- An `AiProviderCatalog` covering OpenAI, Claude/Anthropic, Gemini, DeepSeek, Qwen, Kimi, OpenAI-compatible endpoints, and Ollama.
- A `SafeAiCopilot` that re-evaluates every provider-generated command locally. AI cannot lower deterministic risk.
- Chinese default resources and English system-locale resources.

## Current limitations

- Live AI HTTP adapters, the API-key settings screen, and Android Keystore credential storage are not implemented yet.
- The offline command pack is still a minimal H3C architecture-validation set.
- The APK has not yet been tested on a physical Android device, USB serial cable, or switch.
- V0.1.0 is a Debug build and is not a production release package.

## Build

For projects stored under a Windows path containing non-ASCII characters, run the ASCII mirror build script:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

The script expects JDK 17 at `C:\tmp\NetSerial-tools\jdk17` and Android SDK Platform 36 at `C:\tmp\NetSerial-tools\android-sdk`. It runs unit tests, Android Lint, and the Debug APK build by default.

## Security

- No real API key, token, account credential, or device password is included.
- Android backup and cleartext HTTP are disabled by default.
- AI output is an untrusted draft and must pass `SafeAiCopilot` and `ExecutionGuard`.
- R4 commands cannot be executed automatically.

See [Security](docs/SECURITY.md), [Architecture](docs/ARCHITECTURE.md), and the [Test report](docs/TEST_REPORT.md).

## Upstream and license

The USB serial baseline comes from Kai Morich's MIT-licensed [SimpleUsbTerminal](https://github.com/kai-morich/SimpleUsbTerminal), commit `7710eb7b1b69fb346f3b715960b5a5b5db08beb3`. The original copyright and MIT license are preserved in [LICENSE.txt](LICENSE.txt), with provenance recorded in [UPSTREAM.md](UPSTREAM.md).

Developer: chenwei666.
