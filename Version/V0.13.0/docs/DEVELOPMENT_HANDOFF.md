# V0.13.0 自动任务正式构建交接

- 维护者：chenwei666。工作目录 Version/V0.13.0；不修改更早的 Version 归档。当前配置 versionName 0.13.0、code 15、Debug .preview013，构建镜像 C:/tmp/NetSerial-v0130-build。
- 用户选定模式：任务内自动读取和有限常规配置，高风险才确认。普通聊天保持草稿；任务必须从当前连接终端显式进入，不能推断最新设备。
- 核心：AgentActionCodec 严格 COMMAND/ASK/FINISH/WEB_ACCESS；AgentCommandPolicy 本地风险底线；AgentTaskEngine 识别/现状/范围/提示符/回读检查；AgentTaskRunner 选定模型、记忆与保护目标；AgentSessionBridge 当前连接 lease 与原始回显、手工/自动互斥。
- 单次 32 步/10 分钟；不自动重试。高风险/未知分类输入完整目标名称确认，受保护设备需匹配变更任务及窗口。停止先同步标记取消，资源关闭在后台；离页不恢复，已发送命令可能生效。
- WEB_ACCESS 只接受用户名和协议，不接受密码 JSON。H3C Comware 5/7、Huawei VRP、Cisco IOS/IOS XE、Ruijie RGOS 本地模板整份高风险确认；密码在本地 secure dialog 用 char[] 交付，不入 AI/聊天/记忆。敏感回显隐藏至重新连接；不声称服务可达/登录成功，不自动保存/回退，不保证 String/系统副本彻底清除。
- 已确认记忆最多 5 条，沿用聊天开关，无任务自动新增记忆；固定已选 provider/model，无 failover。真实模型/OEM/物理交换机仍需验证，不以编译或 synthetic 测试替代。
- README 中工具箱截图明确为 V0.12.0 基线，不是新自动任务 UI 验收截图。架构/安全/发布文档中保留的旧章节是历史参考；V0.13.0 顶部规则优先适用于新增任务路径。
- 验证状态由主验证流程更新 TEST_REPORT.md：构建、Lint、JVM、隔离 preview 仪器测试和未测范围分别记录。不在本交接复写未经核验的数字、哈希、生产签名或发布状态。
- 本轮用户已授权 GitHub 正式发布；生产包 6,560,459 bytes，SHA-256 c1fadf90e9a2045eedb2aca715f6b2bd32a69e65f6433bd1807a80dc32ffb85d，原证书 V1/V2/V3、V0.12.0→V0.13.0 覆盖升级和冷启动通过。发布后上传/重新下载/Latest 结果记在根版本历史，不再修改已发布源码归档；没有授权实机配置变更。
