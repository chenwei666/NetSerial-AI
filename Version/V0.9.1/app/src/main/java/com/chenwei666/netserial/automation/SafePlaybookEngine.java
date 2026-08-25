package com.chenwei666.netserial.automation;

import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Produces read-only, reviewable command batches. Execution remains at the terminal safety seam. */
public final class SafePlaybookEngine {
    public PlaybookPlan plan(Vendor vendor, PlaybookType type, String parameter) {
        Objects.requireNonNull(vendor, "vendor");
        Objects.requireNonNull(type, "type");
        if (vendor == Vendor.GENERIC) throw new IllegalArgumentException("supported vendor required");
        List<String> commands = new ArrayList<>();
        switch (type) {
            case PRE_CHANGE_HEALTH:
                addHealth(vendor, commands);
                break;
            case INTERFACE_DIAGNOSIS:
                addInterface(vendor, validateInterface(parameter), commands);
                break;
            case VLAN_AUDIT:
                addVlan(vendor, validateVlan(parameter), commands);
                break;
            case NEIGHBOR_DISCOVERY:
                addNeighbors(vendor, commands);
                break;
            case SECURITY_BASELINE:
                addSecurity(vendor, commands);
                break;
            default:
                throw new IllegalArgumentException("unsupported playbook");
        }
        return new PlaybookPlan(type, commands, RiskLevel.R1_READ_ONLY, Arrays.asList(
                "Stop if the target identity changes",
                "Stop on authorization or syntax error",
                "Stop if output indicates device overload"));
    }

    private static void addHealth(Vendor v, List<String> c) {
        if (isDisplay(v)) {
            c.add("display version"); c.add("display device"); c.add("display interface brief");
            c.add("display interface counters errors"); c.add("display cpu-usage"); c.add("display memory-usage");
            c.add("display current-configuration");
        } else {
            c.add("show version"); c.add("show inventory"); c.add("show interfaces status");
            c.add("show interfaces counters errors"); c.add("show processes cpu"); c.add("show memory");
            c.add("show running-config");
        }
    }

    private static void addInterface(Vendor v, String target, List<String> c) {
        if (isDisplay(v)) {
            c.add("display interface " + target);
            c.add("display transceiver interface " + target + " verbose");
            c.add("display mac-address interface " + target);
        } else {
            c.add("show interfaces " + target);
            c.add(v == Vendor.CISCO_IOS ? "show interfaces " + target + " transceiver detail"
                    : "show interfaces " + target + " transceiver");
            c.add("show mac address-table interface " + target);
        }
    }

    private static void addVlan(Vendor v, int vlan, List<String> c) {
        if (isDisplay(v)) {
            c.add("display vlan " + vlan);
            c.add("display mac-address vlan " + vlan);
            c.add("display arp vlan " + vlan);
        } else {
            c.add("show vlan id " + vlan);
            c.add("show mac address-table vlan " + vlan);
            c.add("show ip arp vlan " + vlan);
        }
    }

    private static void addNeighbors(Vendor v, List<String> c) {
        if (v == Vendor.CISCO_IOS) {
            c.add("show lldp neighbors detail"); c.add("show cdp neighbors detail");
            c.add("show etherchannel summary");
        } else if (v == Vendor.RUIJIE_RGOS) {
            c.add("show lldp neighbors detail"); c.add("show aggregateport summary");
        } else {
            c.add("display lldp neighbor-information list");
            c.add(v == Vendor.HUAWEI_VRP ? "display eth-trunk" : "display link-aggregation summary");
        }
    }

    private static void addSecurity(Vendor v, List<String> c) {
        if (isDisplay(v)) {
            c.add("display current-configuration | include ssh|stelnet|telnet|http|snmp|ntp");
            c.add("display ssh server status"); c.add("display ip https"); c.add("display snmp-agent sys-info");
        } else {
            c.add("show running-config | include ssh|telnet|http|snmp|ntp");
            c.add("show ip ssh"); c.add("show web-server status"); c.add("show snmp");
        }
    }

    private static boolean isDisplay(Vendor vendor) {
        return vendor == Vendor.H3C_COMWARE || vendor == Vendor.HUAWEI_VRP;
    }

    private static String validateInterface(String value) {
        String target = Objects.requireNonNull(value, "interface").trim();
        if (!target.matches("[A-Za-z][A-Za-z0-9._/-]{1,47}")) throw new IllegalArgumentException("interface");
        return target;
    }

    private static int validateVlan(String value) {
        int vlan;
        try { vlan = Integer.parseInt(Objects.requireNonNull(value, "vlan").trim()); }
        catch (RuntimeException exception) { throw new IllegalArgumentException("vlan", exception); }
        if (vlan < 1 || vlan > 4094) throw new IllegalArgumentException("vlan");
        return vlan;
    }
}
