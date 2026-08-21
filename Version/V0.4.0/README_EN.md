# NetSerial AI Operations Terminal V0.4.0

NetSerial AI is an Android terminal for network operations. It combines USB Console, SSH, Telnet, command completion, multi-vendor command references, AI-assisted drafts, change control, configuration comparison, and field network tools in an offline-first workflow.

Developer: chenwei666

## What's new in V0.4.0

- Change-task workflow with ticket/site/device/window fields, pre-check, verification, rollback, a redacted timeline, and Markdown/PDF evidence exports.
- Wrong-target protection with lab/test/production environments, protected devices, management addresses, and a persistent color-coded target banner.
- Normalized configuration comparison, SHA-256 fingerprints, line-level differences, and vendor-aware rollback drafts.
- IPv4/IPv6 CIDR, DNS, Ping, Traceroute, TCP, path MTU, MAC/OUI, port reference, and identifier extraction tools.
- SSH password/keyboard-interactive authentication, session-only private keys, jump hosts, keepalive, and SFTP transfers.
- Four-phase AI plans: pre-check, change, verification, and rollback, with prompt-injection filtering and local completeness checks.
- Complete Chinese/English resources plus SSH keepalive and probe-timeout settings.

## Quick start

1. Configure the device name, vendor, environment, management address, and protection status in Device Profile.
2. Create a Change Task with a ticket, maintenance window, validation, and rollback plan.
3. Connect over USB, SSH, or Telnet and verify the color-coded target banner.
4. Use Tab, cursor keys, and Ctrl keys, or insert a draft from the categorized command library.
5. Configure a built-in AI provider or any OpenAI-compatible endpoint. API keys are protected through Android Keystore-backed storage.
6. Compare pre-change and post-change configurations before closure.
7. Export redacted Markdown or PDF evidence from the change task.

## SSH and file transfer

- Password mode supports both password and keyboard-interactive authentication.
- Private keys are loaded through the Android document picker, retained only in process memory, and cleared on disconnect or exit.
- Jump hosts currently use password/keyboard-interactive authentication and SSH direct-tcpip forwarding.
- SFTP upload is treated as an R3 action; protected targets require a valid matching change task and explicit confirmation.
- Unknown host keys require confirmation. A changed saved host key is blocked by default.

## AI providers

Presets are included for OpenAI, Anthropic, Gemini, DeepSeek, Qwen, Kimi, and Ollama. Custom OpenAI-compatible endpoints, models, and keys are supported. AI output is always a draft; R4 commands are excluded from the AI execution path. Terminal context is redacted and prompt-injection-like lines are removed locally.

## Security boundaries

- Telnet is plaintext and should only be used on isolated management or lab networks.
- SSH/Telnet passwords, private keys, and jump-host passwords are never persisted.
- Exports redact common API keys, tokens, passwords, private keys, and network-device secret/community syntax.
- The bundled OUI catalog is intentionally small; an unknown result does not indicate a faulty device.
- Ping, Traceroute, and path MTU depend on Android tooling, ACLs, and ICMP policy.
- AI output, rollback drafts, and configuration differences require engineer review before execution.

## Build

JDK 17 and the Android SDK are required. On Windows:

```powershell
.\scripts\build.ps1
```

On an initialized production-signing workstation:

```powershell
.\scripts\build.ps1 -Release
```

See [docs/TEST_REPORT.md](docs/TEST_REPORT.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), and [docs/SECURITY.md](docs/SECURITY.md) for release evidence and boundaries.

## External acceptance required

Validate USB OTG, real switches, private-key/keyboard-interactive/jump-host/SFTP paths, Android-specific Ping/Traceroute/MTU behavior, PDF rendering, and real AI-provider calls in an authorized environment before production use.
