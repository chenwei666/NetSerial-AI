package com.chenwei666.netserial.runbook;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public final class RunbookPackCodec {
    private static final int MAX_BYTES = 1_000_000;
    private final Gson gson = new Gson();

    public byte[] encode(RunbookPack pack) {
        byte[] bytes = gson.toJson(pack).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) throw new IllegalArgumentException("Runbook pack is too large");
        return bytes;
    }

    public RunbookPack decode(byte[] document) {
        if (document == null || document.length == 0 || document.length > MAX_BYTES) {
            throw new IllegalArgumentException("Invalid runbook document");
        }
        RunbookPack value = gson.fromJson(new String(document, StandardCharsets.UTF_8), RunbookPack.class);
        if (value == null) throw new IllegalArgumentException("Invalid runbook document");
        return new RunbookPack(value.getId(), value.getVersion(), value.getAuthor(), value.getCommands());
    }
}
