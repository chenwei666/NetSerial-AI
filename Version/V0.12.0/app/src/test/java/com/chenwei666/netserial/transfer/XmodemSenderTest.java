package com.chenwei666.netserial.transfer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class XmodemSenderTest {
    @Test public void buildsChecksumPacket() {
        byte[] payload = new byte[128];
        payload[0] = 1;
        payload[127] = 2;
        byte[] packet = XmodemSender.packet(7, payload, false);
        assertEquals(132, packet.length);
        assertEquals(1, packet[0]);
        assertEquals(7, packet[1]);
        assertEquals(248, packet[2] & 0xff);
        assertEquals(3, packet[131] & 0xff);
    }

    @Test public void crcMatchesKnownVector() {
        assertEquals(0x31c3, XmodemSender.crc16("123456789".getBytes()));
    }
}
