package com.chenwei666.netserial.network.toolbox;

import com.chenwei666.netserial.guided.network.BoundedDnsResolver;
import com.chenwei666.netserial.network.NetworkTargetValidator;
import java.net.InetAddress;
import java.io.IOException;

/** Shares the existing bounded two-worker resolver with guided diagnostics. */
public final class ToolboxResolver {
    private ToolboxResolver() { }
    public static InetAddress[] resolve(String host, ProbeSession session, int timeoutMs)
            throws IOException, InterruptedException {
        String safe = new NetworkTargetValidator().validate(host);
        session.check();
        InetAddress[] result = new BoundedDnsResolver().resolve(safe, session.remainingMs(timeoutMs));
        session.check();
        if (result.length == 0) throw new java.net.UnknownHostException(safe);
        return result;
    }
}
