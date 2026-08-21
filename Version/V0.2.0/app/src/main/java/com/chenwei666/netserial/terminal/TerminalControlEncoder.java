package com.chenwei666.netserial.terminal;

import java.util.Objects;

public final class TerminalControlEncoder {

    public byte[] encode(ControlKey key) {
        Objects.requireNonNull(key, "key");
        switch (key) {
            case TAB: return new byte[]{0x09};
            case ESCAPE: return new byte[]{0x1b};
            case ARROW_UP: return new byte[]{0x1b, 0x5b, 0x41};
            case ARROW_DOWN: return new byte[]{0x1b, 0x5b, 0x42};
            case ARROW_RIGHT: return new byte[]{0x1b, 0x5b, 0x43};
            case ARROW_LEFT: return new byte[]{0x1b, 0x5b, 0x44};
            case BACKSPACE: return new byte[]{0x08};
            case DELETE: return new byte[]{0x7f};
            case QUESTION_MARK: return new byte[]{0x3f};
            case PIPE: return new byte[]{0x7c};
            case CTRL_C: return new byte[]{0x03};
            case CTRL_Z: return new byte[]{0x1a};
            default: throw new IllegalArgumentException("Unsupported control key: " + key);
        }
    }
}
