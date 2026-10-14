# NetSerial AI V0.13.0 · Autonomous AI tasks

An Android operations companion for phones and tablets: USB Console, SSH, Telnet, vendor commands, AI chat with confirmed memory, and practical network tools with understandable results. Developer: chenwei666.

[中文](README.md) · [Changelog](CHANGELOG.md) · [Test report](docs/TEST_REPORT.md) · [Security](docs/SECURITY.md)

## Status

This is the **V0.13.0 production-source archive**. Publication was authorized; production build/signature, V0.12.0 in-place upgrade and cold start passed. Root version history records final GitHub publication/download verification. Autonomous tasks remain a limited new feature, started explicitly on the task page; ordinary chat remains draft-only. Real provider, physical-switch and OEM acceptance is outstanding. See the [test report](docs/TEST_REPORT.md); older archives are unchanged.

## Start a task; confirm high risks only

1. Connect USB Console, SSH or Telnet, complete login and reach a ready CLI prompt. Open AI from that exact terminal, then select **AI auto-execute**.
2. Enter a specific goal and start. One opaque token binds one current connection. Only the selected provider/model is used; there is no automatic provider failover. Missing parameters are requested, but ordinary chat is never a password input.
3. After device identification and relevant prechecks, exact allowlisted queries and narrowly scoped routine changes run automatically. Routine changes currently focus on the requested VLAN and bounded descriptions—not every R2 configuration command.
4. High or unknown risk pauses for an exact typed target name. Protected devices also require a matching authorized change task and maintenance window. Models, device output and memories cannot grant permission.

Each task is limited to 32 steps and 10 minutes, with sequential response checks. Errors, paging/interactive prompts, timeouts, disconnects or target changes stop execution. No automatic retries. Leaving the page or stopping prevents further dispatch, without automatic resume, rollback or startup-config saving; already sent commands may have taken effect.

### Local Web account and HTTPS workflow

`WEB_ACCESS` uses local templates for H3C Comware 5/7, Huawei VRP, Cisco IOS/IOS XE and Ruijie RGOS. Unsupported or unproven versions are not certified as compatible. A dedicated secure local password dialog hands a `char[]` to the executor, never to AI requests, chat or memory. One high-risk approval covers the local template, which may create or overwrite accounts and administrative privileges.

After sensitive commands, terminal/AI output stays suppressed until reconnect to protect delayed credential echoes. A returned CLI prompt is **not proof of Web reachability or successful login**. There is no automatic save or rollback. Arrays are wiped, but templates/transports can create JVM Strings and system copies; complete memory erasure is not promised. Use a trusted keyboard and prefer HTTPS.

Tasks inherit the chat preference for confirmed memories, selecting at most five; they do not automatically write memories. Saving long-term memory still requires explicit confirmation and never establishes permanent execution authority.

## Retained V0.12.0 toolkit baseline

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

USB/SSH/Telnet, vendor commands and control keys, provider/model configuration, guided troubleshooting and configuration comparison remain. Ordinary chat produces drafts; only the explicitly entered task page adds controlled automatic execution.

### Toolkit reference screenshots — V0.12.0 baseline

<img src="docs/images/toolbox-tablet-light.png" width="720" alt="V0.12.0 baseline tablet toolkit in light mode">

<img src="docs/images/toolbox-phone-dark.png" width="280" alt="V0.12.0 baseline phone toolkit in dark mode">

These are not acceptance screenshots of the V0.13.0 task page.

## Build and installation

Run `scripts/build.ps1` for unit tests, Lint and Debug assembly. Toolchain: `C:/tmp/NetSerial-tools`; isolated mirror: `C:/tmp/NetSerial-v0130-build`.

Current configuration: versionName `0.13.0`, versionCode `15`; Debug ID `com.chenwei666.netserial.preview013`, version `0.13.0-preview`. Preview data stays separate. Production ID is `com.chenwei666.netserial`, with the original certificate and verified V0.12.0 → V0.13.0 in-place upgrade/cold start. See [checksums](docs/SHA256SUMS.txt) and root records/Release for publication status. Future upgrades must retain the original production certificate.

Minimum Android 5.0; encrypted persistent memory requires Android 6.0+. The added normal permission `ACCESS_NETWORK_STATE` reads current network/DNS information; no Root is requested. See the [test report](docs/TEST_REPORT.md) for actual evidence and unverified scope. Local loopback/emulator tests do not certify all phones, physical switches or Internet services.

Review reports and AI context before sharing. Redaction cannot identify every free-text secret. Enabled memories/context are sent to your configured provider and may incur API charges; local deletion cannot retract upstream data. See [LICENSE](LICENSE.txt) for licensing and upstream attribution.
