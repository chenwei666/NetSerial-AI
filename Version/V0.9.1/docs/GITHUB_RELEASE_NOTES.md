# NetSerial AI V0.9.1 Candidate / 候选版

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
- APK SHA-256: `fb105b2e9a4973b3e329defdfb8d5d0aaba70ed499aebe596b4b14e36e1c4780`

Physical Android devices, USB adapters, SSH/Telnet servers, and target switch firmware still require authorized field acceptance. This candidate has not yet been pushed, merged, or published.
