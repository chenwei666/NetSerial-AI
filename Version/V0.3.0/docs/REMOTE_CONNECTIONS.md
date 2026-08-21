# 远程连接指南 / Remote Connection Guide

## SSH（推荐 / Recommended）

1. 填写主机名或 IP、端口、用户名和本次密码。Do not include `ssh://` in the host field.
2. 首次连接时从交换机 Console 执行厂商相应的 SSH 主机密钥查看命令，或从受信运维平台取得指纹。
3. 与 App 对话框中的指纹逐字符核对；一致后才选择信任。Verify every fingerprint before accepting it.
4. 密钥变化会阻止连接。先查明设备重装、密钥轮换或潜在中间人攻击，再决定是否清除已知主机。

The app opens an interactive `xterm` shell using SSH password authentication. Public-key import is intentionally not included in V0.3.0 because a secure key-import, passphrase, lifecycle, and backup policy requires separate design and hardware acceptance.

## Telnet（兼容旧设备 / Legacy compatibility）

Telnet transmits credentials and commands without encryption. It is disabled by default. Enable it only for an isolated management VLAN, confirm the warning for each connection, and enter credentials interactively. Migrate legacy devices to SSH whenever possible.

内置解析器处理跨数据包 IAC 状态、转义 IAC 和子协商跳过，并对不支持的选项返回保守拒绝。不同旧设备的 Telnet 选项组合差异很大，必须在隔离实验环境完成登录和回显验收。

## 字符编码 / Character encoding

- `UTF-8`：现代设备和英文环境首选。
- `GBK`：部分中文旧系统回显。
- `ISO-8859-1`：单字节兼容诊断选项。

Changing the encoding affects subsequent remote connections. It does not alter USB serial byte behavior.

## 不保存的数据 / Data never persisted

- SSH password or Telnet login password
- Telnet username entered in the terminal
- Terminal output as an automatic log
- Any real switch credential in app settings or exports

The only SSH connection artifact persisted is the host-key record required to detect impersonation or unexpected key changes.
