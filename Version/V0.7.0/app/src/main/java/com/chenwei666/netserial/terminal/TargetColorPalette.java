package com.chenwei666.netserial.terminal;

public final class TargetColorPalette {
    private static final int[] COLORS = {
            0xff263238, 0xff37474f, 0xff1b5e20, 0xff0d47a1,
            0xff4a148c, 0xff6a1b1a, 0xffe65100, 0xff006064
    };

    public int colorFor(String target) {
        int hash = target == null ? 0 : target.hashCode();
        return COLORS[(hash & 0x7fffffff) % COLORS.length];
    }
}
