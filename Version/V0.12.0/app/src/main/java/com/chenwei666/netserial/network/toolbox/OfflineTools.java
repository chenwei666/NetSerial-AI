package com.chenwei666.netserial.network.toolbox;

import com.chenwei666.netserial.network.*;
import java.util.ArrayList;
import java.util.List;

public final class OfflineTools {
    public ToolResult cidr(ToolRequest request) {
        List<ToolResult.Row> rows = new ArrayList<>();
        String input = request.target;
        if (input.contains(":")) {
            Ipv6Network network = new Ipv6Calculator().calculate(input);
            rows.add(new ToolResult.Row(request.text("网络前缀", "Network prefix"), network.getNetworkPrefix()));
            rows.add(new ToolResult.Row(request.text("总地址数", "Total addresses"), network.getTotalAddresses()));
        } else {
            if (!request.option.isEmpty()) input = input.split("/")[0] + "/" + prefixFromMask(request.option);
            Ipv4Network network = new Ipv4Calculator().calculate(input);
            rows.add(new ToolResult.Row(request.text("网络地址", "Network"), network.getNetwork()));
            rows.add(new ToolResult.Row(request.text("子网掩码", "Netmask"), network.getNetmask()));
            rows.add(new ToolResult.Row(request.text("末地址（/31、/32 不作广播）", "Last address (no broadcast for /31, /32)"), network.getBroadcast()));
            rows.add(new ToolResult.Row(request.text("首可用地址", "First usable"), network.getFirstUsable()));
            rows.add(new ToolResult.Row(request.text("末可用地址", "Last usable"), network.getLastUsable()));
            rows.add(new ToolResult.Row(request.text("总地址数", "Total addresses"), String.valueOf(network.getTotalAddresses())));
            long usable = network.getTotalAddresses() > 2 ? network.getTotalAddresses() - 2 : network.getTotalAddresses();
            rows.add(new ToolResult.Row(request.text("可用地址数", "Usable addresses"), String.valueOf(usable)));
        }
        return new ToolResult(ToolResult.Status.OK, "CIDR", request.text("离线计算，不发送网络请求。", "Offline calculation; no network requests."), rows, "");
    }
    static int prefixFromMask(String mask) {
        String value = mask.trim();
        if (value.matches("/?[0-9]{1,2}")) {
            int prefix = Integer.parseInt(value.replace("/", ""));
            if (prefix <= 32) return prefix;
            throw new IllegalArgumentException("Invalid prefix");
        }
        String[] parts = value.split("\\.", -1);
        if (parts.length != 4) throw new IllegalArgumentException("Invalid mask");
        long bits = 0;
        for (String part : parts) {
            if (!part.matches("0|[1-9][0-9]{0,2}")) throw new IllegalArgumentException("Invalid mask");
            int octet = Integer.parseInt(part);
            if (octet > 255) throw new IllegalArgumentException("Invalid mask");
            bits = (bits << 8) | octet;
        }
        long inverted = (~bits) & 0xffffffffL;
        if ((inverted & (inverted + 1)) != 0) throw new IllegalArgumentException("Noncontiguous mask");
        return Long.bitCount(bits);
    }
    public ToolResult mac(ToolRequest request) {
        MacAddressInfo info = new MacOuiLookup().lookup(request.target);
        List<ToolResult.Row> rows = new ArrayList<>();
        rows.add(new ToolResult.Row("MAC", info.getNormalized()));
        rows.add(new ToolResult.Row("OUI", info.getOui()));
        String vendor = info.isLocallyAdministered() ? request.text("本地/随机地址，不据 OUI 推断厂商", "Locally administered; OUI is not vendor evidence")
                : info.getVendor().startsWith("Unknown") ? request.text("离线库未收录", "Not in bundled catalog") : info.getVendor();
        rows.add(new ToolResult.Row(request.text("厂商参考", "Vendor reference"), vendor));
        rows.add(new ToolResult.Row(request.text("地址类型", "Address type"), info.isMulticast() ? request.text("组播", "Multicast") : request.text("单播", "Unicast")));
        return new ToolResult(ToolResult.Status.OK, "MAC / OUI", request.text("精简离线参考库，不是完整 IEEE 厂商库。", "Small offline reference, not the complete IEEE registry."), rows, "");
    }
    public ToolResult ports(ToolRequest request) {
        List<ToolResult.Row> rows = new ArrayList<>();
        for (PortReference item : new CommonPortCatalog().list()) rows.add(new ToolResult.Row(item.getPort() + "/" + item.getProtocol(), item.getPurpose()));
        return new ToolResult(ToolResult.Status.OK, request.text("常用端口速查", "Port reference"), request.text("仅供参考；实际服务可使用其它端口。", "Reference only; services can use different ports."), rows, "");
    }
    public ToolResult extract(ToolRequest request) {
        if (request.target.length() > 20000) throw new IllegalArgumentException("Text too long");
        String output = new NetworkIdentifierExtractor().extract(request.target);
        List<ToolResult.Row> rows = new ArrayList<>();
        if (!output.isEmpty()) for (String line : output.split("\n")) rows.add(new ToolResult.Row("", line));
        return new ToolResult(ToolResult.Status.OK, request.text("地址与接口提取", "Address and interface extraction"),
                request.text("仅本地提取，最多200项；请核对识别结果。", "Local extraction, up to 200 entries. Review the results."), rows, "");
    }
}
