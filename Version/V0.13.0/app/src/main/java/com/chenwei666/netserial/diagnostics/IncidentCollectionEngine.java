package com.chenwei666.netserial.diagnostics;

import com.chenwei666.netserial.device.Vendor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Creates a bounded, read-only evidence collection plan for a live incident. */
public final class IncidentCollectionEngine {
    public IncidentCollectionPlan plan(Vendor vendor) {
        Vendor selected = Objects.requireNonNull(vendor, "vendor");
        List<IncidentCollectionStep> steps = new ArrayList<>();
        Set<String> commands = new LinkedHashSet<>();
        if (displayFamily(selected)) {
            add(steps, commands, "Identity", "display clock", "Correlate timestamps");
            add(steps, commands, "Identity", "display version", "Record model, software and uptime");
            add(steps, commands, "Layer 2", "display lldp neighbor-information list", "Record physical neighbors");
            add(steps, commands, "Layer 2", "display mac-address", "Record forwarding state");
            add(steps, commands, "Layer 3", "display arp", "Record IP-to-MAC state");
            add(steps, commands, "Layer 3", "display ip routing-table", "Record active routes");
            add(steps, commands, "Events", selected == Vendor.HUAWEI_VRP
                    ? "display logbuffer" : "display logbuffer reverse", "Capture recent device events");
        } else {
            add(steps, commands, "Identity", "show clock", "Correlate timestamps");
            add(steps, commands, "Identity", "show version", "Record model, software and uptime");
            add(steps, commands, "Layer 2", "show lldp neighbors detail", "Record physical neighbors");
            if (selected == Vendor.CISCO_IOS) {
                add(steps, commands, "Layer 2", "show cdp neighbors detail", "Record Cisco neighbors");
            }
            add(steps, commands, "Layer 2", "show mac address-table", "Record forwarding state");
            add(steps, commands, "Layer 3", "show ip arp", "Record IP-to-MAC state");
            add(steps, commands, "Layer 3", "show ip route", "Record active routes");
            add(steps, commands, "Events", "show logging", "Capture recent device events");
        }
        for (String command : new SwitchHealthEngine().plan(selected).getCommands()) {
            add(steps, commands, "Health", command, "Collect bounded health evidence");
        }
        return new IncidentCollectionPlan(selected, steps, Arrays.asList(
                "Stop if device identity differs from the selected target",
                "Stop on authorization or syntax errors",
                "Stop if the device reports overload or the session becomes unstable",
                "Do not run write, reboot, reset, upgrade, or erase operations"));
    }

    private static boolean displayFamily(Vendor vendor) {
        return vendor == Vendor.H3C_COMWARE || vendor == Vendor.HUAWEI_VRP;
    }

    private static void add(List<IncidentCollectionStep> steps, Set<String> commands,
                            String category, String command, String purpose) {
        if (commands.add(command)) steps.add(new IncidentCollectionStep(category, command, purpose));
    }
}
