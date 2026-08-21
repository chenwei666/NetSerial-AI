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
}
