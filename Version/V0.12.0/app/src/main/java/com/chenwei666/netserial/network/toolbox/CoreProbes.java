package com.chenwei666.netserial.network.toolbox;

import com.chenwei666.netserial.network.NetworkTargetValidator;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** ICMP capabilities exposed by Android's unprivileged ping. No root or raw socket fallback. */
public final class CoreProbes {
    interface Runner { Execution run(List<String> command, int timeoutMs, ProbeSession session) throws Exception; }
    interface Resolver { InetAddress resolve(String host, int timeoutMs, ProbeSession session) throws Exception; }
    static final class Execution {
        final int exit; final String raw; final boolean timedOut;
        Execution(int exit, String raw, boolean timedOut) { this.exit = exit; this.raw = raw; this.timedOut = timedOut; }
    }
    private static final int MAX_RAW = 48000;
    private final Runner runner;
    private final Resolver resolver;

    public CoreProbes() {
        this(new BoundedPingProcess(), (host, timeout, session) -> ToolboxResolver.resolve(host, session, timeout)[0]);
    }
    CoreProbes(Runner runner, Resolver resolver) { this.runner = runner; this.resolver = resolver; }

    public ToolResult ping(ToolRequest request, ProbeSession session) throws Exception {
        InetAddress address = resolve(request, session);
        List<Double> samples = new ArrayList<>();
        List<ToolResult.Row> packets = new ArrayList<>();
        StringBuilder raw = new StringBuilder();
        boolean upperBound = false;
        for (int index = 1; index <= request.count; index++) {
            Execution execution;
            try { execution = probe(address, request, session, null, null); }
            catch (IOException unavailable) { return unsupported(request, "Ping", raw.toString()); }
            appendRaw(raw, index, execution.raw);
            PingOutput packet = PingOutput.parse(execution.raw);
            if (packet.unsupported) return unsupported(request, "Ping", raw.toString());
            samples.add(packet.reply ? packet.rtt : Double.NaN);
            upperBound |= packet.reply && packet.lessThan;
            String outcome = packet.reply ? packet.rttText() : execution.timedOut || execution.exit == 1
                    ? request.text("超时 / 未收到回包", "Timeout / no reply")
                    : request.text("没有有效回包；查看原始输出", "No valid reply; see raw output");
            packets.add(new ToolResult.Row(request.text("第 ", "Packet ") + index, outcome));
            session.publish(pingResult(request, address, samples, packets, raw.toString(), upperBound, index < request.count));
        }
        return pingResult(request, address, samples, packets, raw.toString(), upperBound, false);
    }

    private ToolResult pingResult(ToolRequest request, InetAddress address, List<Double> samples,
                                  List<ToolResult.Row> packets, String raw, boolean upperBound, boolean running) {
        int received = 0;
        double min = Double.POSITIVE_INFINITY, max = 0, total = 0;
        for (double sample : samples) if (!Double.isNaN(sample)) {
            received++; min = Math.min(min, sample); max = Math.max(max, sample); total += sample;
        }
        List<ToolResult.Row> rows = new ArrayList<>();
        rows.add(new ToolResult.Row(request.text("目标地址", "Target address"), address.getHostAddress()));
        rows.add(new ToolResult.Row(request.text("发送 / 接收", "Sent / received"), samples.size() + " / " + received));
        rows.add(new ToolResult.Row(request.text("丢包率", "Packet loss"), String.format(Locale.ROOT, "%.1f%%", 100.0 * (samples.size() - received) / samples.size())));
        if (received > 0) rows.add(new ToolResult.Row(request.text("最小 / 平均 / 最大", "Min / average / max"),
                String.format(Locale.ROOT, "%.3f / %.3f / %.3f ms", min, total / received, max)));
        if (upperBound) rows.add(new ToolResult.Row(request.text("精度说明", "Precision"),
                request.text("带 < 的回包使用上限绘图及汇总，不代表精确延迟", "Replies marked < use upper bounds for the chart and aggregates, not exact RTT")));
        rows.addAll(packets);
        ToolResult.Status status = received == 0 ? ToolResult.Status.FAILED : received == samples.size() ? ToolResult.Status.OK : ToolResult.Status.PARTIAL;
        String summary = running ? request.text("检测中：", "Probing: ") + samples.size() + " / " + request.count
                : received == 0 ? request.text("未收到 ICMP 回包。目标或中间网络可能过滤 Ping，不能单凭此结果认定设备离线。", "No ICMP replies. The host or network may filter Ping; this alone does not establish that the device is offline.")
                : request.text("延迟来自实际 ICMP 回包，不包含 DNS 和进程启动耗时。", "Latency comes from ICMP replies, excluding DNS and process startup time.");
        return new ToolResult(status, "Ping", summary, rows, raw, samples);
    }

    public ToolResult trace(ToolRequest request, ProbeSession session) throws Exception {
        InetAddress address = resolve(request, session);
        String title = request.text("路由追踪", "Traceroute");
        List<ToolResult.Row> hops = new ArrayList<>();
        StringBuilder raw = new StringBuilder();
        for (int ttl = 1; ttl <= request.count; ttl++) {
            Execution execution;
            try { execution = probe(address, request, session, ttl, null); }
            catch (IOException unavailable) { return unsupported(request, title, raw.toString()); }
            appendRaw(raw, ttl, execution.raw);
            PingOutput packet = PingOutput.parse(execution.raw);
            if (packet.unsupported) return unsupported(request, title, raw.toString());
            String evidence = packet.reply ? address.getHostAddress() + " · " + packet.rttText()
                    : packet.timeExceeded && !packet.router.isEmpty() ? packet.router + " · " + request.text("系统未提供此跳 RTT", "RTT not provided by OS")
                    : "* · " + request.text("未收到可识别的逐跳响应", "No identifiable hop response");
            hops.add(new ToolResult.Row(request.text("第 ", "Hop ") + ttl, evidence));
            ToolResult progress = new ToolResult(packet.reply ? ToolResult.Status.OK : ToolResult.Status.PARTIAL, title,
                    packet.reply ? request.text("已到达目标。使用免 Root 的 ICMP TTL 探测；* 表示未知跳，不代表断网。", "Destination reached using root-free ICMP TTL probes. * means an unknown hop, not a broken link.")
                            : request.text("免 Root TTL 探测中；仅显示系统实际返回的地址，不估算逐跳延迟。", "Root-free TTL probe in progress; only returned addresses are shown, with no estimated hop RTT."), hops, raw.toString());
            session.publish(progress);
            if (packet.reply) return progress;
        }
        return new ToolResult(ToolResult.Status.PARTIAL, title,
                request.text(request.count + " 跳内未确认到达目标。Android 或网络可能不返回 TTL 超时报文；未知跳不能用来定位故障。可在已连接交换机上确认后执行设备端追踪。", "Destination not confirmed within " + request.count + " hops. Android or the network may suppress TTL-expired responses; unknown hops cannot locate a fault. A device-side trace on your connected switch is an alternative after confirmation."), hops, raw.toString());
    }

    public ToolResult mtu(ToolRequest request, ProbeSession session) throws Exception {
        InetAddress address = resolve(request, session);
        String title = request.text("路径 MTU", "Path MTU");
        if (!(address instanceof Inet4Address)) return ToolResult.message(ToolResult.Status.UNSUPPORTED, title,
                request.text("此版本只支持 IPv4 DF 探测。IPv6 的分片规则不同，不套用 IPv4 结果。", "This probe supports IPv4 DF only. IPv6 fragmentation differs; IPv4 MTU results are not applicable."));
        StringBuilder raw = new StringBuilder();
        PingOutput baseline = mtuPacket(address, request, session, 56, raw);
        if (baseline.unsupported) return unsupported(request, title, raw.toString());
        if (!baseline.reply) return new ToolResult(ToolResult.Status.FAILED, title,
                request.text("小包 DF 探测没有回包，无法判断 MTU。请先检查连通性和 ICMP 策略。", "The small DF probe received no reply; MTU cannot be determined. Check connectivity and ICMP policy first."), new ArrayList<>(), raw.toString());
        int low = 56, high = 1472;
        PingOutput large = mtuPacket(address, request, session, high, raw);
        if (large.unsupported) return unsupported(request, title, raw.toString());
        if (large.reply) return mtuResult(request, ToolResult.Status.OK, 1500,
                request.text("已验证 1500 字节 IPv4 包可不分片到达，路径可能支持更大 MTU。当前检测上限为 1500，并非精确上限。", "A 1500-byte IPv4 packet reached the target without fragmentation. The path may support a larger MTU; 1500 is the probe ceiling, not an exact upper limit."), raw.toString());
        if (!large.tooLarge) return uncertainMtu(request, low, raw.toString());
        // Only explicit fragmentation errors bound the search. Ordinary loss never reduces the ceiling.
        while (high - low > 1) {
            int candidate = (high + low) / 2;
            PingOutput packet = mtuPacket(address, request, session, candidate, raw);
            if (packet.unsupported) return unsupported(request, title, raw.toString());
            if (packet.reply) low = candidate;
            else if (packet.tooLarge) high = candidate;
            else return uncertainMtu(request, low, raw.toString());
            session.publish(mtuResult(request, ToolResult.Status.PARTIAL, low + 28,
                    request.text("DF 探测中：已验证下限", "DF probe in progress: verified lower bound"), raw.toString()));
        }
        List<ToolResult.Row> rows = new ArrayList<>();
        rows.add(new ToolResult.Row(request.text("已验证 IPv4 MTU", "Verified IPv4 MTU"), (low + 28) + " bytes"));
        rows.add(new ToolResult.Row(request.text("验证条件", "Evidence"), request.text("该尺寸回包成功，增加 1 字节收到明确分片错误", "This size replied; one additional byte returned an explicit fragmentation error")));
        return new ToolResult(ToolResult.Status.OK, title,
                request.text("结果仅适用于当前 IPv4 路径与检测时刻，包含 20 字节 IP 头和 8 字节 ICMP 头。", "Result applies to the current IPv4 path at test time, including 20-byte IP and 8-byte ICMP headers."), rows, raw.toString());
    }

    private PingOutput mtuPacket(InetAddress address, ToolRequest request, ProbeSession session, int size, StringBuilder raw) throws Exception {
        try {
            Execution execution = probe(address, request, session, null, size);
            appendRaw(raw, size, execution.raw);
            return PingOutput.parse(execution.raw);
        } catch (IOException unavailable) { return PingOutput.parse("ping: not supported"); }
    }
    private ToolResult uncertainMtu(ToolRequest request, int payload, String raw) {
        return mtuResult(request, ToolResult.Status.PARTIAL, payload + 28,
                request.text("仅确认下限。较大包超时不等于 MTU 太小，不能据此给出精确 MTU。", "Only a lower bound is verified. A larger packet timeout does not prove a smaller MTU; an exact MTU cannot be reported."), raw);
    }
    private ToolResult mtuResult(ToolRequest request, ToolResult.Status status, int lower, String summary, String raw) {
        return new ToolResult(status, request.text("路径 MTU", "Path MTU"), summary,
                Arrays.asList(new ToolResult.Row(request.text("已验证 IPv4 MTU 下限", "Verified IPv4 MTU lower bound"), ">= " + lower + " bytes")), raw);
    }
    private InetAddress resolve(ToolRequest request, ProbeSession session) throws Exception {
        session.check();
        return resolver.resolve(new NetworkTargetValidator().validate(request.target), session.remainingMs(request.timeoutMs), session);
    }
    private Execution probe(InetAddress address, ToolRequest request, ProbeSession session, Integer ttl, Integer payload) throws Exception {
        session.check();
        int seconds = Math.max(1, (request.timeoutMs + 999) / 1000);
        List<String> command = new ArrayList<>();
        command.add(address instanceof Inet4Address ? "/system/bin/ping" : "/system/bin/ping6");
        command.addAll(Arrays.asList("-n", "-c", "1", "-W", String.valueOf(seconds)));
        if (ttl != null) command.addAll(Arrays.asList("-t", String.valueOf(ttl)));
        if (payload != null) command.addAll(Arrays.asList("-M", "do", "-s", String.valueOf(payload)));
        command.add(address.getHostAddress());
        return runner.run(command, session.remainingMs(request.timeoutMs + 500), session);
    }
    private ToolResult unsupported(ToolRequest request, String title, String raw) {
        return new ToolResult(ToolResult.Status.UNSUPPORTED, title,
                request.text("Android 未提供所需的免 Root Ping 能力或权限。不会尝试提权；可用 TCP 检测连通性，或在已连接交换机上确认后执行诊断命令。", "Android does not provide the required unprivileged Ping capability or permission. No privilege escalation is attempted. Use TCP connectivity checks or confirm diagnostic commands on your connected switch."), new ArrayList<>(), raw);
    }
    private static void appendRaw(StringBuilder raw, int index, String text) {
        if (raw.length() >= MAX_RAW) return;
        String addition = "[" + index + "]\n" + text + "\n";
        raw.append(addition, 0, Math.min(addition.length(), MAX_RAW - raw.length()));
    }
}
