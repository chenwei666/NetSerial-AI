package com.chenwei666.netserial.terminal;

import java.util.Objects;

public final class TerminalControlEncoder {

    public byte[] encode(ControlKey key) {
        Objects.requireNonNull(key, "key");
        if (key == ControlKey.TAB) {
            return new byte[]{0x09};
        }
        throw new IllegalArgumentException("Unsupported control key: " + key);
    }
}
