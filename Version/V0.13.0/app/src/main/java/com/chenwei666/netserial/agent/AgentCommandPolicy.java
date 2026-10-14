package com.chenwei666.netserial.agent;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.CommandEvaluationRequest;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.safety.RuleBasedExecutionGuard;
import java.util.Locale;
import java.util.regex.Pattern;

/** Local deterministic floor. Models cannot declare commands safe or widen this allowlist. */
public final class AgentCommandPolicy {
    public enum Decision { AUTO, CONFIRM, BLOCKED }
    private static final Pattern UNSUPPORTED = Pattern.compile(
            "^(?:do|run|shell|bash|sh|dis|disp|sys|conf|en|q|tclsh|tcl|python|python3|perl|lua|" +
            "execute|exec|script|scripting|event|event-manager|kron|scheduler|alias|macro|" +
            "delete|remove|rm|erase-file|format|copy|move|rename|more|cat|" +
            "ftp|sftp|tftp|scp|curl|wget|install|upgrade|bootrom)(?: |$).*");
    private static final Pattern CREDENTIAL = Pattern.compile(
            "(?:^| )(?:password|passwd|secret|community|token|api-key|key-string|private-key)(?: |$)");
    private static final String IFACE = "[a-z][a-z0-9./:-]{0,63}";
    private static final Pattern READ = Pattern.compile(
            "(?:show|display) version|" +
            "show (?:running-config|startup-config|inventory|clock|users)|" +
            "display (?:current-configuration|saved-configuration|device|clock|users)|" +
            "show interfaces?(?: (?:status|description|brief|" + IFACE + "))?|" +
            "display interface(?: (?:brief|" + IFACE + "))?|" +
            "show (?:ip|ipv6) interface(?: brief)?|" +
            "display (?:ip|ipv6) interface(?: brief)?|" +
            "show vlan(?: brief)?|display vlan(?: all)?|" +
            "show ip http server status|show ip http server|show ip https server status|" +
            "display ip (?:http|https)(?: server)?|" +
            "display http server|show web-server status|show service|" +
            "show (?:lldp|cdp) neighbors(?: detail)?|display lldp neighbor-information(?: verbose)?|" +
            "show (?:ip route|arp|mac address-table)|display (?:ip routing-table|arp|mac-address)");
    private static final Pattern NAVIGATION = Pattern.compile("(?:system-view|configure terminal|quit|return|end|exit)");
    private final RuleBasedExecutionGuard floor = RuleBasedExecutionGuard.createDefault();

    public Decision evaluate(String command) {
        if (!validSingleLine(command)) return Decision.BLOCKED;
        String normalized = normalize(command);
        if (UNSUPPORTED.matcher(normalized).matches() || CREDENTIAL.matcher(normalized).find())
            return Decision.BLOCKED;
        RiskLevel risk = floor.evaluate(new CommandEvaluationRequest(Vendor.GENERIC, CliMode.UNKNOWN,
                command, RiskLevel.R0_INFORMATIONAL)).getEffectiveRisk();
        if (risk.ordinal() >= RiskLevel.R3_HIGH.ordinal()) return Decision.CONFIRM;
        if (isReadOnly(command) || isNavigation(command)) return Decision.AUTO;
        if (normalized.matches("vlan [0-9]{1,4}") && validVlan(normalized.substring(5))) return Decision.AUTO;
        // Runner must additionally prove a VLAN context before using this routine configuration exemption.
        if (command.trim().matches("(?i)description [a-z0-9][a-z0-9 _.-]{0,95}")) return Decision.AUTO;
        return Decision.CONFIRM;
    }

    public static boolean isReadOnly(String command) {
        if (!validSingleLine(command)) return false;
        String normalized = normalize(command);
        if (READ.matcher(normalized).matches()) return true;
        if (normalized.matches("(?:show|display) vlan [0-9]{1,4}"))
            return validVlan(normalized.substring(normalized.lastIndexOf(' ') + 1));
        return false;
    }

    public static boolean isNavigation(String command) {
        return validSingleLine(command) && NAVIGATION.matcher(normalize(command)).matches();
    }

    private static boolean validSingleLine(String command) {
        if (command == null || command.trim().isEmpty() || command.length() > 512) return false;
        for (int index = 0; index < command.length(); index++) {
            char c = command.charAt(index);
            // Terminal controls, shell/CLI chaining, redirection, quoting and Unicode lookalikes fail closed.
            if (c < 32 || c > 126 || ";|&><`$\\\"'{}[]!?#".indexOf(c) >= 0) return false;
        }
        return true;
    }

    private static boolean validVlan(String number) {
        int value = Integer.parseInt(number);
        return value >= 1 && value <= 4094;
    }

    private static String normalize(String command) {
        return command.trim().toLowerCase(Locale.ROOT).replaceAll(" +", " ");
    }
}
