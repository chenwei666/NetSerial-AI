package com.chenwei666.netserial.automation;

import java.util.List;

/** Production adapters execute against an already authenticated session; tests use an in-memory adapter. */
public interface TargetCommandAdapter {
    TargetCommandResult execute(String target, BatchStage stage, List<String> commands) throws Exception;
}
