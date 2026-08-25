package com.chenwei666.netserial.commands;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Curated offline command reference. Commands are drafts and are never sent automatically. */
public final class CommonCommandCatalog {
    private final List<CommonCommand> commands;

    private CommonCommandCatalog(List<CommonCommand> commands) {
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
    }

    public static CommonCommandCatalog createDefault() {
        List<CommonCommand> result = new ArrayList<>();
        addComware(result, Vendor.H3C_COMWARE);
        addComware(result, Vendor.HUAWEI_VRP);
        addIosLike(result, Vendor.CISCO_IOS);
        addIosLike(result, Vendor.RUIJIE_RGOS);
        return new CommonCommandCatalog(result);
    }

    public CommonCommandCatalog withAdditional(List<CommonCommand> additional) {
        List<CommonCommand> combined = new ArrayList<>(commands);
        if (additional != null) combined.addAll(additional);
        return new CommonCommandCatalog(combined);
    }

    public List<CommonCommand> all() {
        return commands;
    }

    public List<CommonCommand> search(Vendor vendor, CommandCategory category, String query, int limit) {
        Objects.requireNonNull(vendor, "vendor");
        if (limit < 1 || limit > 200) throw new IllegalArgumentException("limit must be between 1 and 200");
        String normalized = normalize(query);
        List<CommonCommand> matches = new ArrayList<>();
        for (CommonCommand command : commands) {
            if (command.getVendor() != vendor) continue;
            if (category != null && command.getCategory() != category) continue;
            String searchable = normalize(command.getCommand() + " " + command.getDescription());
            if (!normalized.isEmpty() && !searchable.contains(normalized)) continue;
            matches.add(command);
        }
        Collections.sort(matches, (first, second) -> first.getCommand().compareTo(second.getCommand()));
        if (matches.size() > limit) return new ArrayList<>(matches.subList(0, limit));
        return matches;
    }

    private static void addComware(List<CommonCommand> out, Vendor vendor) {
        boolean huawei = vendor == Vendor.HUAWEI_VRP;
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.DEVICE_INFORMATION, "display version", "Software and hardware version", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.DEVICE_INFORMATION, "display device", "Device and component state", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.INTERFACE, "display interface brief", "Interface status summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.INTERFACE, "display interface counters errors", "Interface error counters", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.VLAN, "display vlan", "VLAN summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.LAYER_3, "display ip interface brief", "IPv4 interface summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.ROUTING, "display ip routing-table", "IPv4 routing table", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.LOOP_PREVENTION, "display stp brief", "Spanning-tree summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.LINK_AGGREGATION,
                huawei ? "display eth-trunk" : "display link-aggregation summary", "Link aggregation summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.SECURITY, "display mac-address", "Learned MAC addresses", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.TROUBLESHOOTING, "display logbuffer", "Recent device logs", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.TROUBLESHOOTING, "ping 192.0.2.1", "Connectivity test; replace example address", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.TROUBLESHOOTING, "tracert 192.0.2.1", "Path test; replace example address", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.SAVE_AND_BACKUP, "display current-configuration", "Running configuration", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.SAVE_AND_BACKUP, huawei ? "save" : "save force", "Save active configuration", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.DEVICE_INFORMATION, "system-view", "Enter system configuration view", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.SYSTEM_VIEW, CommandCategory.INTERFACE, "interface GigabitEthernet 1/0/1", "Enter an interface; verify identifier", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.INTERFACE, "description UPLINK", "Set interface description", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.INTERFACE, "undo shutdown", "Enable interface", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.INTERFACE, "shutdown", "Disable interface and interrupt traffic", RiskLevel.R3_HIGH);
        add(out, vendor, CliMode.SYSTEM_VIEW, CommandCategory.VLAN, huawei ? "vlan batch 10 20" : "vlan 10", "Create VLAN; verify IDs", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.VLAN, "port link-type access", "Set access link type", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.VLAN, huawei ? "port default vlan 10" : "port access vlan 10", "Assign access VLAN", RiskLevel.R3_HIGH);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.VLAN, huawei ? "port trunk allow-pass vlan 10 20" : "port trunk permit vlan 10 20", "Allow VLANs on trunk", RiskLevel.R3_HIGH);
        add(out, vendor, CliMode.SYSTEM_VIEW, CommandCategory.ROUTING, "ip route-static 192.0.2.0 255.255.255.0 198.51.100.1", "Static route using documentation addresses", RiskLevel.R3_HIGH);
    }

    private static void addIosLike(List<CommonCommand> out, Vendor vendor) {
        boolean ruijie = vendor == Vendor.RUIJIE_RGOS;
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.DEVICE_INFORMATION, "show version", "Software and hardware version", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.DEVICE_INFORMATION, "show inventory", "Hardware inventory", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.INTERFACE, "show interfaces status", "Interface status summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.INTERFACE, "show interfaces counters errors", "Interface error counters", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.VLAN, ruijie ? "show vlan" : "show vlan brief", "VLAN summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.LAYER_3, "show ip interface brief", "IPv4 interface summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.ROUTING, "show ip route", "IPv4 routing table", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.LOOP_PREVENTION, "show spanning-tree summary", "Spanning-tree summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.LINK_AGGREGATION, ruijie ? "show aggregateport summary" : "show etherchannel summary", "Link aggregation summary", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.SECURITY, "show mac address-table", "Learned MAC addresses", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.TROUBLESHOOTING, "show logging", "Recent device logs", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.TROUBLESHOOTING, "ping 192.0.2.1", "Connectivity test; replace example address", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.TROUBLESHOOTING, "traceroute 192.0.2.1", "Path test; replace example address", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.SAVE_AND_BACKUP, "show running-config", "Running configuration", RiskLevel.R1_READ_ONLY);
        add(out, vendor, CliMode.SYSTEM_VIEW, CommandCategory.SAVE_AND_BACKUP, ruijie ? "write" : "write memory", "Save active configuration", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.USER_VIEW, CommandCategory.DEVICE_INFORMATION, "enable", "Enter privileged mode", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.SYSTEM_VIEW, CommandCategory.INTERFACE, "interface GigabitEthernet 1/0/1", "Enter an interface; verify identifier", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.INTERFACE, "description UPLINK", "Set interface description", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.INTERFACE, "no shutdown", "Enable interface", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.INTERFACE, "shutdown", "Disable interface and interrupt traffic", RiskLevel.R3_HIGH);
        add(out, vendor, CliMode.SYSTEM_VIEW, CommandCategory.VLAN, "vlan 10", "Create VLAN", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.VLAN, "switchport mode access", "Set access mode", RiskLevel.R2_CONFIGURATION);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.VLAN, "switchport access vlan 10", "Assign access VLAN", RiskLevel.R3_HIGH);
        add(out, vendor, CliMode.INTERFACE_VIEW, CommandCategory.VLAN, "switchport trunk allowed vlan 10,20", "Set trunk VLAN list", RiskLevel.R3_HIGH);
        add(out, vendor, CliMode.SYSTEM_VIEW, CommandCategory.ROUTING, "ip route 192.0.2.0 255.255.255.0 198.51.100.1", "Static route using documentation addresses", RiskLevel.R3_HIGH);
    }

    private static void add(List<CommonCommand> out, Vendor vendor, CliMode mode,
                            CommandCategory category, String command, String description, RiskLevel risk) {
        out.add(new CommonCommand(vendor, mode, category, command, description, risk));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
