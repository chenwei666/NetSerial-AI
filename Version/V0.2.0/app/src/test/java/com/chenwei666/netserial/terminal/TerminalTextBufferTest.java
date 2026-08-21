package com.chenwei666.netserial.terminal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TerminalTextBufferTest {
    @Test
    public void evictsOldLinesWhenCapacityIsExceeded() {
        TerminalTextBuffer buffer = new TerminalTextBuffer(1024);
        buffer.append("old-line\n");
        for (int index = 0; index < 120; index++) {
            buffer.append("new-line-" + index + "\n");
        }
        assertFalse(buffer.snapshot().contains("old-line"));
        assertTrue(buffer.snapshot().contains("new-line-119"));
    }

    @Test
    public void tailAndClearAreBounded() {
        TerminalTextBuffer buffer = new TerminalTextBuffer(1024);
        buffer.append("abcdef");
        assertEquals("def", buffer.tail(3));
        buffer.clear();
        assertEquals("", buffer.snapshot());
    }
}
