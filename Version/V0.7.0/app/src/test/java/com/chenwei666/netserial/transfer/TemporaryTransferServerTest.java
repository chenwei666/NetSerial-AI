package com.chenwei666.netserial.transfer;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class TemporaryTransferServerTest {
    @Test public void httpRequiresTokenAndServesExactBytes() throws Exception {
        File file = File.createTempFile("netserial-http-", ".cfg");
        byte[] expected = "hostname edge-01\n".getBytes(StandardCharsets.UTF_8);
        Files.write(file.toPath(), expected);
        TemporaryTransferPolicy policy = new TemporaryTransferPolicy(InetAddress.getLoopbackAddress(), 10_000, 2);
        try (TemporaryHttpFileServer server = new TemporaryHttpFileServer(policy, file)) {
            String address = server.start();
            HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[128];
            for (int count; (count = connection.getInputStream().read(buffer)) >= 0;) output.write(buffer, 0, count);
            assertArrayEquals(expected, output.toByteArray());
            assertEquals(404, ((HttpURLConnection) new URL(address.replaceFirst("/[0-9a-f]+/", "/wrong/"))
                    .openConnection()).getResponseCode());
        } finally {
            assertTrue(file.delete() || !file.exists());
        }
    }

    @Test public void tftpServesReadOnlyOctetTransfer() throws Exception {
        File file = File.createTempFile("netserial-tftp-", ".cfg");
        byte[] expected = "display current-configuration".getBytes(StandardCharsets.US_ASCII);
        Files.write(file.toPath(), expected);
        TemporaryTransferPolicy policy = new TemporaryTransferPolicy(InetAddress.getLoopbackAddress(), 10_000, 1);
        try (TemporaryTftpReadServer server = new TemporaryTftpReadServer(policy, file);
             DatagramSocket client = new DatagramSocket()) {
            int port = server.start();
            byte[] name = server.getPublishedName().getBytes(StandardCharsets.US_ASCII);
            byte[] mode = "octet".getBytes(StandardCharsets.US_ASCII);
            byte[] request = new byte[2 + name.length + 1 + mode.length + 1];
            request[1] = 1;
            System.arraycopy(name, 0, request, 2, name.length);
            System.arraycopy(mode, 0, request, 3 + name.length, mode.length);
            client.setSoTimeout(3_000);
            client.send(new DatagramPacket(request, request.length, InetAddress.getLoopbackAddress(), port));
            byte[] response = new byte[516];
            DatagramPacket packet = new DatagramPacket(response, response.length);
            client.receive(packet);
            assertEquals(3, response[1]);
            byte[] actual = java.util.Arrays.copyOfRange(response, 4, packet.getLength());
            assertArrayEquals(expected, actual);
            byte[] ack = new byte[]{0, 4, response[2], response[3]};
            client.send(new DatagramPacket(ack, ack.length, packet.getAddress(), packet.getPort()));
        } finally {
            assertTrue(file.delete() || !file.exists());
        }
    }

    @Test public void tftpRejectsSpoofedAckEndpoint() throws Exception {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        DatagramPacket request = new DatagramPacket(new byte[8], 8, loopback, 31000);
        byte[] ack = new byte[]{0, 4, 0, 1};
        DatagramPacket valid = new DatagramPacket(ack, ack.length, loopback, 31000);
        DatagramPacket spoofedPort = new DatagramPacket(ack, ack.length, loopback, 31001);
        assertTrue(TemporaryTftpReadServer.isExpectedAck(request, valid, 1));
        assertFalse(TemporaryTftpReadServer.isExpectedAck(request, spoofedPort, 1));
    }
}
