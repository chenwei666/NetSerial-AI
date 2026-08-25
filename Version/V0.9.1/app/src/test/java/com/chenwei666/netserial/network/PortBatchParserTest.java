package com.chenwei666.netserial.network;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;

public class PortBatchParserTest {
    @Test public void parsesRangesAndDeduplicatesInInputOrder() {
        assertEquals(Arrays.asList(22, 80, 81, 82, 443),
                new PortBatchParser().parse("22, 80-82, 443, 22"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsTooManyPorts() {
        new PortBatchParser().parse("1-17");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsReverseRange() {
        new PortBatchParser().parse("100-90");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidPort() {
        new PortBatchParser().parse("22,65536");
    }
}
