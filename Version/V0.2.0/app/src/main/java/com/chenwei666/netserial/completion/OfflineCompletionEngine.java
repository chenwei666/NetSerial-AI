package com.chenwei666.netserial.completion;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class OfflineCompletionEngine implements CompletionEngine {
    private final List<CommandEntry> commands;

    private OfflineCompletionEngine(List<CommandEntry> commands) {
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
    }

    public static OfflineCompletionEngine createDefault() {
        List<CommandEntry> commands = new ArrayList<>();
        add(commands, Vendor.H3C_COMWARE, CliMode.USER_VIEW,
                "display", "display current-configuration", "display interface brief", "display version",
                "display vlan", "display ip interface brief", "system-view", "ping", "tracert");
        add(commands, Vendor.H3C_COMWARE, CliMode.SYSTEM_VIEW,
                "interface", "vlan", "undo", "save force", "display current-configuration",
                "ip route-static", "link-aggregation group", "quit", "return");
        add(commands, Vendor.H3C_COMWARE, CliMode.INTERFACE_VIEW,
                "description", "port link-type access", "port access vlan", "port trunk permit vlan",
                "undo shutdown", "shutdown", "speed", "duplex", "quit", "return");

        add(commands, Vendor.HUAWEI_VRP, CliMode.USER_VIEW,
                "display current-configuration", "display interface brief", "display version",
                "display vlan", "display ip interface brief", "system-view", "ping", "tracert");
        add(commands, Vendor.HUAWEI_VRP, CliMode.SYSTEM_VIEW,
                "interface", "vlan batch", "undo", "save", "display current-configuration",
                "ip route-static", "eth-trunk", "quit", "return");
        add(commands, Vendor.HUAWEI_VRP, CliMode.INTERFACE_VIEW,
                "description", "port link-type access", "port default vlan", "port trunk allow-pass vlan",
                "undo shutdown", "shutdown", "speed", "duplex", "quit", "return");

        add(commands, Vendor.CISCO_IOS, CliMode.USER_VIEW,
                "show running-config", "show interfaces status", "show version", "show vlan brief",
                "show ip interface brief", "enable", "ping", "traceroute");
        add(commands, Vendor.CISCO_IOS, CliMode.SYSTEM_VIEW,
                "interface", "vlan", "no", "write memory", "show running-config",
                "ip route", "port-channel", "end", "exit");
        add(commands, Vendor.CISCO_IOS, CliMode.INTERFACE_VIEW,
                "description", "switchport mode access", "switchport access vlan",
                "switchport trunk allowed vlan", "no shutdown", "shutdown", "speed", "duplex", "end", "exit");

        add(commands, Vendor.RUIJIE_RGOS, CliMode.USER_VIEW,
                "show running-config", "show interfaces status", "show version", "show vlan",
                "show ip interface brief", "enable", "ping", "traceroute");
        add(commands, Vendor.RUIJIE_RGOS, CliMode.SYSTEM_VIEW,
                "interface", "vlan", "no", "write", "show running-config",
                "ip route", "aggregateport", "end", "exit");
        add(commands, Vendor.RUIJIE_RGOS, CliMode.INTERFACE_VIEW,
                "description", "switchport mode access", "switchport access vlan",
                "switchport trunk allowed vlan", "no shutdown", "shutdown", "speed", "duplex", "end", "exit");
        return new OfflineCompletionEngine(commands);
    }

    private static void add(List<CommandEntry> commands, Vendor vendor, CliMode mode,
                            String... values) {
        for (String value : values) {
            commands.add(new CommandEntry(vendor, mode, value));
        }
    }

    @Override
    public CompletionResult complete(CompletionRequest request) {
        String prefix = normalize(request.getInput());
        List<CompletionSuggestion> matches = new ArrayList<>();
        for (CommandEntry command : commands) {
            if (matches.size() >= request.getLimit()) {
                break;
            }
            if (command.vendor == request.getVendor()
                    && command.cliMode == request.getCliMode()
                    && normalize(command.command).startsWith(prefix)) {
                matches.add(new CompletionSuggestion(
                        command.command,
                        CompletionSource.LOCAL_COMMAND_PACK
                ));
            }
        }
        Collections.sort(matches, new Comparator<CompletionSuggestion>() {
            @Override
            public int compare(CompletionSuggestion first, CompletionSuggestion second) {
                return first.getInsertion().compareTo(second.getInsertion());
            }
        });
        return new CompletionResult(matches);
    }

    private static String normalize(String input) {
        return input.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static final class CommandEntry {
        private final Vendor vendor;
        private final CliMode cliMode;
        private final String command;

        private CommandEntry(Vendor vendor, CliMode cliMode, String command) {
            this.vendor = vendor;
            this.cliMode = cliMode;
            this.command = command;
        }
    }
}
