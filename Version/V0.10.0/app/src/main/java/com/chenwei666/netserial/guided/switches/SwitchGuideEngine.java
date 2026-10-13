package com.chenwei666.netserial.guided.switches;

import com.chenwei666.netserial.device.DeviceFingerprint;
import com.chenwei666.netserial.device.DeviceFingerprintEngine;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.diagnostics.HealthFinding;
import com.chenwei666.netserial.diagnostics.SwitchHealthEngine;
import com.chenwei666.netserial.guided.DiagnosticFinding;
import com.chenwei666.netserial.guided.DiagnosticFinding.Status;
import com.chenwei666.netserial.guided.GuideText;
import com.chenwei666.netserial.guided.GuidedScenario;
import com.chenwei666.netserial.terminal.AnsiTextSanitizer;
import com.chenwei666.netserial.terminal.SensitiveTextRedactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Offline explanations from captured evidence; never connects, executes, or saves a backup. */
public final class SwitchGuideEngine {
    private static final int MAX_CAPTURE = 32_000;
    private static final int MAX_FINDINGS = 16;
    private static final Pattern COMMAND_ERROR = Pattern.compile(
            "(?im)^\\s*(?:[%^]\\s*)?(?:error\\b|syntax error|invalid (?:input|command|interface)|unrecognized command|"
                    + "unknown command|incomplete command|ambiguous command|permission denied|"
                    + "authorization failed|command (?:is )?not (?:found|supported)|"
                    + "insufficient (?:privilege|permission)|错误|无效命令|权限不足).*$");
    private static final Pattern SENSITIVE_LINE = Pattern.compile(
            "(?im)^.*\\b(?:password|passwd|secret|community|private-key|api[_ -]?key|token|"
                    + "authentication-key|cipher-key|pre-shared-key)\\b.*$");
    private static final Pattern PRIVATE_KEY_TAIL = Pattern.compile(
            "(?s)-----BEGIN [^-]*PRIVATE KEY-----.*");

    public List<GuideCommand> commands(GuidedScenario scenario, Vendor vendor, String parameter) {
        return SwitchGuideCommands.create(scenario, vendor, parameter);
    }

    public List<DiagnosticFinding> analyze(GuidedScenario scenario, Vendor vendor,
                                           String parameter, String evidence) {
        List<DiagnosticFinding> result = new ArrayList<>();
        String target;
        try {
            target = SwitchGuideCommands.validate(scenario, parameter);
        } catch (IllegalArgumentException invalid) {
            result.add(finding(Status.WARNING, "目标格式不正确", "Invalid target format",
                    "端口请填写完整编号（如 GigabitEthernet1/0/1），VLAN 请填 1–4094 的数字。",
                    "Use an interface ID such as GigabitEthernet1/0/1, or a VLAN number from 1 to 4094.",
                    "修正输入后重新生成；不要输入整条命令或换行。",
                    "Correct the target and try again. Do not enter a full command or a new line.", ""));
            return result;
        }
        String boundedInput = boundedInput(evidence);
        // Detect errors before redacting whole credential lines. Redaction must not hide a
        // failed command and let an earlier UP line turn the result into a passing check.
        boolean commandFailed = COMMAND_ERROR.matcher(new AnsiTextSanitizer().sanitize(boundedInput)).find();
        String capture = sanitize(boundedInput);
        if (capture.trim().isEmpty()) {
            result.add(finding(Status.UNKNOWN, "还没有设备输出", "No device output yet",
                    "需要读取当前连接的终端输出，或粘贴设备输出，才能判断。",
                    "Read the connected terminal or paste device output before interpreting its state.",
                    "先连接并登录设备，确认品牌后预览只读命令，手动发送，再回来读取。",
                    "Connect and sign in, select the vendor, review and manually send read-only commands, then read the output.", ""));
            return result;
        }
        if (commandFailed) {
            result.add(finding(Status.WARNING, "设备返回了命令或权限错误", "The device reported a command or permission error",
                    "这段输出包含未成功执行的命令，不能作为检查通过的依据。",
                    "At least one command did not complete successfully. This capture cannot establish a passing check.",
                    "核对品牌、型号、登录权限及命令帮助；取得成功输出后重新读取。",
                    "Check the vendor, model, login permissions, and command help. Capture successful output and retry.", capture));
            return result;
        }
        DeviceFingerprint fingerprint = new DeviceFingerprintEngine().identify(capture);
        Vendor selected = vendor == null ? Vendor.GENERIC : vendor;
        if (selected != Vendor.GENERIC && fingerprint.isHighConfidence()
                && fingerprint.getVendor() != selected) {
            result.add(finding(Status.WARNING, "所选品牌与输出不一致", "Selected vendor differs from the capture",
                    "输出中的品牌标识与手动选择不一致，可能读到了另一台设备或旧会话。",
                    "The capture identifies a different vendor. It may belong to another device or an older session.",
                    "核对当前连接的设备，重新选择正确品牌并读取当前输出。",
                    "Verify the connected device, choose the matching vendor, and read fresh output.", fingerprint.getEvidence()));
            return result;
        }
        if (scenario == GuidedScenario.IDENTIFY_DEVICE) {
            result.add(identify(fingerprint));
            return result;
        }
        if (selected == Vendor.GENERIC && !fingerprint.isHighConfidence()) {
            result.add(finding(Status.UNKNOWN, "设备品牌尚未确认", "Device vendor is not confirmed",
                    "目前输出不足以可靠区分厂商，不能自动套用某个品牌的命令。",
                    "The supplied output cannot reliably identify a vendor, so vendor-specific commands are not assumed.",
                    "检查设备铭牌或登录欢迎信息，选择品牌；也可粘贴已获取的版本输出进行识别。",
                    "Check the device label or login banner and select its vendor, or paste version output for identification.", ""));
            return result;
        }
        switch (scenario) {
            case PORT_LINK:
                result.addAll(SwitchPortEvidence.analyze(capture, target, false));
                break;
            case VLAN_GATEWAY:
                result.addAll(SwitchPortEvidence.analyze(capture, target, true));
                result.add(finding(Status.UNKNOWN, "VLAN、网关和 DHCP 还需要联合核对", "VLAN, gateway, and DHCP need cross-checking",
                        "VLAN 存在或三层接口 UP，并不能证明终端已分到正确地址、上联已放行或网关可达。",
                        "An existing VLAN or an UP layer-3 interface does not prove DHCP assignment, uplink allowance, or gateway reachability.",
                        "对照终端 IP/掩码/网关，核对接入口 VLAN 与上联放行列表，再从终端测试网关；DHCP 失败需检查地址池和中继。",
                        "Compare the client IP, mask, and gateway with access and uplink VLANs. Test the gateway from the client; check the DHCP pool and relay if assignment failed.", ""));
                break;
            case SWITCH_HEALTH:
                addHealth(result, capture);
                break;
            case CONFIG_BACKUP:
                result.add(finding(Status.UNKNOWN, "输出已读取，备份尚未保存", "Output was read; a backup has not been saved",
                        "读取终端内容不是保存备份；这段内容还可能缺页或包含其他命令输出。",
                        "Reading terminal text does not save a backup. It may be incomplete or mixed with other command output.",
                        "确认运行配置完整、已翻完所有分页，然后打开配置快照中心显式保存；脱敏副本不能直接用于完整恢复。",
                        "Confirm the complete running configuration, including all pages, then explicitly save in Configuration Snapshots. A redacted copy is not a complete restore image.", ""));
                break;
            default:
                result.add(finding(Status.UNKNOWN, "需要补充排障信息", "More diagnostic information is needed",
                        "仅凭这一段终端输出还无法确定问题原因。", "This terminal excerpt alone cannot establish the cause.",
                        "选择端口、VLAN 或健康检查向导，并收集对应的只读输出。",
                        "Choose the port, VLAN, or health guide and collect the corresponding read-only output.", ""));
                break;
        }
        if (evidence != null && evidence.length() > MAX_CAPTURE) {
            result.add(finding(Status.UNKNOWN, "仅分析了部分输出", "Only part of the output was analyzed",
                    "输入过长，本次只读取前 32,000 字符以内的完整行。", "The input is long; only complete lines within the first 32,000 characters were read.",
                    "请单独粘贴目标命令的完整输出再检查。", "Paste the complete output of the specific command and check again.", ""));
        }
        return Collections.unmodifiableList(result);
    }

    private static DiagnosticFinding identify(DeviceFingerprint fingerprint) {
        if (!fingerprint.isHighConfidence()) {
            return finding(Status.UNKNOWN, "暂时无法确认品牌", "Vendor could not be confirmed",
                    "缺少清晰的品牌版本标识，或输出混有多个品牌。", "Clear vendor/version markers are missing, or the text mixes several vendors.",
                    "读取当前设备欢迎信息或版本输出；也可查看机身铭牌后手动选择品牌。",
                    "Read the current device banner or version output, or check the hardware label and select its vendor.", "");
        }
        return finding(Status.PASS, "已发现品牌标识", "Vendor marker found",
                "输出匹配 " + fingerprint.getEvidence() + "；这只确认品牌线索，不代表设备健康。",
                "Output matches " + fingerprint.getEvidence() + "; this is a vendor clue, not a device-health assessment.",
                "核对设备铭牌和当前连接，再使用对应品牌的命令向导。",
                "Verify the hardware label and active connection before using the matching command guide.", fingerprint.getEvidence());
    }

    private static void addHealth(List<DiagnosticFinding> result, String capture) {
        // Existing thresholds represent utilization. Idle/free percentages and negated alarms
        // must not be fed to them as used resources or active hardware faults.
        StringBuilder eligible = new StringBuilder();
        for (String line : capture.split("\\n")) {
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.matches(".*\\b(?:idle|free|available)\\b.*")
                    || lower.matches(".*\\b(?:no|not|without)\\s+(?:fan|power|temperature)\\b.*")) continue;
            eligible.append(line).append('\n');
        }
        for (HealthFinding health : new SwitchHealthEngine().analyze(eligible.toString()).getFindings()) {
            if (result.size() >= MAX_FINDINGS) break;
            String code = health.getCode();
            if ("NO_THRESHOLD_ALERT".equals(code)) continue;
            String zh;
            String en;
            String nextZh;
            String nextEn;
            switch (code) {
                case "CPU_HIGH":
                    zh = "CPU 使用率偏高"; en = "CPU utilization appears high";
                    nextZh = "间隔一分钟再次读取 CPU；若持续偏高，检查高占用进程和近期网络变化。";
                    nextEn = "Read CPU again after one minute; if sustained, inspect busy processes and recent network changes.";
                    break;
                case "MEMORY_HIGH":
                    zh = "内存使用率偏高"; en = "Memory utilization appears high";
                    nextZh = "确认该百分比代表已使用内存，再与历史基线对比，检查路由或进程增长。";
                    nextEn = "Confirm the percentage represents used memory, compare with the baseline, and inspect route or process growth.";
                    break;
                case "TEMPERATURE_HIGH":
                    zh = "温度读数偏高"; en = "A temperature reading appears high";
                    nextZh = "检查机柜通风、进风口和风扇，并按该型号的温度阈值判断。";
                    nextEn = "Inspect rack airflow, vents, and fans, then compare with the model's temperature limits.";
                    break;
                case "INTERFACE_ERRORS":
                    zh = "发现非零错误计数"; en = "Non-zero error counters found";
                    nextZh = "先记录当前计数，过一分钟重新读取；持续增长时检查网线、光模块和拥塞。";
                    nextEn = "Record the counters and read again after one minute; if rising, inspect cables, optics, and congestion.";
                    break;
                default:
                    zh = "发现硬件告警文字"; en = "Hardware alarm text found";
                    nextZh = "核对告警是否仍有效，检查风扇、电源与温度，保留冗余并联系维护人员。";
                    nextEn = "Confirm the alarm is current, inspect fans, power, and temperature, preserve redundancy, and contact maintenance.";
            }
            result.add(finding(Status.WARNING, zh, en,
                    "当前片段匹配到检查规则；仍需核对单位、时间及设备型号。",
                    "The excerpt matched a check rule; verify the units, time, and device model.",
                    nextZh, nextEn, health.getEvidence()));
        }
        result.add(finding(Status.UNKNOWN, "尚不能确认整机健康", "Whole-device health is not established",
                "这里只检查可识别的异常文字和阈值；没有告警匹配不等于正常，端口、温度和电源信息可能缺失。",
                "Only recognized alarms and thresholds are checked. No matching alert does not mean healthy; port, temperature, or power evidence may be missing.",
                "按命令清单补齐输出，对照该型号基线；有业务故障时继续端口或 VLAN 向导。",
                "Collect the listed outputs and compare with the model baseline. Use the port or VLAN guide for service faults.", ""));
    }

    static String sanitize(String input) {
        String text = new AnsiTextSanitizer().sanitize(boundedInput(input)).replace('\r', '\n');
        text = new SensitiveTextRedactor().redact(text);
        text = PRIVATE_KEY_TAIL.matcher(text).replaceAll("[REDACTED_PRIVATE_KEY]");
        return SENSITIVE_LINE.matcher(text).replaceAll("[REDACTED_SENSITIVE_LINE]");
    }

    private static String boundedInput(String input) {
        String raw = input == null ? "" : input;
        if (raw.length() > MAX_CAPTURE) {
            int boundary = raw.lastIndexOf('\n', MAX_CAPTURE - 1);
            raw = boundary < 0 ? "" : raw.substring(0, boundary);
        }
        return raw;
    }

    static DiagnosticFinding finding(Status status, String zh, String en, String explanationZh,
                                     String explanationEn, String nextZh, String nextEn, String evidence) {
        return new DiagnosticFinding(status, new GuideText(zh, en), new GuideText(explanationZh, explanationEn),
                new GuideText(nextZh, nextEn), evidence);
    }
}
