package com.chenwei666.netserial.guided.network;

import com.chenwei666.netserial.guided.DiagnosticFinding;
import com.chenwei666.netserial.guided.DiagnosticFinding.Status;
import com.chenwei666.netserial.guided.GuideText;
import com.chenwei666.netserial.guided.GuidedScenario;
import com.chenwei666.netserial.network.NetworkTargetValidator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** Small, phone-side checks against one explicit target; never sends switch commands. */
public final class NetworkDiagnosisRunner {
    public interface ProgressListener {
        void onProgress(int completed, int total, GuideText message);
    }

    /** Each operation must respect its timeout. Implementations must release resources on cancel. */
    public interface ProbeAdapter {
        InetAddress[] resolve(String host, int timeoutMillis) throws IOException, InterruptedException;
        boolean reachable(InetAddress address, int timeoutMillis) throws IOException, InterruptedException;
        boolean connect(InetAddress address, int port, int timeoutMillis) throws IOException, InterruptedException;
        void cancel();
    }

    private static final int SAMPLES = 3;
    private static final int STEPS = SAMPLES + 2;
    private static final int DNS_TIMEOUT_MS = 3000;
    private static final int PING_TIMEOUT_MS = 1500;
    private static final int TCP_TIMEOUT_MS = 2000;
    private static final int RUN_TIMEOUT_MS = 12000;
    private final ProbeAdapter probes;
    private final NetworkTargetValidator validator = new NetworkTargetValidator();
    private final Object lifecycle = new Object();
    private volatile boolean cancelled;
    private Thread worker;

    public NetworkDiagnosisRunner() { this(new SystemNetworkProbeAdapter()); }

    public NetworkDiagnosisRunner(ProbeAdapter probes) { this.probes = Objects.requireNonNull(probes); }

    public List<DiagnosticFinding> run(GuidedScenario scenario, String target, String port,
                                       ProgressListener listener) throws InterruptedException {
        if (scenario == null || !scenario.isNetwork()) throw new IllegalArgumentException("Network scenario required");
        if (target == null) throw new IllegalArgumentException("Target required");
        String host = validator.validate(target);
        int selectedPort = validator.validatePort(port);
        InetAddress numeric = numericAddress(host);
        if (numeric != null) requireUnicast(numeric);
        synchronized (lifecycle) {
            if (worker != null) throw new IllegalStateException("Diagnosis already running");
            // Cancellation may arrive while this work is still queued, before run() starts.
            // A new diagnosis after cancellation must use a new runner instance.
            checkCancelled();
            worker = Thread.currentThread();
        }
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(RUN_TIMEOUT_MS);
        try {
            return inspect(scenario, host, selectedPort, numeric, listener, deadline);
        } finally {
            synchronized (lifecycle) {
                probes.cancel();
                worker = null;
            }
        }
    }

    /** Permanently cancels this runner, including work that has not started yet. */
    public void cancel() {
        synchronized (lifecycle) {
            cancelled = true;
            if (worker != null) worker.interrupt();
            probes.cancel();
        }
    }

    private List<DiagnosticFinding> inspect(GuidedScenario scenario, String host, int port,
                                            InetAddress numeric, ProgressListener listener,
                                            long deadline) throws InterruptedException {
        List<DiagnosticFinding> findings = new ArrayList<>();
        progress(listener, 0, new GuideText("正在检查目标地址", "Checking the target address"));
        InetAddress address = numeric;
        if (numeric == null) {
            try {
                InetAddress[] addresses = probes.resolve(host, timeout(DNS_TIMEOUT_MS, deadline));
                checkCancelled();
                if (addresses == null || addresses.length == 0) throw new UnknownHostException();
                address = addresses[0];
                requireUnicast(address);
                findings.add(finding(Status.PASS, "域名已解析", "Name resolved",
                        "手机成功查询到该名称的地址。后续只检测第一个地址，其他地址尚未验证。",
                        "The phone resolved this name. Only the first address is checked; other addresses remain untested.",
                        "如果只有个别网站打不开，可对照实际网址，或在专业工具中检查其他解析结果。",
                        "If a specific site still fails, verify its name or inspect other DNS answers in Professional tools.",
                        host + " → " + address.getHostAddress()));
            } catch (IOException error) {
                checkCancelled();
                findings.add(dnsFailure(host, error));
                progress(listener, STEPS, new GuideText("解析未完成，已停止后续检测", "Name resolution failed; later checks stopped"));
                return Collections.unmodifiableList(findings);
            }
        } else {
            boolean dnsScenario = scenario == GuidedScenario.DNS_FAILURE;
            findings.add(finding(dnsScenario ? Status.WARNING : Status.PASS,
                    "使用 IP 地址，未查询 DNS", "Using an IP address; DNS was not tested",
                    "IP 地址不需要域名解析，因此这次结果不能用来判断 DNS 是否正常。",
                    "An IP address requires no name lookup, so this run cannot assess DNS.",
                    "要检查域名问题，请输入打不开的域名，例如你实际使用的网站域名。",
                    "To check DNS, enter the actual hostname that does not open.", host));
        }
        progress(listener, 1, new GuideText("地址已确认，正在测试响应", "Address ready; checking replies"));
        findings.add(checkReachability(address, listener, deadline));
        checkCancelled();
        findings.add(checkTcp(scenario, address, port, deadline));
        progress(listener, STEPS, new GuideText("检测完成", "Checks complete"));
        findings.add(scopeFinding(scenario));
        return Collections.unmodifiableList(findings);
    }

    private DiagnosticFinding checkReachability(InetAddress address, ProgressListener listener,
                                                long deadline) throws InterruptedException {
        int replies = 0;
        int unavailable = 0;
        for (int sample = 0; sample < SAMPLES; sample++) {
            checkCancelled();
            try {
                if (probes.reachable(address, timeout(PING_TIMEOUT_MS, deadline))) replies++;
            } catch (IOException error) {
                unavailable++;
            }
            checkCancelled();
            progress(listener, sample + 2, new GuideText("正在检测响应 " + (sample + 1) + "/" + SAMPLES,
                    "Checking reply " + (sample + 1) + "/" + SAMPLES));
        }
        String evidence = address.getHostAddress() + "\nreplies=" + replies + "/" + SAMPLES
                + "\nunavailable=" + unavailable;
        if (replies == SAMPLES) return finding(Status.PASS, "目标回应了检测", "The target replied",
                "3 次轻量检测均得到回应，仅说明此时手机到该地址可响应。不能证明网站、登录或整个网络正常。",
                "All 3 brief checks received replies. This only shows that this address responds from the phone now, not that websites, login, or the whole network work.",
                "继续查看下方端口检测；频繁断线可在专业工具中持续观察。",
                "Read the port check below. For intermittent failures, observe longer in Professional tools.", evidence);
        if (unavailable == SAMPLES) return finding(Status.UNKNOWN, "手机无法完成 Ping", "Ping could not run",
                "当前 Android 设备的 Ping 工具不可用或不允许执行。没有足够信息判断目标是否在线。",
                "Ping is unavailable or not permitted on this Android device. There is not enough evidence to decide whether the target is online.",
                "参考端口检测，或换用专业工具中的其他检测方式。",
                "Use the port check or another check in Professional tools.", evidence);
        return finding(Status.WARNING, replies == 0 ? "没有收到 Ping 回应" : "部分检测未得到回应",
                replies == 0 ? "No Ping replies" : "Some checks received no reply",
                "目标或防火墙可能不回应 Ping，也可能存在路径或连接问题；这不能直接证明设备离线。3 次样本不足以确认长期丢包率。",
                "The target or firewall may ignore Ping, or a path/connection problem may exist. This does not prove that the device is offline. Three samples cannot establish long-term packet loss.",
                "结合端口结果检查；若故障反复，在专业工具里做更长时间的观察。",
                "Compare with the port result. For recurring failures, collect longer observations in Professional tools.", evidence);
    }

    private DiagnosticFinding checkTcp(GuidedScenario scenario, InetAddress address, int port,
                                       long deadline) throws InterruptedException {
        boolean connected = false;
        String detail = "no connection";
        try {
            connected = probes.connect(address, port, timeout(TCP_TIMEOUT_MS, deadline));
            detail = connected ? "connected" : "no connection";
        } catch (IOException error) {
            detail = error instanceof SocketTimeoutException ? "timeout" : "not connected";
        }
        checkCancelled();
        String evidence = address.getHostAddress() + ":" + port + "\nTCP=" + detail;
        if (connected) return finding(Status.PASS, "所选端口可以连接", "The selected port accepts connections",
                "手机已与该 TCP 端口建立连接，未发送应用数据。尚未验证账号、密码、TLS 证书或服务功能。",
                "The phone connected to this TCP port without sending application data. Credentials, TLS certificates, and service functionality were not tested.",
                scenario == GuidedScenario.SWITCH_LOGIN ? "确认协议与端口匹配，再到连接页面登录；认证失败需要另查账号和权限。" : "尝试实际访问服务；若仍失败，检查网址、证书、代理或服务端日志。",
                scenario == GuidedScenario.SWITCH_LOGIN ? "Match the protocol to the port and try the connection screen; authentication failures require separate account/permission checks." : "Try the actual service; if it fails, inspect the URL, certificate, proxy, or server logs.", evidence);
        return finding(Status.FAIL, "所选端口未连接成功", "The selected port did not connect",
                "本次 TCP 连接未建立。可能是服务未开启、端口填写错误、访问控制或路径问题，不能据此判断密码错误。",
                "This TCP connection was not established. Possible causes include a stopped service, wrong port, access controls, or the network path; this is not evidence of a wrong password.",
                "确认目标 IP、端口和手机所在网络，再检查设备服务与访问控制。只检测了填写的一个端口。",
                "Verify the address, port, and phone network, then check the device service and access controls. Only the entered port was tested.", evidence);
    }

    private DiagnosticFinding scopeFinding(GuidedScenario scenario) {
        if (scenario == GuidedScenario.SLOW_NETWORK) return finding(Status.UNKNOWN,
                "网速与长期稳定性仍需观察", "Speed and long-term stability need more evidence",
                "少量 Ping 和 TCP 检测无法测出带宽，也不能把程序执行耗时当作精确往返延迟。当前结果不足以定位网络慢的原因。",
                "A few Ping/TCP checks do not measure bandwidth, and program execution time is not precise round-trip latency. These results cannot identify the cause of slow networking.",
                "在专业工具中继续做路径、MTU 或持续观察，并记录故障时间和受影响的应用。",
                "Continue with route/MTU checks or longer observation in Professional tools; record failure times and affected apps.", "");
        return finding(Status.UNKNOWN, "检测范围：当前手机网络", "Scope: the phone's current network",
                "以上结果来自手机自身的当前网络连接，未通过 USB 串口中的交换机转发。它不能代替电脑、交换机或其他网络的检测。",
                "These checks used the phone's current network, not the switch attached over USB serial. They do not test a computer, the switch's own path, or other networks.",
                "若出问题的是电脑或交换机，请核对它们的网络环境；需要设备侧证据时，进入交换机向导预览只读命令。",
                "If a computer or switch is affected, check its network context. Use a switch guide to preview read-only commands for device-side evidence.", "");
    }

    private DiagnosticFinding dnsFailure(String host, IOException error) {
        boolean notFound = error instanceof UnknownHostException;
        return finding(notFound ? Status.FAIL : Status.UNKNOWN, "域名解析未成功", "Name resolution did not succeed",
                "手机没有及时得到该名称的可用地址。可能是名称填写错误、DNS 问题、网络不可达或系统解析繁忙，不能仅凭此结果确定故障根因。",
                "The phone did not obtain a usable address in time. The name, DNS, network connectivity, or a busy system resolver may be involved; this alone does not establish the root cause.",
                "先检查名称拼写和手机网络。如果知道此服务的 IP，可另行输入该 IP 比较结果；未自动检测其他目标。",
                "Verify the name and phone network. If you know this service's IP, enter it separately to compare results; no other destination was tested automatically.",
                host + "\nDNS=" + (error instanceof SocketTimeoutException ? "timeout" : notFound ? "not resolved" : "unavailable"));
    }

    private static DiagnosticFinding finding(Status status, String zhTitle, String enTitle,
                                              String zhExplanation, String enExplanation,
                                              String zhNext, String enNext, String evidence) {
        return new DiagnosticFinding(status, new GuideText(zhTitle, enTitle),
                new GuideText(zhExplanation, enExplanation), new GuideText(zhNext, enNext), evidence);
    }

    private void progress(ProgressListener listener, int completed, GuideText message)
            throws InterruptedException {
        checkCancelled();
        if (listener != null) listener.onProgress(completed, STEPS, message);
        checkCancelled();
    }

    private int timeout(int maximum, long deadline) throws InterruptedException, SocketTimeoutException {
        checkCancelled();
        long remaining = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
        if (remaining <= 0) throw new SocketTimeoutException("Diagnosis deadline reached");
        return (int) Math.min(maximum, remaining);
    }

    private void checkCancelled() throws InterruptedException {
        if (cancelled || Thread.currentThread().isInterrupted()) throw new InterruptedException("cancelled");
    }

    private static InetAddress numericAddress(String host) {
        try {
            if (host.contains(":")) return InetAddress.getByName(host); // IPv6 syntax cannot trigger DNS.
            if (!host.matches("[0-9.]+")) return null;
            String[] parts = host.split("\\.", -1);
            if (parts.length != 4) throw new IllegalArgumentException("Invalid IPv4 address");
            byte[] bytes = new byte[4];
            for (int index = 0; index < 4; index++) {
                if (parts[index].isEmpty() || parts[index].length() > 3) throw new IllegalArgumentException("Invalid IPv4 address");
                int value = Integer.parseInt(parts[index]);
                if (value > 255) throw new IllegalArgumentException("Invalid IPv4 address");
                bytes[index] = (byte) value;
            }
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException invalid) {
            throw new IllegalArgumentException("Invalid IP address", invalid);
        }
    }

    private static void requireUnicast(InetAddress address) {
        if (address == null || address.isAnyLocalAddress() || address.isMulticastAddress()
                || "255.255.255.255".equals(address.getHostAddress())) {
            throw new IllegalArgumentException("A single unicast destination is required");
        }
    }
}
