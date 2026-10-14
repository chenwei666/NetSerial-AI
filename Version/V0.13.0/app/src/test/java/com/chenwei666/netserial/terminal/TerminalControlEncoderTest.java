package com.chenwei666.netserial.terminal;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class TerminalControlEncoderTest {

    @Test
    public void tabEncodesAsAsciiHorizontalTabWithoutNewline() {
        TerminalControlEncoder encoder = new TerminalControlEncoder();

        byte[] encoded = encoder.encode(ControlKey.TAB);

        assertArrayEquals(new byte[]{0x09}, encoded);
    }

    @Test
    public void navigationAndControlKeysUseTerminalSequences() {
        TerminalControlEncoder encoder = new TerminalControlEncoder();
        assertArrayEquals(new byte[]{0x1b}, encoder.encode(ControlKey.ESCAPE));
        assertArrayEquals(new byte[]{0x1b, 0x5b, 0x41}, encoder.encode(ControlKey.ARROW_UP));
        assertArrayEquals(new byte[]{0x1b, 0x5b, 0x42}, encoder.encode(ControlKey.ARROW_DOWN));
        assertArrayEquals(new byte[]{0x1b, 0x5b, 0x43}, encoder.encode(ControlKey.ARROW_RIGHT));
        assertArrayEquals(new byte[]{0x1b, 0x5b, 0x44}, encoder.encode(ControlKey.ARROW_LEFT));
        assertArrayEquals(new byte[]{0x03}, encoder.encode(ControlKey.CTRL_C));
        assertArrayEquals(new byte[]{0x1a}, encoder.encode(ControlKey.CTRL_Z));
    }
}
