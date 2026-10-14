package com.chenwei666.netserial.diagnostics;

import com.chenwei666.netserial.device.Vendor;

import java.util.ArrayList;
import java.util.List;

/** Creates a read-only evidence chain from IP/MAC/interface to switching state. */
public final class PortTroubleshootingEngine {
    public PortTroubleshootingPlan plan(Vendor vendor, PortLookupType type, String value) {
        if (type == null) throw new IllegalArgumentException("Lookup type is required");
        String target = validate(value);
        if (vendor == null) vendor = Vendor.GENERIC;
        List<TroubleshootingStep> steps = new ArrayList<>();
        if (type == PortLookupType.IP) {
            steps.add(step("RESOLVE", arp(vendor, target), "Resolve the IP address to a MAC address."));
            target = "<resolved-mac>";
        }
        if (type != PortLookupType.INTERFACE) {
            steps.add(step("LOCATE", mac(vendor, target), "Locate the learned VLAN and interface."));
            target = "<resolved-interface>";
        }
        steps.add(step("STATUS", interfaceDetail(vendor, target), "Inspect link state, speed, duplex, and counters."));
        steps.add(step("VLAN", vlan(vendor, target), "Verify access/trunk VLAN membership."));
        steps.add(step("LOOP", spanningTree(vendor, target), "Verify spanning-tree role and protection state."));
        steps.add(step("BUNDLE", aggregation(vendor, target), "Verify aggregation membership and consistency."));
        steps.add(step("POWER", poe(vendor, target), "Inspect PoE delivery when supported."));
        steps.add(step("OPTICS", optics(vendor, target), "Inspect optical diagnostics when supported."));
        return new PortTroubleshootingPlan(steps);
    }

    private static TroubleshootingStep step(String phase, String command, String purpose) {
        return new TroubleshootingStep(phase, command, purpose);
    }

    private static String validate(String value) {
        String result = value == null ? "" : value.trim();
        if (result.isEmpty() || result.length() > 64 || !result.matches("[A-Za-z0-9:._/-]+")) {
            throw new IllegalArgumentException("Invalid lookup value");
        }
        return result;
    }

    private static String arp(Vendor vendor, String target) { return huawei(vendor) ? "display arp | include " + target : cisco(vendor) ? "show ip arp " + target : display(vendor, "arp | include " + target, "arp " + target); }
    private static String mac(Vendor vendor, String target) { return huawei(vendor) ? "display mac-address " + target : cisco(vendor) ? "show mac address-table address " + target : display(vendor, "mac-address " + target, "mac address-table address " + target); }
    private static String interfaceDetail(Vendor vendor, String target) { return huawei(vendor) ? "display interface " + target : cisco(vendor) ? "show interfaces " + target : display(vendor, "interface " + target, "interfaces " + target); }
    private static String vlan(Vendor vendor, String target) { return huawei(vendor) ? "display port vlan " + target : cisco(vendor) ? "show interfaces " + target + " switchport" : display(vendor, "port vlan interface " + target, "interfaces " + target + " switchport"); }
    private static String spanningTree(Vendor vendor, String target) { return huawei(vendor) ? "display stp interface " + target : cisco(vendor) ? "show spanning-tree interface " + target + " detail" : display(vendor, "stp interface " + target, "spanning-tree interface " + target); }
    private static String aggregation(Vendor vendor, String target) { return huawei(vendor) ? "display eth-trunk interface " + target : cisco(vendor) ? "show etherchannel summary" : display(vendor, "link-aggregation verbose", "aggregatePort summary"); }
    private static String poe(Vendor vendor, String target) { return huawei(vendor) ? "display poe power interface " + target : cisco(vendor) ? "show power inline " + target : display(vendor, "poe interface " + target, "poe interface " + target); }
    private static String optics(Vendor vendor, String target) { return huawei(vendor) ? "display transceiver diagnosis interface " + target : cisco(vendor) ? "show interfaces " + target + " transceiver detail" : display(vendor, "transceiver diagnosis interface " + target, "interfaces transceiver " + target); }
    private static boolean huawei(Vendor vendor) { return vendor == Vendor.HUAWEI_VRP; }
    private static boolean cisco(Vendor vendor) { return vendor == Vendor.CISCO_IOS; }
    private static String display(Vendor vendor, String h3c, String ruijie) {
        if (vendor == Vendor.RUIJIE_RGOS) return "show " + ruijie;
        if (vendor == Vendor.GENERIC) return "show " + ruijie;
        return "display " + h3c;
    }
}
