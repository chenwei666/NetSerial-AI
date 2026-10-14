package com.chenwei666.netserial.network.toolbox;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.LinkAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.RouteInfo;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;

/** Routes explicit UI requests. Construction and opening the catalog perform no probes. */
public final class ToolboxService {
    private final Context context;
    public ToolboxService(Context context) { this.context = context.getApplicationContext(); }
    public ToolResult run(String id, ToolRequest request, ProbeSession session) throws Exception {
        session.check();
        switch (id) {
            case "PING": return new CoreProbes().ping(request, session);
            case "TRACE": return new CoreProbes().trace(request, session);
            case "MTU": return new CoreProbes().mtu(request, session);
            case "DNS": return new AdvancedProbes(defaultDns()).dns(request, session);
            case "HTTP": return new AdvancedProbes().http(request, session);
            case "TLS": return new AdvancedProbes().tls(request, session);
            case "WOL": return new AdvancedProbes().wol(request, session);
            case "TCP": return new ConnectionProbes().tcp(request, session);
            case "DISCOVER": return new ConnectionProbes().discover(request, session);
            case "INFO": return networkInfo(request);
            case "ADDRESS": return addresses(request, session);
            case "CIDR": return new OfflineTools().cidr(request);
            case "MAC": return new OfflineTools().mac(request);
            case "PORTS": return new OfflineTools().ports(request);
            case "EXTRACT": return new OfflineTools().extract(request);
            case "HEALTH": return health(request, session);
            default: throw new IllegalArgumentException("Unknown tool");
        }
    }
    private LinkProperties activeLink() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null || Build.VERSION.SDK_INT < 23) return null;
        Network network = manager.getActiveNetwork();
        return network == null ? null : manager.getLinkProperties(network);
    }
    private ToolResult addresses(ToolRequest request, ProbeSession session) throws Exception {
        List<ToolResult.Row> rows = new ArrayList<>();
        for (java.net.InetAddress address : ToolboxResolver.resolve(request.target, session, request.timeoutMs)) {
            String family = address instanceof java.net.Inet4Address ? "IPv4" : "IPv6";
            List<String> flags = new ArrayList<>();
            if (address.isLoopbackAddress()) flags.add(request.text("回环", "Loopback"));
            if (address.isLinkLocalAddress()) flags.add(request.text("链路本地", "Link-local"));
            if (address.isSiteLocalAddress()) flags.add(request.text("站点/私有地址", "Site-local/private"));
            if (address.isMulticastAddress()) flags.add(request.text("组播", "Multicast"));
            if (address.isAnyLocalAddress()) flags.add(request.text("未指定地址", "Unspecified"));
            byte[] bytes = address.getAddress();
            if (bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc) flags.add("ULA");
            rows.add(new ToolResult.Row(address.getHostAddress(), family + " · " + String.join(", ", flags)));
        }
        return new ToolResult(ToolResult.Status.OK, request.text("地址摘要", "Address summary"),
                request.text("地址分类不是连通性证明，也不代表地址安全。", "Address classification is not proof of connectivity or safety."), rows, "");
    }
    private String defaultDns() {
        LinkProperties link = activeLink();
        return link == null || link.getDnsServers().isEmpty() ? null : link.getDnsServers().get(0).getHostAddress();
    }
    private ToolResult networkInfo(ToolRequest request) {
        List<ToolResult.Row> rows = new ArrayList<>();
        if (Build.VERSION.SDK_INT < 23) return ToolResult.message(ToolResult.Status.UNSUPPORTED,
                request.text("当前网络", "Current network"), request.text("此页的默认网络信息需要 Android 6.0+。", "Default-network details require Android 6.0+."));
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network current = manager == null ? null : manager.getActiveNetwork();
        LinkProperties link = current == null ? null : manager.getLinkProperties(current);
        if (link == null) return ToolResult.message(ToolResult.Status.FAILED, request.text("当前网络", "Current network"),
                request.text("当前没有可读取的默认网络。检查 Wi-Fi、以太网或移动网络。", "No default network is available. Check Wi-Fi, Ethernet or mobile data."));
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(current);
        if (capabilities != null) {
            List<String> types = new ArrayList<>();
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) types.add("Wi-Fi");
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) types.add("Ethernet");
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) types.add("Cellular");
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) types.add("VPN");
            rows.add(new ToolResult.Row(request.text("传输类型", "Transport"), String.join(" / ", types)));
            rows.add(new ToolResult.Row(request.text("系统互联网验证", "System Internet validation"), capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    ? request.text("已验证（不代表所有目标可达）", "Validated (not all targets)") : request.text("未验证", "Not validated")));
        }
        rows.add(new ToolResult.Row(request.text("接口", "Interface"), String.valueOf(link.getInterfaceName())));
        if (Build.VERSION.SDK_INT >= 29) rows.add(new ToolResult.Row(request.text("接口 MTU（不是路径 MTU）", "Interface MTU (not path MTU)"), String.valueOf(link.getMtu())));
        for (LinkAddress address : link.getLinkAddresses()) rows.add(new ToolResult.Row("IP", address.toString()));
        for (java.net.InetAddress dns : link.getDnsServers()) rows.add(new ToolResult.Row("DNS", dns.getHostAddress()));
        for (RouteInfo route : link.getRoutes()) rows.add(new ToolResult.Row(request.text("路由", "Route"), route.toString()));
        return new ToolResult(ToolResult.Status.OK, request.text("当前网络", "Current network"),
                request.text("来自 Android 公开接口；不读取定位、SSID 或设备 MAC。信息只代表当前默认网络。", "From public Android APIs; no location, SSID or device MAC access. Describes only the default network."), rows, "");
    }
    private ToolResult health(ToolRequest request, ProbeSession session) throws Exception {
        int port = new com.chenwei666.netserial.network.NetworkTargetValidator().validatePort(request.option.isEmpty() ? "443" : request.option);
        boolean literal = isLiteralAddress(new com.chenwei666.netserial.network.NetworkTargetValidator().validate(request.target));
        List<ToolResult.Row> rows = new ArrayList<>();
        int failures = 0;
        ToolRequest dns = new ToolRequest(request.target, "A", request.timeoutMs, 3, request.chinese);
        ToolRequest tcp = new ToolRequest(request.target, String.valueOf(port), request.timeoutMs, 3, request.chinese);
        for (String step : new String[] {"DNS", "PING", "TCP"}) {
            session.check();
            ToolResult result;
            try {
                result = "DNS".equals(step) && literal ? ToolResult.message(ToolResult.Status.OK, "DNS",
                        request.text("已跳过：目标是 IP 地址，无需 DNS 查询。", "Skipped: the target is a literal IP address; no DNS query is needed."))
                        : run(step, "DNS".equals(step) ? dns : tcp, session);
            }
            catch (InterruptedException cancelled) { throw cancelled; }
            catch (Exception failed) { result = ToolResult.message(ToolResult.Status.FAILED, step,
                    request.text("本项失败，请打开对应工具检查输入、DNS 和连通性。", "Check this tool's input, DNS and connectivity.")); }
            if (result.status != ToolResult.Status.OK) failures++;
            rows.add(new ToolResult.Row(step + " · " + result.status, result.summary));
            for (ToolResult.Row row : result.rows) rows.add(new ToolResult.Row(step + " / " + row.label, row.value));
            session.publish(new ToolResult(ToolResult.Status.PARTIAL, request.text("基础体检", "Basic health check"),
                    request.text("已完成：", "Completed: ") + step, rows, ""));
        }
        return new ToolResult(failures == 0 ? ToolResult.Status.OK : ToolResult.Status.PARTIAL,
                request.text("基础体检", "Basic health check"), failures == 0
                ? literal ? request.text("IP 目标无需 DNS 查询；本次 Ping 和指定 TCP 端口检查通过，不代表全网或业务正常。", "The IP target needs no DNS query. Ping and the selected TCP port passed this run; this does not prove whole-network or service health.")
                : request.text("本次 DNS、Ping 和指定 TCP 端口检查通过；不代表全网或业务正常。", "DNS, Ping and the selected TCP port passed this run; this does not prove whole-network or service health.")
                : request.text("部分检查未通过。先看 DNS，再检查目标是否禁用 ICMP，最后核对端口与防火墙；不要据此直接修改交换机配置。", "Some checks did not pass. Review DNS, ICMP filtering, then ports/firewall. Do not change switch configuration based solely on this test."), rows, "");
    }

    /** Pure lexical classification: never asks a resolver to determine whether a target is an IP. */
    static boolean isLiteralAddress(String target) {
        if (target.matches("[0-9]+(?:\\.[0-9]+){3}")) {
            for (String octet : target.split("\\.")) {
                if (octet.length() > 3 || Integer.parseInt(octet) > 255) throw new IllegalArgumentException("Invalid IPv4 address");
            }
            return true;
        }
        if (!target.contains(":")) return false;
        if (!target.matches("[0-9a-fA-F:]+")) throw new IllegalArgumentException("Invalid IPv6 address");
        String[] sides = target.split("::", -1);
        if (sides.length > 2) throw new IllegalArgumentException("Invalid IPv6 compression");
        int groups = 0;
        for (String side : sides) {
            if (side.isEmpty()) continue;
            for (String group : side.split(":", -1)) {
                if (!group.matches("[0-9a-fA-F]{1,4}")) throw new IllegalArgumentException("Invalid IPv6 group");
                groups++;
            }
        }
        if (sides.length == 2 ? groups >= 8 : groups != 8) throw new IllegalArgumentException("Invalid IPv6 length");
        return true;
    }
}
