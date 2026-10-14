package com.chenwei666.netserial.guided.switches;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.guided.GuideText;
import com.chenwei666.netserial.guided.GuidedScenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Deliberately small, cross-model command set. Unsupported commands require manual review. */
final class SwitchGuideCommands {
    private SwitchGuideCommands() { }

    static List<GuideCommand> create(GuidedScenario scenario, Vendor vendor, String parameter) {
        String target = validate(scenario, parameter);
        if (vendor == null || vendor == Vendor.GENERIC) return Collections.emptyList();
        List<GuideCommand> result = new ArrayList<>();
        boolean display = vendor == Vendor.H3C_COMWARE || vendor == Vendor.HUAWEI_VRP;
        switch (scenario) {
            case IDENTIFY_DEVICE:
                add(result, display ? "display version" : "show version",
                        "读取品牌、型号和软件版本", "Read the vendor, model, and software version");
                break;
            case PORT_LINK:
                add(result, display ? (target.isEmpty() ? "display interface brief" : "display interface " + target)
                                : (target.isEmpty() ? "show interfaces status" : "show interfaces " + target),
                        "查看端口状态、速率及错误计数", "Inspect port state, speed, and error counters");
                break;
            case VLAN_GATEWAY:
                add(result, display ? "display vlan" + (target.isEmpty() ? "" : " " + target)
                                : target.isEmpty() ? (vendor == Vendor.CISCO_IOS ? "show vlan brief" : "show vlan")
                                : "show vlan id " + target,
                        "查看 VLAN 是否存在及成员端口", "Inspect VLAN existence and member ports");
                add(result, display ? "display ip interface brief" : "show ip interface brief",
                        "查看三层接口地址与状态", "Inspect layer-3 interface addresses and states");
                add(result, display ? "display ip routing-table" : "show ip route",
                        "查看网关路由；有路由不代表目标一定可达", "Inspect routes; a route does not prove reachability");
                break;
            case SWITCH_HEALTH:
                add(result, display ? "display version" : "show version",
                        "读取设备版本和运行时间", "Read software version and uptime");
                add(result, display ? "display interface brief" : "show interfaces status",
                        "查看各端口概况", "Inspect the port overview");
                add(result, display ? "display cpu-usage"
                                : vendor == Vendor.CISCO_IOS ? "show processes cpu sorted" : "show cpu",
                        "查看 CPU 使用率", "Inspect CPU utilization");
                add(result, vendor == Vendor.H3C_COMWARE ? "display memory"
                                : vendor == Vendor.HUAWEI_VRP ? "display memory-usage"
                                : vendor == Vendor.CISCO_IOS ? "show processes memory" : "show memory",
                        "查看内存；不同固件输出格式可能不同", "Inspect memory; output formats vary by firmware");
                break;
            case CONFIG_BACKUP:
                add(result, display ? "display current-configuration" : "show running-config",
                        "读取运行配置；读取后仍需单独保存备份", "Read running configuration; save a backup separately afterwards");
                break;
            default:
                break;
        }
        return Collections.unmodifiableList(result);
    }

    static String validate(GuidedScenario scenario, String parameter) {
        if (scenario == null) throw new IllegalArgumentException("scenario");
        String value = parameter == null ? "" : parameter;
        // Reject line separators before trimming so a trailing newline cannot become a valid draft.
        if (value.length() > 64 || value.matches("(?s).*[\\p{Cntrl}\\u2028\\u2029].*")) {
            throw new IllegalArgumentException("single-line parameter required");
        }
        value = value.trim();
        if (value.isEmpty()) return value;
        if (scenario == GuidedScenario.PORT_LINK) {
            if (!value.matches("[A-Za-z][A-Za-z0-9._/-]{1,47}") || !value.matches(".*[0-9].*")) {
                throw new IllegalArgumentException("interface identifier required");
            }
        } else if (scenario == GuidedScenario.VLAN_GATEWAY) {
            if (!value.matches("[0-9]{1,4}")) throw new IllegalArgumentException("VLAN must be 1–4094");
            int vlan = Integer.parseInt(value);
            if (vlan < 1 || vlan > 4094) throw new IllegalArgumentException("VLAN must be 1–4094");
            value = Integer.toString(vlan);
        } else {
            throw new IllegalArgumentException("This scenario does not accept a command parameter");
        }
        return value;
    }

    private static void add(List<GuideCommand> commands, String command, String chinese, String english) {
        commands.add(new GuideCommand(command, new GuideText(chinese, english)));
    }
}
