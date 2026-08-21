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
        commands.add(new CommandEntry(Vendor.H3C_COMWARE, CliMode.USER_VIEW, "display"));
        commands.add(new CommandEntry(Vendor.H3C_COMWARE, CliMode.SYSTEM_VIEW, "interface"));
        return new OfflineCompletionEngine(commands);
    }

    @Override
    public CompletionResult complete(CompletionRequest request) {
        String prefix = request.getInput().toLowerCase(Locale.ROOT);
        List<CompletionSuggestion> matches = new ArrayList<>();
        for (CommandEntry command : commands) {
            if (matches.size() >= request.getLimit()) {
                break;
            }
            if (command.vendor == request.getVendor()
                    && command.cliMode == request.getCliMode()
                    && command.command.startsWith(prefix)) {
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
