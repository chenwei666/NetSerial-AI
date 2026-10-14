# NetSerial AI V0.12.0 · Root-free network toolkit

An Android operations companion for phones and tablets: USB Console, SSH, Telnet, vendor commands, AI chat with confirmed memory, and practical network tools with understandable results. Developer: chenwei666.

[中文](README.md) · [Changelog](CHANGELOG.md) · [Test report](docs/TEST_REPORT.md) · [Security](docs/SECURITY.md)

## Status

V0.12.0 production source and user guide. Get the APK and checksums from the [V0.12.0 Release](https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.12.0). It retains V0.10.0's production package and signing identity. Older archives remain unchanged; V0.11.0 is a development checkpoint whose AI improvements are included here, not a separate public release.

## What improved

- Searchable, categorized tools with only the selected tool's inputs visible; adaptive phone/tablet layouts, Chinese/English and light/dark appearance.
- Ping displays actual reply RTTs, loss statistics and a trend. Process duration is not presented as RTT, and missing replies are not shown as zero milliseconds.
- Results prioritize summaries, numerical facts and details, with collapsible raw output. Failure, partial completion, unsupported capability and cancellation are distinct states.
- Cancellable, bounded jobs; reports are redacted and previewed before copying, sharing or opening an editable AI draft. No automatic AI submission.

## 16 tool entries

| Purpose | Tools |
| --- | --- |
| Basic troubleshooting | Single-target health check, Ping, specified TCP port checks |
| Paths and addresses | Root-free TTL-based trace, IPv4 DF/MTU probe, address summary |
| Domains and websites | DNS A/AAAA/CNAME/MX/TXT/NS/PTR, HTTP/HTTPS HEAD diagnostics, TLS certificate/identity inspection |
| Current network and LAN | Local network information, bounded LAN discovery, Wake-on-LAN |
| Offline helpers | IPv4/IPv6 CIDR and mask calculations, MAC/OUI lookup, port reference, network identifier extraction |

## Important boundaries

- Root-free does not mean every Android device supports every probe. Trace depends on system Ping/TTL support and intermediate replies; unknown hops remain unknown and permission restrictions are reported. MTU is IPv4 DF only: timeout is not proof of a smaller MTU; a successful 1500-byte probe establishes a lower bound, not an exact larger-path limit.
- LAN discovery requires confirmation, accepts RFC1918 private `/26`–`/32` only, and is limited to 64 addresses, four selected TCP ports and a 40-second budget. No public-subnet scan. Lack of response does not prove a device is offline. Use only on networks you own or are authorized to assess.
- WOL requires confirmation and sends one 102-byte magic packet to the specified IPv4 destination, UDP port 9. Sent does not mean awake.
- DNS uses the supplied system resolver address or explicit `TYPE @server-IP`, without hidden public-DNS fallback. If no system DNS address is available, only A/AAAA can use the system resolver. DNSSEC is not validated.
- HTTP sends HEAD only: no body download, redirect following, credentials or cookies. A 405 response means HEAD is unsupported. HTTPS/TLS keep certificate trust and hostname checks; this is not an exhaustive TLS protocol or revocation scan.
- OUI data is bundled and limited; unknown vendors remain unknown. Android/OEM/VPN restrictions may limit local network information; protected Wi-Fi identifiers are not promised.

## Retained AI and terminal features

V0.11.0's conversation-first interface, permanent settings, native Markdown subset, code copying and confirmed memory remain. Long-term memories are saved only after editing, scope selection and explicit confirmation. Manage, edit, delete or disable them independently of chat history. Android 6.0+ encrypts storage; each request selects at most five applicable records, with 180-day expiry. Device scope means a named profile, not verified hardware identity. Legacy automatic memories are not imported implicitly.

USB/SSH/Telnet, vendor command references and control keys, existing AI providers/model configuration, guided troubleshooting and configuration comparison remain. AI does not automatically execute device commands; existing risk gates and manual review remain in force.

## Build and installation

Run `scripts/build.ps1` for unit tests, Lint and Debug assembly. Toolchain: `C:/tmp/NetSerial-tools`; isolated mirror: `C:/tmp/NetSerial-v0120-build`.

Production ID: `com.chenwei666.netserial`, versionName `0.12.0`, versionCode `14`. The signed APK supports in-place upgrade from the previous production release; back up important information and do not uninstall first. A clean-emulator upgrade was verified, not real-user credential/history migration. Debug ID: `com.chenwei666.netserial.preview012`, versionName `0.12.0-preview`; it coexists without importing production data. Build production with `scripts/build.ps1 -Release` using existing signing materials; never create a replacement key for an update.

Minimum Android 5.0; encrypted persistent memory requires Android 6.0+. The added normal permission `ACCESS_NETWORK_STATE` reads current network/DNS information; no Root is requested. See the [test report](docs/TEST_REPORT.md) for actual evidence and unverified scope. Local loopback/emulator tests do not certify all phones, physical switches or Internet services.

Review reports and AI context before sharing. Redaction cannot identify every free-text secret. Enabled memories/context are sent to your configured provider and may incur API charges; local deletion cannot retract upstream data. See [LICENSE](LICENSE.txt) for licensing and upstream attribution.
