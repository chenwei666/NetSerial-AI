package com.chenwei666.netserial.network.toolbox;

/** User-facing failure taxonomy without copying remote exceptions, URLs or credentials. */
public final class ToolFailures {
    private ToolFailures() { }
    public static ToolResult result(String title, ToolRequest request, Exception error) {
        ToolResult.Status status = ToolResult.Status.FAILED;
        String message;
        if (error instanceof ProbeSession.DeadlineExceeded) {
            message = request.text("本次检测达到总时限，已停止。减少次数或跳数后重试；未完成不代表目标不可达。", "Overall deadline reached; probing stopped. Reduce the count/hops and retry. Incomplete does not mean unreachable.");
        } else if (error instanceof InterruptedException) {
            status = ToolResult.Status.CANCELLED;
            message = request.text("检测已停止；未完成的项目没有结论。", "Probe stopped; incomplete checks have no conclusion.");
        } else if (error instanceof java.net.UnknownHostException) {
            message = request.text("DNS 解析失败。检查域名、当前网络和 DNS 服务器；可先使用目标 IP 检测。", "DNS resolution failed. Check hostname, network and DNS server, or test the target IP first.");
        } else if (error instanceof java.net.SocketTimeoutException) {
            message = request.text("等待网络响应超时。可能是丢包、过滤、服务未响应或 DNS 超时；不等于设备离线。", "Network response timed out. Possible packet loss, filtering, silent service or DNS timeout; not proof that the device is offline.");
        } else if (error instanceof javax.net.ssl.SSLException || error instanceof java.security.cert.CertificateException) {
            message = request.text("TLS 校验或握手失败。检查系统时间、证书有效期、域名与信任链及协议兼容性；没有跳过证书验证。", "TLS validation or handshake failed. Check time, expiry, hostname, trust chain and protocol compatibility. Certificate validation was not bypassed.");
        } else if (error instanceof java.net.ConnectException) {
            message = request.text("TCP 连接被拒绝。核对端口、服务监听状态和防火墙拒绝规则。", "TCP connection refused. Check port, service listener and firewall rejection rules.");
        } else if (error instanceof java.net.NoRouteToHostException) {
            message = request.text("没有到目标的可用路由。检查本机 IP、网关、VPN 和网络连接。", "No route to target. Check local IP, gateway, VPN and network connection.");
        } else if (error instanceof IllegalArgumentException || error instanceof java.net.URISyntaxException) {
            message = request.text("输入不符合当前工具要求。请按示例检查目标、端口、记录类型、掩码和数量限制；局域网发现仅限私有 /26–/32 网段。", "Input does not meet this tool's requirements. Check target, ports, record type, netmask and limits. LAN discovery only accepts private /26–/32 subnets.");
        } else if (error instanceof SecurityException) {
            status = ToolResult.Status.UNSUPPORTED;
            message = request.text("当前 Android 权限或系统策略不允许此操作；不会尝试 Root 或提权。", "Android permissions or system policy prevent this operation. No root or privilege escalation is attempted.");
        } else {
            message = request.text("检测未完成：网络中断、协议响应无效或系统能力不可用。检查参数和连接后重试；不会把失败当成成功。", "Probe incomplete: interrupted network, invalid protocol response or unavailable platform capability. Check input and connectivity; failure is not reported as success.");
        }
        return ToolResult.message(status, title, message);
    }
}
