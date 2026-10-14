package com.chenwei666.netserial.guided.switches;

import com.chenwei666.netserial.guided.DiagnosticFinding;
import com.chenwei666.netserial.guided.DiagnosticFinding.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses explicit state fields, never arbitrary occurrences of "up" in descriptions. */
final class SwitchPortEvidence {
    private static final String INTERFACE = "([A-Za-z][A-Za-z0-9._/-]*[0-9][A-Za-z0-9._/-]*)";
    private static final Pattern HEADER = Pattern.compile("(?i)^" + INTERFACE
            + "(?:\\s+(?:is|current state\\s*[:：]).*)?$");
    private static final Pattern CISCO = Pattern.compile("(?i)^" + INTERFACE
            + "\\s+is\\s+(administratively down|up|down),\\s*line protocol is\\s+(up|down)(?:\\s.*)?$");
    private static final Pattern DISPLAY = Pattern.compile(
            "(?i)^(?:" + INTERFACE + "\\s+)?current state\\s*[:：]\\s*(administratively down|admin(?:istratively)?\\s+down|up|down)\\s*$");
    private static final Pattern PROTOCOL = Pattern.compile("(?i)^line protocol (?:current )?state\\s*[:：]\\s*(up|down)\\s*$");
    private static final Pattern TABLE = Pattern.compile("(?i)^" + INTERFACE
            + "\\s+(up|down|adm|administratively down|\\*down)\\s+(up|down|--)\\s*(?:.*)?$");
    private static final Pattern STATUS_ROW = Pattern.compile("(?i)^" + INTERFACE
            + "\\s+(?:.*?\\s+)?(connected|notconnect|disabled|err-disabled)\\s+"
            + "(?:[0-9]+|trunk|routed|unassigned)\\s+.*$");
    private static final Pattern IP_ROW = Pattern.compile("(?i)^" + INTERFACE
            + "\\s+(?:[0-9]+(?:\\.[0-9]+){3}(?:/[0-9]+)?|unassigned|--)"
            + "(?:\\s+(?:YES|NO)\\s+\\S+)?\\s+(administratively down|up|down|\\*down)\\s+(up|down)\\s*$");

    private SwitchPortEvidence() { }

    static List<DiagnosticFinding> analyze(String capture, String target, boolean vlan) {
        List<DiagnosticFinding> result = new ArrayList<>();
        String context = "";
        String pending = "";
        String pendingEvidence = "";
        for (String raw : capture.split("\\n")) {
            if (result.size() >= 12) break;
            String line = raw.trim();
            Matcher cisco = CISCO.matcher(line);
            Matcher display = DISPLAY.matcher(line);
            Matcher table = TABLE.matcher(line);
            Matcher status = STATUS_ROW.matcher(line);
            Matcher ip = IP_ROW.matcher(line);
            Matcher header = HEADER.matcher(line);
            Matcher protocol = PROTOCOL.matcher(line);
            if (cisco.matches()) {
                context = cisco.group(1);
                add(result, context, cisco.group(2), cisco.group(3), line, target, vlan);
                pending = "";
            } else if (display.matches()) {
                if (display.group(1) != null) context = display.group(1);
                pending = display.group(2);
                pendingEvidence = line;
                if (!pending.equalsIgnoreCase("up")) {
                    add(result, context, pending, "", line, target, vlan);
                    pending = "";
                }
            } else if (protocol.matches()) {
                if (!pending.isEmpty()) {
                    add(result, context, pending, protocol.group(1), pendingEvidence + "\n" + line, target, vlan);
                    pending = "";
                }
            } else if (table.matches()) {
                add(result, table.group(1), table.group(2), table.group(3), line, target, vlan);
                context = table.group(1);
                pending = "";
            } else if (status.matches()) {
                add(result, status.group(1), status.group(2), "", line, target, vlan);
                context = status.group(1);
                pending = "";
            } else if (ip.matches()) {
                add(result, ip.group(1), ip.group(2), ip.group(3), line, target, vlan);
                context = ip.group(1);
                pending = "";
            } else if (header.matches()) {
                context = header.group(1);
                pending = "";
            } else if (!line.isEmpty()) {
                pending = "";
            }
        }
        if (result.isEmpty()) {
            result.add(SwitchGuideEngine.finding(Status.UNKNOWN, "缺少可确认的接口状态", "No confirmed interface state",
                    "未找到目标接口的明确物理及协议状态；配置文本、描述或版本信息不能证明端口正常。",
                    "No explicit state was recognized for the target interface. Configuration, descriptions, or version text cannot prove a working port.",
                    "核对接口编号，收集该接口详细输出；VLAN 问题还需查看三层接口概况。",
                    "Verify the interface ID and collect its detailed output; use the layer-3 interface overview for VLAN issues.", ""));
        }
        return result;
    }

    private static void add(List<DiagnosticFinding> result, String name, String physical,
                            String protocol, String evidence, String target, boolean vlan) {
        if (name.isEmpty() || !matchesTarget(name, target, vlan)) return;
        String state = physical.toLowerCase(Locale.ROOT);
        Status status;
        String titleZh;
        String titleEn;
        String explanationZh;
        String explanationEn;
        String nextZh;
        String nextEn;
        if (state.equals("err-disabled")) {
            status = Status.FAIL;
            titleZh = "端口被保护机制停用"; titleEn = "Port disabled by protection";
            explanationZh = "设备明确报告 err-disabled，当前端口不能正常转发。";
            explanationEn = "The device explicitly reports err-disabled; the port cannot forward normally.";
            nextZh = "检查端口日志，定位环路、端口安全或链路抖动原因，解决原因后再由管理员恢复。";
            nextEn = "Inspect port logs for loops, port security, or link flaps. Resolve the cause before an administrator restores it.";
        } else if (state.contains("admin") || state.equals("adm") || state.equals("*down") || state.equals("disabled")) {
            status = Status.WARNING;
            titleZh = "端口被手动关闭"; titleEn = "Port is administratively disabled";
            explanationZh = "这通常是配置关闭，不等于网线损坏；端口也可能是按设计停用。";
            explanationEn = "This normally indicates an administrative setting, not a broken cable; the port may be intentionally disabled.";
            nextZh = "先确认端口用途及停用原因，请管理员审核是否应该启用。";
            nextEn = "Confirm the port's purpose and why it was disabled; ask an administrator to review whether it should be enabled.";
        } else if (state.equals("down") || state.equals("notconnect")) {
            status = Status.WARNING;
            titleZh = "端口没有建立物理连接"; titleEn = "Physical link is not established";
            explanationZh = "当前没有链路信号；空闲端口也会显示 DOWN，不能据此认定设备故障。";
            explanationEn = "No physical link is reported. Unused ports also show DOWN, so this alone does not prove a device fault.";
            nextZh = "若该端口应接设备，请检查两端电源、网线或光模块，确认插对端口。";
            nextEn = "If the port should be connected, check both devices' power, cable or optics, and the intended ports.";
        } else if (protocol.equalsIgnoreCase("down")) {
            status = Status.WARNING;
            titleZh = "物理连接正常，但协议未就绪"; titleEn = "Physical link is up but protocol is down";
            explanationZh = "有物理链路不代表该接口已经能够承载业务。";
            explanationEn = "A physical link does not establish that this interface can carry traffic.";
            nextZh = "核对两端配置；三层 VLAN 接口需检查 VLAN 成员端口和上联状态。";
            nextEn = "Compare both ends' settings; for a VLAN interface, check VLAN member ports and uplink state.";
        } else if (protocol.equalsIgnoreCase("up") || state.equals("connected")) {
            status = Status.PASS;
            titleZh = "检测到已建立的链路"; titleEn = "Established link detected";
            explanationZh = "仅确认这段输出中的链路状态；不代表 DHCP、VLAN、网关或互联网都正常。";
            explanationEn = "This confirms only the link state in this excerpt, not DHCP, VLAN, gateway, or Internet connectivity.";
            nextZh = "仍不能上网时继续 VLAN/网关向导，并核对错误计数是否持续增加。";
            nextEn = "If connectivity still fails, continue with the VLAN/gateway guide and check whether error counters keep increasing.";
        } else {
            status = Status.UNKNOWN;
            titleZh = "协议状态缺失"; titleEn = "Protocol state is missing";
            explanationZh = "只看到了部分接口状态，暂时无法确认链路完整就绪。";
            explanationEn = "Only part of the interface state is available; a fully ready link is not established.";
            nextZh = "读取该接口详细信息，确认物理状态和协议状态。";
            nextEn = "Read the detailed interface output and verify both physical and protocol states.";
        }
        result.add(SwitchGuideEngine.finding(status, name + "：" + titleZh, name + ": " + titleEn,
                explanationZh, explanationEn, nextZh, nextEn, evidence));
    }

    private static boolean matchesTarget(String name, String target, boolean vlan) {
        String normalized = normalize(name);
        if (vlan) {
            if (!normalized.matches("vlan(?:if|-interface)?[0-9]+")) return false;
            return target.isEmpty() || normalized.replaceFirst("^vlan(?:if|-interface)?", "").equals(target);
        }
        return target.isEmpty() || normalized.equals(normalize(target));
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceFirst("^gigabitethernet", "gi")
                .replaceFirst("^ten-gigabitethernet", "te")
                .replaceFirst("^tengigabitethernet", "te")
                .replaceFirst("^fastethernet", "fa");
    }
}
