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

    public static OfflineCompletionEngine createWithAdditional(List<CommonCommand> additional) {
        List<CommandEntry> commands = new ArrayList<>();
        OfflineCompletionEngine defaults = createDefault();
        commands.addAll(defaults.commands);
        if (additional != null) {
            for (CommonCommand command : additional) {
                commands.add(new CommandEntry(command.getVendor(), command.getMode(), command.getCommand()));
            }
        }
        return new OfflineCompletionEngine(commands);
    }

    private static void add(List<CommandEntry> commands, Vendor vendor, CliMode mode, String... values) {
        for (String value : values) commands.add(new CommandEntry(vendor, mode, value));
    }

    @Override
    public CompletionResult complete(CompletionRequest request) {
        String prefix = normalize(request.getInput());
        List<ScoredSuggestion> scored = new ArrayList<>();
        for (CommandEntry command : commands) {
            String normalizedCommand = normalize(command.command);
            int score = score(prefix, normalizedCommand, request.getContext());
            if (command.vendor == request.getVendor()
                    && command.cliMode == request.getCliMode() && score >= 0) {
                boolean duplicate = false;
                for (ScoredSuggestion existing : scored) {
                    if (existing.suggestion.getInsertion().equals(command.command)) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    scored.add(new ScoredSuggestion(new CompletionSuggestion(command.command,
                            CompletionSource.LOCAL_COMMAND_PACK), score));
                }
            }
        }
        Collections.sort(scored, new Comparator<ScoredSuggestion>() {
            @Override
            public int compare(ScoredSuggestion first, ScoredSuggestion second) {
                int score = Integer.compare(second.score, first.score);
                return score != 0 ? score : first.suggestion.getInsertion()
                        .compareTo(second.suggestion.getInsertion());
            }
        });
        List<CompletionSuggestion> matches = new ArrayList<>();
        for (ScoredSuggestion value : scored) matches.add(value.suggestion);
        if (matches.size() > request.getLimit()) {
            matches = new ArrayList<>(matches.subList(0, request.getLimit()));
        }
        return new CompletionResult(matches);
    }

    private static int score(String input, String command, String context) {
        if (input.isEmpty()) return 10;
        if (command.equals(input)) return 100;
        if (command.startsWith(input)) return 80;
        if (!input.contains(" ")) return -1;
        String[] tokens = input.split(" ");
        int cursor = 0;
        for (String token : tokens) {
            int match = command.indexOf(token, cursor);
            if (match < 0) return -1;
            cursor = match + token.length();
        }
        int score = 50;
        String normalizedContext = normalize(context == null ? "" : context);
        String first = command.split(" ")[0];
        if (!normalizedContext.isEmpty() && normalizedContext.contains(first)) score += 5;
        return score;
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

    private static final class ScoredSuggestion {
        private final CompletionSuggestion suggestion;
        private final int score;

        private ScoredSuggestion(CompletionSuggestion suggestion, int score) {
            this.suggestion = suggestion;
            this.score = score;
        }
    }
}
