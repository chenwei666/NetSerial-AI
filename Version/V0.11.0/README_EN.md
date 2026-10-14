# NetSerial AI V0.11.0 · Readable chat, confirmed memory

Android network operations toolkit: USB Console, SSH, Telnet, vendor command references, Tab/control keys, AI chat, guided troubleshooting, configuration comparison and field diagnostics. Developer: chenwei666.

[中文](README.md) · [Changelog](CHANGELOG.md) · [Test report](docs/TEST_REPORT.md)

## Status

Local development preview only. This version has not been pushed or released to GitHub and has no production-signed APK. Published baseline: [v0.10.0](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.10.0). Older source archives remain unchanged.

## Changes

- Conversation-first layout, collapsible device/context details, compact composer, permanent settings gear, theme-aware menus.
- Native headings, bold, lists, quotes, inline and fenced code. Actual command asterisks, whitespace and line endings remain intact. Complex tables/nesting fall back to text.
- Copy original answers or individual code blocks; manually review commands. No automatic device execution; critical R4 commands remain blocked.
- Remember only after editing, choosing a scope and explicitly confirming. Manage, add, edit and delete memories. Chat history and long-term memory are separate.
- Android 6.0+ encrypted storage, a memory-use toggle and a last-request memory receipt. At most five confirmed records per request, expiring after 180 days.
- Existing providers, model selection, troubleshooting, language and appearance settings remain available. No paid AI calls were made during development tests.

See [memory behavior](docs/SECURITY.md). Device scope uses the named device profile, not verified hardware identity. Default unnamed devices and guided troubleshooting allow personal preferences only. Legacy automatic memories are not imported or trusted implicitly.

## Build and install

Run `scripts/build.ps1` for JVM tests, Lint and Debug assembly. Toolchain: `C:/tmp/NetSerial-tools`; isolated mirror: `C:/tmp/NetSerial-v0110-build`.

Preview application ID: `com.chenwei666.netserial.preview011`, label NetSerial AI Preview, versionCode 13. It coexists with production and older previews without importing their keys, connections or history. Production configuration remains present but was not used for this delivery.

General tools support Android 5.0+; encrypted persistent memory requires Android 6.0+. See the test report for verified scope; real AI providers and physical network devices are not covered by synthetic tests.

## Privacy and safety

Review context before sending. Enabled memories are sent to your configured AI provider, including configured failover providers. Secret detection is best effort; do not store passwords, API keys or private keys. Local deletion cannot retract data already sent upstream. See [security notes](docs/SECURITY.md) and [LICENSE](LICENSE.txt).
