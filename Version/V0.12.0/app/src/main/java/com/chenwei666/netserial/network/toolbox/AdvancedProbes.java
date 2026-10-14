package com.chenwei666.netserial.network.toolbox;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/** Explicit single-target probes. No root, hidden public resolver, authentication or redirects. */
public final class AdvancedProbes {
    private final String defaultDnsServer;
    public AdvancedProbes() { this(""); }
    public AdvancedProbes(String defaultDnsServer) { this.defaultDnsServer = defaultDnsServer == null ? "" : defaultDnsServer; }

    public ToolResult dns(ToolRequest r, ProbeSession s) throws Exception {
        String[] option = (r.option.isEmpty() ? "A" : r.option).split("\\s+");
        if (option.length > 2 || (option.length == 2 && !option[1].startsWith("@")))
            throw new IllegalArgumentException(r.text("格式：A 或 MX @DNS服务器IP", "Use A or MX @DNS-server-IP"));
        int type = DnsWire.type(option[0]);
        String server = option.length == 2 ? option[1].substring(1) : defaultDnsServer;
        if (option.length == 2 && server.isEmpty()) throw new IllegalArgumentException(r.text("@后必须填写DNS服务器IP", "A DNS server IP is required after @"));
        String name = type == 12 ? reverseName(literal(r.target)) : DnsWire.normalizeName(r.target);
        long start = System.nanoTime();
        List<ToolResult.Row> rows = new ArrayList<>();
        if (server.isEmpty()) {
            if (type != 1 && type != 28) return ToolResult.message(ToolResult.Status.UNSUPPORTED, "DNS",
                    r.text("系统未提供DNS地址，请指定 @服务器IP。不会自动使用公共DNS。", "No system DNS address. Specify @server-IP; no public DNS fallback."));
            for (InetAddress ip : ToolboxResolver.resolve(name, s, r.timeoutMs))
                if ((type == 1 && ip instanceof Inet4Address) || (type == 28 && ip instanceof Inet6Address))
                    rows.add(new ToolResult.Row(DnsWire.typeName(type), ip.getHostAddress()));
            boolean found = !rows.isEmpty();
            rows.add(new ToolResult.Row(r.text("解析方式", "Resolver"), r.text("系统解析器（可能命中缓存）", "System resolver (may use cache)")));
            rows.add(new ToolResult.Row(r.text("耗时", "Elapsed"), elapsed(start)));
            return new ToolResult(found ? ToolResult.Status.OK : ToolResult.Status.PARTIAL, "DNS",
                    found ? r.text("已获得指定类型地址", "Requested address type returned") : r.text("没有指定类型的地址", "No address of the requested type"), rows, "");
        }
        InetAddress address = literal(server);
        int id = new SecureRandom().nextInt(65536);
        byte[] request = DnsWire.query(id, name, type);
        byte[] response = dnsExchange(address, 53, request, s, r.timeoutMs);
        DnsWire.Reply reply = DnsWire.parse(response, id, name, type);
        rows.add(new ToolResult.Row(r.text("DNS服务器", "DNS server"), address.getHostAddress()));
        rows.add(new ToolResult.Row(r.text("查询", "Question"), name + " " + DnsWire.typeName(type)));
        rows.add(new ToolResult.Row(r.text("耗时", "Elapsed"), elapsed(start)));
        rows.add(new ToolResult.Row(r.text("响应码", "Response code"), rcode(reply.rcode)));
        boolean requested = false;
        for (DnsWire.Answer answer : reply.answers) {
            if (answer.type.equals(DnsWire.typeName(type))) requested = true;
            rows.add(new ToolResult.Row(answer.name + " · " + answer.type + " · TTL " + answer.ttl + " s", answer.value));
        }
        return new ToolResult(reply.rcode != 0 ? ToolResult.Status.FAILED : requested ? ToolResult.Status.OK : ToolResult.Status.PARTIAL,
                "DNS", reply.rcode != 0 ? r.text("DNS服务器返回错误", "DNS server returned an error")
                : requested ? r.text("查询完成；未验证DNSSEC", "Query complete; DNSSEC not validated")
                : r.text("响应中无所请求的记录；可能无记录或只返回别名", "No requested record in the answer; it may be absent or alias-only"), rows, "");
    }

    static byte[] dnsExchange(InetAddress server, int port, byte[] request, ProbeSession s, int timeout) throws Exception {
        byte[] response;
        DatagramSocket socket = s.track(new DatagramSocket());
        try {
            socket.connect(server, port); socket.setSoTimeout(s.remainingMs(timeout));
            socket.send(new DatagramPacket(request, request.length));
            byte[] buffer = new byte[16385]; DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet); s.check();
            if (packet.getLength() > 16384) throw new IOException("DNS response too large");
            response = Arrays.copyOf(buffer, packet.getLength());
        } finally { socket.close(); s.release(socket); }
        if (response.length < 12 || response[0] != request[0] || response[1] != request[1]) throw new IOException("DNS transaction mismatch");
        // Validate the echoed question before allowing a TCP retry.
        String qname = questionName(request); int qtype = ((request[request.length - 4] & 255) << 8) | (request[request.length - 3] & 255);
        DnsWire.Reply reply = DnsWire.parse(response, ((request[0] & 255) << 8) | (request[1] & 255), qname, qtype);
        if (!reply.truncated) return response;
        Socket tcp = s.track(new Socket());
        try {
            tcp.connect(new InetSocketAddress(server, port), s.remainingMs(timeout));
            tcp.setSoTimeout(s.remainingMs(timeout));
            DataOutputStream out = new DataOutputStream(tcp.getOutputStream());
            out.writeShort(request.length); out.write(request); out.flush();
            DataInputStream in = new DataInputStream(tcp.getInputStream());
            int size = in.readUnsignedShort(); if (size < 12 || size > 16384) throw new IOException("DNS TCP frame size");
            response = new byte[size];
            for (int i = 0; i < size;) {
                tcp.setSoTimeout(s.remainingMs(timeout));
                int n = in.read(response, i, size - i); if (n < 0) throw new IOException("Truncated DNS TCP response"); i += n;
            }
            reply = DnsWire.parse(response, ((request[0] & 255) << 8) | (request[1] & 255), qname, qtype);
            if (reply.truncated) throw new IOException("DNS TCP response still truncated");
            return response;
        } finally { tcp.close(); s.release(tcp); }
    }
    private static String questionName(byte[] query) throws IOException {
        StringBuilder name = new StringBuilder(); int p = 12;
        while (p < query.length && query[p] != 0) {
            int n = query[p++] & 255; if (n > 63 || p + n > query.length) throw new IOException("Invalid query");
            if (name.length() > 0) name.append('.');
            name.append(new String(query, p, n, StandardCharsets.US_ASCII)); p += n;
        }
        return name.toString();
    }

    public ToolResult http(ToolRequest r, ProbeSession s) throws Exception {
        URI uri = httpUri(r.target);
        String host = host(uri); boolean secure = "https".equalsIgnoreCase(uri.getScheme());
        int port = uri.getPort() < 0 ? (secure ? 443 : 80) : uri.getPort();
        long start = System.nanoTime(); Socket socket = connect(host, port, secure, r, s);
        try {
            String path = uri.getRawPath(); if (path == null || path.isEmpty()) path = "/";
            if (uri.getRawQuery() != null) path += "?" + uri.getRawQuery();
            String authority = (host.contains(":") ? "[" + host + "]" : host) + ":" + port;
            String request = "HEAD " + path + " HTTP/1.1\r\nHost: " + authority
                    + "\r\nUser-Agent: NetSerial-Diagnostics/0.12\r\nAccept: */*\r\nConnection: close\r\n\r\n";
            socket.getOutputStream().write(request.getBytes(StandardCharsets.US_ASCII)); socket.getOutputStream().flush();
            String header = readHeaders(socket, s, r.timeoutMs);
            String[] lines = header.split("\r\n");
            if (!lines[0].matches("HTTP/1\\.[01] [1-5][0-9][0-9](?: .*)?")) throw new IOException(r.text("无效HTTP响应", "Invalid HTTP response"));
            int code = Integer.parseInt(lines[0].substring(9, 12));
            List<ToolResult.Row> rows = new ArrayList<>();
            rows.add(new ToolResult.Row(r.text("请求方式", "Method"), "HEAD"));
            rows.add(new ToolResult.Row(r.text("目标", "Target"), displayUri(uri)));
            rows.add(new ToolResult.Row(r.text("状态码", "Status"), String.valueOf(code)));
            rows.add(new ToolResult.Row(r.text("响应头耗时（含解析与连接）", "Time to headers (including DNS/connect)"), elapsed(start)));
            rows.add(new ToolResult.Row(r.text("传输", "Transport"), secure ? r.text("TLS信任链与域名校验通过", "TLS trust and hostname verified") : r.text("明文HTTP", "Cleartext HTTP")));
            for (int i = 1; i < lines.length; i++) {
                int colon = lines[i].indexOf(':'); if (colon < 1) throw new IOException("Malformed HTTP header");
                String key = lines[i].substring(0, colon).toLowerCase(Locale.ROOT), value = lines[i].substring(colon + 1).trim();
                if (key.equals("location")) {
                    String location;
                    try { location = displayUri(httpUri(uri.resolve(value).toString())); }
                    catch (Exception bad) { location = r.text("已隐藏不可安全显示的跳转地址", "Unsafe redirect address hidden"); }
                    rows.add(new ToolResult.Row(r.text("跳转目标（未访问）", "Redirect target (not visited)"), location));
                } else if (key.equals("content-type") || key.equals("content-length") || key.equals("server") || key.equals("date"))
                    rows.add(new ToolResult.Row(key, DnsWire.printable(value, 512)));
            }
            return new ToolResult(code >= 200 && code < 300 ? ToolResult.Status.OK : ToolResult.Status.PARTIAL,
                    "HTTP / HTTPS", r.text("收到响应；未下载正文、未跟随跳转、未发送账号或Cookie。405表示服务器不支持HEAD。", "Response received; no body download, redirect following, credentials or cookies. 405 means HEAD is unsupported."), rows, "");
        } finally { socket.close(); s.release(socket); }
    }

    public ToolResult tls(ToolRequest r, ProbeSession s) throws Exception {
        String host = r.target;
        if (host.startsWith("[") && host.endsWith("]")) host = host.substring(1, host.length() - 1);
        if (host.contains("://") || host.contains("/") || host.contains("@")) throw new IllegalArgumentException(r.text("请输入域名或IP，不包含URL", "Enter a hostname or IP, not a URL"));
        int port = r.option.isEmpty() ? 443 : Integer.parseInt(r.option);
        if (port < 1 || port > 65535) throw new IllegalArgumentException("1–65535");
        long start = System.nanoTime(); SSLSocket socket = (SSLSocket) connect(host, port, true, r, s);
        try {
            X509Certificate leaf = (X509Certificate) socket.getSession().getPeerCertificates()[0];
            leaf.checkValidity();
            List<ToolResult.Row> rows = new ArrayList<>();
            rows.add(new ToolResult.Row(r.text("目标", "Target"), host + ":" + port));
            rows.add(new ToolResult.Row(r.text("协商协议", "Negotiated protocol"), socket.getSession().getProtocol()));
            rows.add(new ToolResult.Row(r.text("密码套件", "Cipher suite"), socket.getSession().getCipherSuite()));
            rows.add(new ToolResult.Row(r.text("主题", "Subject"), DnsWire.printable(leaf.getSubjectX500Principal().getName(), 1024)));
            rows.add(new ToolResult.Row(r.text("签发者", "Issuer"), DnsWire.printable(leaf.getIssuerX500Principal().getName(), 1024)));
            rows.add(new ToolResult.Row(r.text("生效时间", "Valid from"), utc(leaf.getNotBefore())));
            rows.add(new ToolResult.Row(r.text("到期时间", "Expires"), utc(leaf.getNotAfter())));
            rows.add(new ToolResult.Row(r.text("剩余天数", "Remaining days"), Long.toString(Math.max(0, (leaf.getNotAfter().getTime() - System.currentTimeMillis()) / 86400000L))));
            rows.add(new ToolResult.Row(r.text("握手耗时（含解析与连接）", "Handshake time (including DNS/connect)"), elapsed(start)));
            return new ToolResult(ToolResult.Status.OK, "TLS", r.text("系统信任链、有效期与目标域名校验通过；未进行吊销状态或全部协议扫描。", "System trust, validity and hostname verified; revocation and exhaustive protocol scanning not performed."), rows, "");
        } finally { socket.close(); s.release(socket); }
    }

    public ToolResult wol(ToolRequest r, ProbeSession s) throws Exception {
        InetAddress target = literal(r.target);
        if (!(target instanceof Inet4Address) || target.isMulticastAddress() || target.isAnyLocalAddress())
            throw new IllegalArgumentException(r.text("请输入IPv4单播或广播地址", "Enter a unicast or broadcast IPv4 destination"));
        byte[] data = magicPacket(r.option);
        DatagramSocket socket = s.track(new DatagramSocket());
        try { socket.setBroadcast(true); s.check(); socket.send(new DatagramPacket(data, data.length, target, 9)); s.check(); }
        finally { socket.close(); s.release(socket); }
        List<ToolResult.Row> rows = new ArrayList<>();
        rows.add(new ToolResult.Row(r.text("目标", "Destination"), target.getHostAddress() + ":9 / UDP"));
        rows.add(new ToolResult.Row(r.text("报文", "Packet"), "1 × " + data.length + " bytes"));
        return new ToolResult(ToolResult.Status.OK, "Wake-on-LAN", r.text("唤醒报文已发送；不代表设备已开机。网卡、BIOS及网络需支持WOL。", "Magic packet sent; this does not prove the device woke up. NIC, BIOS and network must support WOL."), rows, "");
    }
    public static byte[] magicPacket(String mac) {
        if (mac == null || !mac.matches("(?i)(?:[0-9a-f]{2}[:-]){5}[0-9a-f]{2}|[0-9a-f]{12}|[0-9a-f]{4}(?:\\.[0-9a-f]{4}){2}"))
            throw new IllegalArgumentException("Invalid MAC");
        String hex = mac.replace(":", "").replace("-", "").replace(".", "");
        byte[] address = new byte[6]; for (int i = 0; i < 6; i++) address[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        if ((address[0] & 1) != 0 || hex.equals("000000000000")) throw new IllegalArgumentException("MAC must identify a unicast device");
        byte[] packet = new byte[102]; Arrays.fill(packet, 0, 6, (byte) 255);
        for (int i = 0; i < 16; i++) System.arraycopy(address, 0, packet, 6 + i * 6, 6);
        return packet;
    }
    static InetAddress literal(String value) throws Exception {
        String raw = value;
        if (raw.startsWith("[") && raw.endsWith("]")) raw = raw.substring(1, raw.length() - 1);
        if (raw.contains(":")) {
            if (!raw.matches("[0-9a-fA-F:]+(?:%[A-Za-z0-9_.-]{1,32})?") || raw.length() > 78) throw new IllegalArgumentException("Expected literal IP");
            InetAddress ip = InetAddress.getByName(raw); if (!(ip instanceof Inet6Address)) throw new IllegalArgumentException("Expected IPv6"); return ip;
        }
        String[] octets = raw.split("\\.", -1);
        if (octets.length != 4) throw new IllegalArgumentException("Expected literal IPv4");
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            if (!octets[i].matches("0|[1-9][0-9]{0,2}")) throw new IllegalArgumentException("Invalid IPv4 octet");
            int n = Integer.parseInt(octets[i]); if (n > 255) throw new IllegalArgumentException("Invalid IPv4 octet"); bytes[i] = (byte) n;
        }
        return InetAddress.getByAddress(bytes);
    }
    static String reverseName(InetAddress ip) {
        byte[] b = ip.getAddress(); StringBuilder result = new StringBuilder();
        if (b.length == 4) { for (int i = 3; i >= 0; i--) result.append(b[i] & 255).append('.'); return result + "in-addr.arpa"; }
        for (int i = 15; i >= 0; i--) result.append(Integer.toHexString(b[i] & 15)).append('.').append(Integer.toHexString((b[i] >> 4) & 15)).append('.');
        return result + "ip6.arpa";
    }
    static URI httpUri(String value) throws Exception {
        if (value.length() > 4096 || value.matches("(?s).*[\\x00-\\x20\\x7f].*")) throw new IllegalArgumentException("Invalid HTTP URL");
        URI uri = new URI(value);
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) || uri.getRawUserInfo() != null
                || uri.getHost() == null || uri.getPort() == 0 || uri.getPort() > 65535 || uri.getRawFragment() != null)
            throw new IllegalArgumentException("Use http(s) URL without credentials or fragment");
        if (!uri.toASCIIString().equals(value)) uri = new URI(uri.toASCIIString());
        return uri;
    }
    private static String host(URI uri) { String h = uri.getHost(); return h.startsWith("[") ? h.substring(1, h.length() - 1) : h; }
    static String displayUri(URI uri) {
        // Paths and queries may contain passwords or bearer links. Only the origin is reportable.
        String h = host(uri); return uri.getScheme() + "://" + (h.contains(":") ? "[" + h + "]" : h)
                + (uri.getPort() < 0 ? "" : ":" + uri.getPort()) + "/…";
    }
    private static Socket connect(String host, int port, boolean tls, ToolRequest r, ProbeSession s) throws Exception {
        InetAddress[] addresses = ToolboxResolver.resolve(host, s, r.timeoutMs);
        Socket plain = null;
        IOException lastFailure = null;
        for (int i = 0; i < Math.min(addresses.length, 8); i++) {
            Socket candidate = s.track(new Socket());
            try {
                candidate.connect(new InetSocketAddress(addresses[i], port), s.remainingMs(r.timeoutMs));
                plain = candidate; break;
            } catch (IOException failedAddress) {
                lastFailure = failedAddress; candidate.close(); s.release(candidate); s.check();
            } catch (Exception stopped) { candidate.close(); s.release(candidate); throw stopped; }
        }
        if (plain == null) throw lastFailure == null ? new IOException("No resolved address") : lastFailure;
        Socket active = plain;
        try {
            plain.setSoTimeout(s.remainingMs(r.timeoutMs));
            if (tls) {
                SSLSocket ssl = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(plain, host, port, true);
                active = ssl; s.track(ssl);
                SSLParameters parameters = ssl.getSSLParameters();
                boolean endpointCheckedInHandshake = configureEndpointVerification(parameters);
                ssl.setSSLParameters(parameters);
                ssl.setSoTimeout(s.remainingMs(r.timeoutMs)); ssl.startHandshake(); s.check();
                // API 21–23 do not expose SSLParameters endpoint identification. Their platform
                // HTTPS hostname verifier performs the same mandatory identity check after trust validation.
                if (!endpointCheckedInHandshake && !HttpsURLConnection.getDefaultHostnameVerifier().verify(host, ssl.getSession()))
                    throw new SSLHandshakeException("TLS hostname verification failed");
                s.release(plain);
            }
            return active;
        } catch (Exception failure) { try { active.close(); } finally { plain.close(); s.release(active); s.release(plain); } throw failure; }
    }
    static boolean configureEndpointVerification(SSLParameters parameters) throws Exception {
        try {
            SSLParameters.class.getMethod("setEndpointIdentificationAlgorithm", String.class).invoke(parameters, "HTTPS");
            return true;
        } catch (NoSuchMethodException api21to23) { return false; }
    }
    private static String readHeaders(Socket socket, ProbeSession s, int timeout) throws Exception {
        InputStream in = socket.getInputStream(); ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int matched = 0; byte[] delimiter = {'\r', '\n', '\r', '\n'};
        while (bytes.size() < 32768) {
            socket.setSoTimeout(s.remainingMs(timeout)); int b = in.read();
            if (b < 0) throw new IOException("Incomplete HTTP headers");
            bytes.write(b); matched = b == delimiter[matched] ? matched + 1 : (b == '\r' ? 1 : 0);
            if (matched == 4) return new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1);
        }
        throw new IOException("HTTP headers exceed 32 KiB");
    }
    private static String elapsed(long start) { return String.format(Locale.ROOT, "%.1f ms", (System.nanoTime() - start) / 1000000.0); }
    private static String utc(Date date) { SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.ROOT); f.setTimeZone(TimeZone.getTimeZone("UTC")); return f.format(date); }
    private static String rcode(int n) { switch (n) { case 0: return "NOERROR"; case 1: return "FORMERR"; case 2: return "SERVFAIL"; case 3: return "NXDOMAIN"; case 4: return "NOTIMP"; case 5: return "REFUSED"; default: return "RCODE " + n; } }
}
