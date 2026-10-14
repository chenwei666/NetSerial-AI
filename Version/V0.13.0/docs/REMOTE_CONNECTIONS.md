# 远程连接说明 / Remote Connections

## SSH

- 支持密码和 keyboard-interactive 认证。
- 支持 PEM/OpenSSH 私钥的会话级载入；文件最大 256 KiB，不保存原文或口令。
- 支持密码认证跳板机和 direct-tcpip 目标转发。
- 支持可配置 Keepalive，默认由应用设置控制。
- 支持 SFTP 下载与上传；上传按高风险动作确认。
- 首次主机密钥由用户确认并写入应用私有 `known_hosts`；发生变化时阻断。

Private keys, passwords, passphrases, and jump-host passwords are held only for the active connection attempt/session and are cleared when no longer needed.

## Telnet

Telnet 提供基本协商和终端交互，用于必须兼容旧设备的场景。它不加密身份凭据和命令，只能在受控、隔离的管理网络使用。

## 超时与错误

连接超时和 SSH Keepalive 可在设置页配置。错误提示只返回经过整理的消息，不在日志中输出凭据。断开连接时会关闭 shell/SFTP/转发通道、网络会话和工作线程。

## 外部验收矩阵

| 场景 | 最低验证 |
| --- | --- |
| SSH 密码 | 正确/错误密码、首次指纹、已知指纹、变化指纹 |
| Keyboard-interactive | 单提示认证、失败与取消 |
| 私钥 | 无口令/有口令、错误口令、不支持格式 |
| 跳板机 | 正常转发、跳板认证失败、目标不可达 |
| SFTP | 小/大文件上传下载、权限拒绝、中断恢复 |
| Telnet | 登录、命令、协商、远端关闭、非 UTF-8 字符集 |
