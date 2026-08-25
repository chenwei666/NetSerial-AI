# NetSerial AI V0.9.1 / 正式版

V0.9.1 lets Operations Center read the currently connected USB, SSH, or Telnet terminal with one tap and automatically select the detected switch vendor.

## Highlights / 重点功能

- One-tap read of the most recently active connected terminal.
- Current protocol, target, and readable character count shown before reading.
- Automatic H3C Comware, Huawei VRP, Cisco IOS, and Ruijie RGOS identification.
- ANSI cleanup, local sensitive-field redaction, four-session limit, and 100,000-character bounded snapshots.
- Bilingual success, empty-session, and disconnected guidance.

## Safety / 安全边界

The snapshot stays in process memory, disappears on process exit, and contains no connection or send callback. Identification never opens a hidden session or silently sends `show/display version`; engineers remain responsible for initiating any read-only command in the terminal. Existing preview, target, maintenance-window, and R0–R4 gates remain unchanged.

## Verification / 验证

- 60 test classes / 186 tests: all passed
- Release Lint: passed, 0 errors / 140 warnings
- Production-signed Release APK: built successfully
- ZIP alignment and V1/V2/V3 signatures: verified
- APK size: `6,273,048 bytes`
- APK SHA-256: `8890591333b84658539bff9ba80a2367a85ca50825578c065d4d4c1e6884e646`

Physical Android devices, USB adapters, SSH/Telnet servers, and target switch firmware still require authorized field acceptance. V0.9.1 is published as the GitHub Latest production release, and the remote APK size and SHA-256 were verified after download.
