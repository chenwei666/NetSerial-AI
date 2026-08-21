# NetSerial AI Operations Terminal V0.2.0

[中文](README.md) | [English](README_EN.md)

This Android USB serial terminal provides a safety-first workflow: serial input → offline completion or AI draft → local risk review → manual load → manual send.

## Quick start

1. Select a USB serial port and baud rate from the device list.
2. Open “Device profile and memory” from the terminal menu and select the device name, vendor, CLI mode, and baud rate.
3. Tap offline suggestions while typing. The TAB button always sends the real ASCII byte `0x09`.
4. Open “AI settings”, create a provider profile, save a key, and activate the profile. Ollama behind an HTTPS reverse proxy does not need a key.
5. Tap “AI” in the terminal, then describe a goal or paste a command. Terminal context is opt-in and is ANSI-cleaned, redacted, and limited to 12,000 characters.
6. Every returned command is reclassified locally. Tapping a non-R4 command only loads it into the input field; the Send button remains a separate user action.

## Providers

- OpenAI, Gemini, DeepSeek, Qwen, Kimi, and custom OpenAI-compatible gateways use the compatible adapter.
- Claude/Anthropic uses the native Messages protocol and `x-api-key`.
- Ollama uses an unauthenticated OpenAI-compatible endpoint behind HTTPS because the app globally blocks cleartext traffic.
- Endpoints and model names remain user-configurable.

## Security

- Keys are encrypted with Android Keystore AES-GCM and never enter profile JSON, export files, memory, terminal logs, or source control.
- Android backup and cleartext HTTP are disabled; sensitive screens use secure-window protection.
- Terminal context is never sent unless explicitly selected.
- Local memory is explicitly written, device-scoped, expires after 180 days by default, and rejects likely credentials.
- AI has no direct serial-write capability. R4 commands are disabled in the UI.

## Build

For a Windows workspace under a non-ASCII path:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

The default pipeline runs `testDebugUnitTest`, `lintDebug`, and `assembleDebug` using JDK 17 and Android SDK 36 from `C:\tmp\NetSerial-tools`.

## External acceptance still required

- No real API key was supplied, so no live provider request or charge was made.
- No physical Android device, USB serial adapter, or switch was connected.
- The current artifact is Debug-signed, not a production Release.
- Command packs contain common commands and still require vendor-document and device-version validation.

Developer: chenwei666.
