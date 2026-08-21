package com.chenwei666.netserial.completion;

import com.chenwei666.netserial.commands.CommonCommand;
import com.chenwei666.netserial.commands.CommonCommandCatalog;
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
        for (CommonCommand command : CommonCommandCatalog.createDefault().all()) {
            commands.add(new CommandEntry(command.getVendor(), command.getMode(), command.getCommand()));
        }
        add(commands, Vendor.H3C_COMWARE, CliMode.USER_VIEW, "display", "system-view", "ping", "tracert");
        add(commands, Vendor.H3C_COMWARE, CliMode.SYSTEM_VIEW, "interface", "vlan", "undo", "quit", "return");
        add(commands, Vendor.H3C_COMWARE, CliMode.INTERFACE_VIEW, "description", "undo", "quit", "return");
        add(commands, Vendor.HUAWEI_VRP, CliMode.USER_VIEW, "display", "system-view", "ping", "tracert");
        add(commands, Vendor.HUAWEI_VRP, CliMode.SYSTEM_VIEW, "interface", "vlan batch", "undo", "quit", "return");
        add(commands, Vendor.HUAWEI_VRP, CliMode.INTERFACE_VIEW, "description", "undo", "quit", "return");
        add(commands, Vendor.CISCO_IOS, CliMode.USER_VIEW, "show", "enable", "ping", "traceroute");
        add(commands, Vendor.CISCO_IOS, CliMode.SYSTEM_VIEW, "interface", "vlan", "no", "end", "exit");
        add(commands, Vendor.CISCO_IOS, CliMode.INTERFACE_VIEW, "description", "no", "end", "exit");
        add(commands, Vendor.RUIJIE_RGOS, CliMode.USER_VIEW, "show", "enable", "ping", "traceroute");
        add(commands, Vendor.RUIJIE_RGOS, CliMode.SYSTEM_VIEW, "interface", "vlan", "no", "end", "exit");
        add(commands, Vendor.RUIJIE_RGOS, CliMode.INTERFACE_VIEW, "description", "no", "end", "exit");
        return new OfflineCompletionEngine(commands);
    }

    private static void add(List<CommandEntry> commands, Vendor vendor, CliMode mode, String... values) {
        for (String value : values) commands.add(new CommandEntry(vendor, mode, value));
    }

    @Override
    public CompletionResult complete(CompletionRequest request) {
        String prefix = normalize(request.getInput());
        List<CompletionSuggestion> matches = new ArrayList<>();
        for (CommandEntry command : commands) {
            if (command.vendor == request.getVendor()
                    && command.cliMode == request.getCliMode()
                    && normalize(command.command).startsWith(prefix)) {
                boolean duplicate = false;
                for (CompletionSuggestion existing : matches) {
                    if (existing.getInsertion().equals(command.command)) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    matches.add(new CompletionSuggestion(
                            command.command,
                            CompletionSource.LOCAL_COMMAND_PACK
                    ));
                }
            }
        }
        Collections.sort(matches, new Comparator<CompletionSuggestion>() {
            @Override
            public int compare(CompletionSuggestion first, CompletionSuggestion second) {
                return first.getInsertion().compareTo(second.getInsertion());
            }
        });
        if (matches.size() > request.getLimit()) {
            matches = new ArrayList<>(matches.subList(0, request.getLimit()));
        }
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
