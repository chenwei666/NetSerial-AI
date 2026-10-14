package com.chenwei666.netserial.network.toolbox;

import com.chenwei666.netserial.network.Ipv4Calculator;
import com.chenwei666.netserial.network.Ipv4Network;
import com.chenwei666.netserial.network.PortBatchParser;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Single-target connects and explicitly bounded private-subnet discovery. No service login. */
public final class ConnectionProbes {
    enum State { OPEN, REFUSED, TIMEOUT, UNREACHABLE, ERROR }
    static final class Observation {
        final State state; final double milliseconds;
        Observation(State state, double milliseconds) { this.state = state; this.milliseconds = milliseconds; }
    }
    public ToolResult tcp(ToolRequest request, ProbeSession session) throws Exception {
        List<Integer> ports = new PortBatchParser().parse(request.option.isEmpty() ? "443" : request.option);
        InetAddress address = ToolboxResolver.resolve(request.target, session, request.timeoutMs)[0];
        List<ToolResult.Row> rows = new ArrayList<>();
        rows.add(new ToolResult.Row(request.text("解析地址", "Resolved address"), address.getHostAddress()));
        int open = 0;
        for (int port : ports) {
            Observation result = connect(address, port, request.timeoutMs, session);
            if (result.state == State.OPEN) open++;
            rows.add(new ToolResult.Row("TCP " + port, state(result.state, request) + " · "
                    + String.format(Locale.ROOT, "%.2f ms", result.milliseconds)
                    + request.text("（连接尝试耗时）", " (connect attempt)")));
            session.publish(new ToolResult(ToolResult.Status.PARTIAL, "TCP", request.text("正在检查端口…", "Checking ports…"), rows, ""));
        }
        return new ToolResult(open == ports.size() ? ToolResult.Status.OK : open > 0 ? ToolResult.Status.PARTIAL : ToolResult.Status.FAILED,
                "TCP · " + request.target, request.text("开放端口：", "Open ports: ") + open + "/" + ports.size()
                + request.text("。连接成功不代表账号或应用服务正常。超时不等于端口关闭。", ". A connection does not prove authentication or application health. Timeout is not proof of a closed port."), rows, "");
    }
    static Observation connect(InetAddress address, int port, int timeout, ProbeSession session) throws InterruptedException {
        session.check();
        long started = System.nanoTime();
        Socket socket = new Socket();
        State state;
        try {
            session.track(socket);
            socket.connect(new InetSocketAddress(address, port), session.remainingMs(timeout));
            state = State.OPEN;
        } catch (SocketTimeoutException error) { state = State.TIMEOUT;
        } catch (ConnectException error) { state = State.REFUSED;
        } catch (NoRouteToHostException error) { state = State.UNREACHABLE;
        } catch (IOException error) { state = State.ERROR;
        } finally {
            session.release(socket);
            try { socket.close(); } catch (IOException ignored) { }
        }
        session.check();
        return new Observation(state, (System.nanoTime() - started) / 1_000_000.0);
    }
    private static String state(State state, ToolRequest request) {
        switch (state) {
            case OPEN: return request.text("开放", "Open");
            case REFUSED: return request.text("连接被拒绝", "Connection refused");
            case TIMEOUT: return request.text("超时 / 可能被过滤", "Timeout / possibly filtered");
            case UNREACHABLE: return request.text("无可用路由", "No route");
            default: return request.text("连接失败", "Connection failed");
        }
    }
    /** Validate before any network call; never allow arbitrary Internet ranges or >64 addresses. */
    static List<String> discoveryTargets(String cidr) {
        Ipv4Network network = new Ipv4Calculator().calculate(cidr);
        if (network.getTotalAddresses() > 64) throw new IllegalArgumentException("Discovery is limited to /26 through /32");
        long first = ipv4(network.getFirstUsable()), last = ipv4(network.getLastUsable());
        if (!privateAddress(first) || !privateAddress(last)) throw new IllegalArgumentException("Private subnet required");
        List<String> addresses = new ArrayList<>();
        for (long value = first; value <= last; value++) addresses.add(format(value));
        return addresses;
    }
    public ToolResult discover(ToolRequest request, ProbeSession session) throws Exception {
        List<String> addresses = discoveryTargets(request.target);
        List<Integer> ports = new PortBatchParser().parse(request.option.isEmpty() ? "80,443" : request.option);
        if (ports.size() > 4) throw new IllegalArgumentException("Discovery permits at most four TCP ports");
        List<ToolResult.Row> rows = new ArrayList<>();
        int checked = 0, seen = 0;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(40);
        boolean limited = false;
        for (String host : addresses) {
            session.check();
            if (System.nanoTime() >= deadline) { limited = true; break; }
            InetAddress address = InetAddress.getByAddress(bytes(ipv4(host)));
            List<String> evidence = new ArrayList<>();
            for (int port : ports) {
                if (System.nanoTime() >= deadline) { limited = true; break; }
                Observation result = connect(address, port, Math.min(350, request.timeoutMs), session);
                if (result.state == State.OPEN || result.state == State.REFUSED)
                    evidence.add(port + ": " + state(result.state, request));
            }
            if (limited) break;
            checked++;
            if (!evidence.isEmpty()) {
                seen++;
                rows.add(new ToolResult.Row(host, String.join(", ", evidence)));
            }
            session.publish(new ToolResult(ToolResult.Status.PARTIAL, request.text("局域网发现", "LAN discovery"),
                    request.text("已检查：", "Checked: ") + checked + "/" + addresses.size(), rows, ""));
        }
        rows.add(0, new ToolResult.Row(request.text("已检查 / 目标数", "Checked / targets"), checked + " / " + addresses.size()));
        rows.add(1, new ToolResult.Row(request.text("有 TCP 响应的地址", "Addresses with TCP responses"), String.valueOf(seen)));
        return new ToolResult(ToolResult.Status.PARTIAL, request.text("局域网发现", "LAN discovery"),
                (limited ? request.text("已达到40秒上限。", "40-second limit reached. ") : "")
                + request.text("仅探测所选端口；未响应不代表离线，拒绝可能来自中间防火墙。不是完整设备清单。", "Only selected ports were probed. Silence is not proof of offline status; refusals may come from a firewall. This is not a complete inventory."), rows, "");
    }
    private static long ipv4(String ip) {
        String[] parts = ip.split("\\.", -1);
        if (parts.length != 4) throw new IllegalArgumentException("IPv4 required");
        long value = 0;
        for (String part : parts) {
            if (!part.matches("0|[1-9][0-9]{0,2}")) throw new IllegalArgumentException("Invalid IPv4");
            int octet = Integer.parseInt(part);
            if (octet > 255) throw new IllegalArgumentException("Invalid IPv4");
            value = (value << 8) | octet;
        }
        return value;
    }
    private static boolean privateAddress(long ip) {
        return (ip >>> 24) == 10 || (ip >>> 20) == 0xac1 || (ip >>> 16) == 0xc0a8;
    }
    private static String format(long ip) { return ((ip >>> 24) & 255) + "." + ((ip >>> 16) & 255) + "." + ((ip >>> 8) & 255) + "." + (ip & 255); }
    private static byte[] bytes(long ip) { return new byte[] {(byte)(ip >>> 24), (byte)(ip >>> 16), (byte)(ip >>> 8), (byte)ip}; }
}
