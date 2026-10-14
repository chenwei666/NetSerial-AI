package com.chenwei666.netserial.network.toolbox;

import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class AdvancedProbesTest {
    private static final String NAME = "example.test";
    private static byte[] response(int type, byte[] rdata) throws Exception {
        byte[] query = DnsWire.query(42, NAME, type);
        query[2] = (byte) 0x81; query[3] = (byte) 0x80; query[7] = 1;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); bytes.write(query);
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeShort(0xc00c); out.writeShort(type); out.writeShort(1); out.writeInt(300);
        out.writeShort(rdata.length); out.write(rdata); return bytes.toByteArray();
    }
    @Test public void dnsAWithCompressedOwner() throws Exception {
        DnsWire.Reply r = DnsWire.parse(response(1, new byte[]{(byte)192, 0, 2, 1}),42, NAME,1);
        assertEquals("192.0.2.1",r.answers.get(0).value); assertEquals(300,r.answers.get(0).ttl);
    }
    @Test public void dnsSupportsUnderscoreTxtOwnerAndIdn() throws Exception {
        assertEquals("_dmarc.example.test",DnsWire.normalizeName("_dmarc.Example.Test."));
        assertTrue(DnsWire.normalizeName("测试.example").startsWith("xn--"));
        assertTrue(DnsWire.query(1,"_dmarc.example.test",16).length>12);
    }
    @Test public void dnsAaaa() throws Exception {
        byte[] address = new byte[16]; address[0]=0x20; address[1]=1; address[15]=1;
        assertEquals("AAAA",DnsWire.parse(response(28,address),42,NAME,28).answers.get(0).type);
    }
    @Test public void dnsTextChunksAndControlSanitizing() throws Exception {
        assertEquals("one two ",DnsWire.parse(response(16,new byte[]{3,'o','n','e',4,'t','w','o',10}),42,NAME,16).answers.get(0).value);
    }
    @Test public void dnsNameAndMxRecords() throws Exception {
        for(int type:new int[]{2,5,12}) assertEquals(NAME,DnsWire.parse(response(type,new byte[]{(byte)0xc0,12}),42,NAME,type).answers.get(0).value);
        assertEquals("10 " + NAME,DnsWire.parse(response(15,new byte[]{0,10,(byte)0xc0,12}),42,NAME,15).answers.get(0).value);
    }
    @Test(expected=IOException.class) public void dnsRejectsWrongTransaction() throws Exception { DnsWire.parse(response(1,new byte[4]),43,NAME,1); }
    @Test(expected=IOException.class) public void dnsRejectsWrongQuestion() throws Exception { DnsWire.parse(response(1,new byte[4]),42,"other.test",1); }
    @Test(expected=IOException.class) public void dnsRejectsTruncatedAddress() throws Exception { DnsWire.parse(response(1,new byte[3]),42,NAME,1); }
    @Test(expected=IOException.class) public void dnsRejectsTextOverflow() throws Exception { DnsWire.parse(response(16,new byte[]{8,'a'}),42,NAME,16); }
    @Test(expected=IOException.class) public void dnsRejectsPointerLoop() throws Exception {
        byte[] data=response(5,new byte[]{(byte)0xc0,12}); data[12]=(byte)0xc0; data[13]=12; DnsWire.parse(data,42,NAME,5);
    }
    @Test(expected=IOException.class) public void dnsRejectsTrailingBytes() throws Exception {
        byte[] data=response(1,new byte[4]); DnsWire.parse(Arrays.copyOf(data,data.length+1),42,NAME,1);
    }
    @Test public void dnsNxDomainIsNotSuccess() throws Exception {
        byte[] data=DnsWire.query(42,NAME,1); data[2]=(byte)0x81;data[3]=(byte)0x83;
        assertEquals(3,DnsWire.parse(data,42,NAME,1).rcode);
    }
    @Test public void everyTruncationRejectsCleanly() throws Exception {
        byte[] data=response(1,new byte[]{127,0,0,1});
        for(int size=0;size<data.length;size++) {
            try {DnsWire.parse(Arrays.copyOf(data,size),42,NAME,1);fail("accepted truncation at "+size);}
            catch(IOException expected){}
        }
    }
    @Test public void boundedRandomFramesNeverEscapeAsRuntimeCrash() throws Exception {
        java.util.Random random=new java.util.Random(1035);
        for(int i=0;i<500;i++) {
            byte[] data=new byte[12+random.nextInt(300)];random.nextBytes(data);
            data[0]=0;data[1]=42;data[2]=(byte)0x81;data[3]=(byte)0x80;
            try {DnsWire.parse(data,42,NAME,1);}catch(IOException expected){}
        }
    }
    @Test(expected=IllegalArgumentException.class) public void explicitEmptyServerIsNotSystemFallback() throws Exception {
        new AdvancedProbes().dns(request(NAME,"A @"),session());
    }
    @Test public void reverseIpv4AndIpv6() throws Exception {
        assertEquals("1.2.0.192.in-addr.arpa",AdvancedProbes.reverseName(AdvancedProbes.literal("192.0.2.1")));
        String ip6=AdvancedProbes.reverseName(AdvancedProbes.literal("::1"));assertTrue(ip6.startsWith("1.0.0.0."));assertTrue(ip6.endsWith("ip6.arpa"));
    }
    @Test(expected=IllegalArgumentException.class) public void noHiddenDnsLookupForServer() throws Exception { AdvancedProbes.literal("dns.example.test"); }
    @Test public void dnsNoServerUnsupportedForRecordQuery() throws Exception {
        assertEquals(ToolResult.Status.UNSUPPORTED,new AdvancedProbes().dns(request(NAME,"MX"),session()).status);
    }
    @Test public void magicPacketContainsSixFfAndSixteenMacs() {
        byte[] packet=AdvancedProbes.magicPacket("02:11:22:33:44:55");assertEquals(102,packet.length);
        for(int i=0;i<6;i++)assertEquals((byte)255,packet[i]);
        for(int i=0;i<16;i++)assertArrayEquals(new byte[]{2,17,34,51,68,85},Arrays.copyOfRange(packet,6+i*6,12+i*6));
    }
    @Test(expected=IllegalArgumentException.class) public void wolRejectsMulticastMac() { AdvancedProbes.magicPacket("ff:ff:ff:ff:ff:ff"); }
    @Test(expected=IllegalArgumentException.class) public void wolRejectsZeroMac() { AdvancedProbes.magicPacket("00:00:00:00:00:00"); }
    @Test(expected=IllegalArgumentException.class) public void httpRejectsCredentials() throws Exception { AdvancedProbes.httpUri("https://user:secret@example.test/path"); }
    @Test(expected=IllegalArgumentException.class) public void httpRejectsNonHttp() throws Exception { AdvancedProbes.httpUri("file:///etc/passwd"); }
    @Test(expected=IllegalArgumentException.class) public void httpRejectsLineInjection() throws Exception { AdvancedProbes.httpUri("https://example.test/\r\nX: y"); }
    @Test public void reportUrlNeverContainsPathQuerySecrets() throws Exception {
        assertEquals("https://example.test/…",AdvancedProbes.displayUri(AdvancedProbes.httpUri("https://example.test/secret?token=sensitive")));
    }
    @Test public void httpHeadDoesNotFollowRedirectOrLeakCookies() throws Exception {
        try(ServerSocket server=new ServerSocket(0,1,InetAddress.getLoopbackAddress())) {
            FutureTask<String> service=new FutureTask<>(()->{
                try(Socket peer=server.accept()) {
                    String req=readRequest(peer);
                    peer.getOutputStream().write(("HTTP/1.1 302 Found\r\nLocation: https://example.test/token?key=secret\r\nSet-Cookie: password=secret\r\nContent-Length: 99999999\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
                    return req;
                }
            }); Thread worker=new Thread(service);worker.setDaemon(true);worker.start();
            ToolResult result=new AdvancedProbes().http(request("http://127.0.0.1:"+server.getLocalPort()+"/",""),session());
            assertEquals(ToolResult.Status.PARTIAL,result.status);assertFalse(result.export().contains("secret"));assertFalse(result.export().contains("password"));
            String req=service.get(3,TimeUnit.SECONDS);assertTrue(req.startsWith("HEAD / HTTP/1.1"));assertFalse(req.contains("Cookie:"));assertFalse(req.contains("Authorization:"));
        }
    }
    @Test public void httpMalformedResponseFails() throws Exception {
        try(ServerSocket server=new ServerSocket(0,1,InetAddress.getLoopbackAddress())) {
            Thread worker=new Thread(()->{ try(Socket peer=server.accept()) { readRequest(peer);peer.getOutputStream().write("garbage\r\n\r\n".getBytes(StandardCharsets.US_ASCII)); }catch(Exception ignored){} });worker.setDaemon(true);worker.start();
            try {new AdvancedProbes().http(request("http://127.0.0.1:"+server.getLocalPort(),""),session());fail("malformed response");}catch(IOException expected){}
            worker.join(3000);
        }
    }
    @Test public void tlsRequiresEndpointIdentity() throws Exception {
        javax.net.ssl.SSLParameters parameters=new javax.net.ssl.SSLParameters();
        assertTrue(AdvancedProbes.configureEndpointVerification(parameters));
        assertEquals("HTTPS",parameters.getEndpointIdentificationAlgorithm());
    }
    @Test public void tlsPlaintextPeerFailsWithoutHttpDowngrade() throws Exception {
        try(ServerSocket server=new ServerSocket(0,1,InetAddress.getLoopbackAddress())) {
            FutureTask<Boolean> service=new FutureTask<>(()->{
                try(Socket peer=server.accept()) {
                    peer.setSoTimeout(3000);assertEquals(22,peer.getInputStream().read());
                    peer.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII));return true;
                }
            });Thread worker=new Thread(service);worker.setDaemon(true);worker.start();
            try {new AdvancedProbes().tls(request("127.0.0.1",Integer.toString(server.getLocalPort())),session());fail("must fail TLS");}
            catch(javax.net.ssl.SSLException expected){}
            assertTrue(service.get(4,TimeUnit.SECONDS));
        }
    }
    @Test public void dnsUdpTruncationRetriesTcpSameQuestion() throws Exception {
        InetAddress local=InetAddress.getLoopbackAddress();
        try(ServerSocket tcp=new ServerSocket(0,1,local);DatagramSocket udp=new DatagramSocket(tcp.getLocalPort(),local)) {
            FutureTask<Boolean> service=new FutureTask<>(()->{
                udp.setSoTimeout(3000);byte[] receive=new byte[512];DatagramPacket packet=new DatagramPacket(receive,receive.length);udp.receive(packet);
                byte[] truncated=Arrays.copyOf(receive,packet.getLength());truncated[2]=(byte)0x83;truncated[3]=(byte)0x80;
                udp.send(new DatagramPacket(truncated,truncated.length,packet.getSocketAddress()));
                try(Socket peer=tcp.accept()) {
                    peer.setSoTimeout(3000);DataInputStream in=new DataInputStream(peer.getInputStream());byte[] q=new byte[in.readUnsignedShort()];in.readFully(q);
                    assertArrayEquals(DnsWire.query(42,NAME,1),q);
                    byte[] answer=response(1,new byte[]{127,0,0,1});DataOutputStream out=new DataOutputStream(peer.getOutputStream());out.writeShort(answer.length);out.write(answer);out.flush();return true;
                }
            });Thread worker=new Thread(service);worker.setDaemon(true);worker.start();
            byte[] result=AdvancedProbes.dnsExchange(local,tcp.getLocalPort(),DnsWire.query(42,NAME,1),session(),2000);
            assertEquals("127.0.0.1",DnsWire.parse(result,42,NAME,1).answers.get(0).value);assertTrue(service.get(4,TimeUnit.SECONDS));
        }
    }
    @Test public void cancelledProbeNeverConnects() throws Exception {
        ProbeSession s=session();s.cancel();
        try {new AdvancedProbes().http(request("http://127.0.0.1:9",""),s);fail("cancelled");} catch(InterruptedException expected){}
    }
    private static ToolRequest request(String target,String option){return new ToolRequest(target,option,2000,1,false);}
    private static ProbeSession session(){return new ProbeSession(8000,null);}
    private static String readRequest(Socket socket)throws Exception{
        socket.setSoTimeout(3000);ByteArrayOutputStream out=new ByteArrayOutputStream();int matched=0;byte[] end={'\r','\n','\r','\n'};
        while(out.size()<8192){int b=socket.getInputStream().read();if(b<0)break;out.write(b);matched=b==end[matched]?matched+1:0;if(matched==4)break;}
        return new String(out.toByteArray(),StandardCharsets.US_ASCII);
    }
}
