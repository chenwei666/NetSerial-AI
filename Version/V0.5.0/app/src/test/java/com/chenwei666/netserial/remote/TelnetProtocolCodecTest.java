package com.chenwei666.netserial.remote;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class TelnetProtocolCodecTest {
    @Test
    public void stripsNegotiationAndRejectsUnsupportedWill() {
        TelnetProtocolCodec codec = new TelnetProtocolCodec();
        TelnetFrame frame = codec.process(new byte[]{'L', 'o', (byte) 255, (byte) 251, 42, 'g', 'i', 'n'});
        assertArrayEquals("Login".getBytes(StandardCharsets.US_ASCII), frame.getPayload());
        assertArrayEquals(new byte[]{(byte) 255, (byte) 254, 42}, frame.getResponse());
    }

    @Test
    public void acceptsServerEchoAndTerminalTypeRequest() {
        TelnetProtocolCodec codec = new TelnetProtocolCodec();
        TelnetFrame echo = codec.process(new byte[]{(byte) 255, (byte) 251, 1});
        assertArrayEquals(new byte[]{(byte) 255, (byte) 253, 1}, echo.getResponse());

        TelnetFrame terminalType = codec.process(new byte[]{(byte) 255, (byte) 250, 24, 1,
                (byte) 255, (byte) 240});
        assertArrayEquals(new byte[]{(byte) 255, (byte) 250, 24, 0,
                'X', 'T', 'E', 'R', 'M', (byte) 255, (byte) 240}, terminalType.getResponse());
    }

    @Test
    public void escapesOutgoingIac() {
        TelnetProtocolCodec codec = new TelnetProtocolCodec();
        assertArrayEquals(new byte[]{'A', (byte) 255, (byte) 255, 'B'},
                codec.encodeOutgoing(new byte[]{'A', (byte) 255, 'B'}));
    }

    @Test
    public void preservesEscapedIacAndStateAcrossChunks() {
        TelnetProtocolCodec codec = new TelnetProtocolCodec();
        TelnetFrame first = codec.process(new byte[]{'A', (byte) 255});
        TelnetFrame second = codec.process(new byte[]{(byte) 255, 'B'});
        assertArrayEquals(new byte[]{'A'}, first.getPayload());
        assertArrayEquals(new byte[]{(byte) 255, 'B'}, second.getPayload());
    }

    @Test
    public void skipsSubnegotiationPayload() {
        TelnetProtocolCodec codec = new TelnetProtocolCodec();
        TelnetFrame frame = codec.process(new byte[]{'A', (byte) 255, (byte) 250, 24, 1,
                (byte) 255, (byte) 240, 'B'});
        assertArrayEquals(new byte[]{'A', 'B'}, frame.getPayload());
    }
}
