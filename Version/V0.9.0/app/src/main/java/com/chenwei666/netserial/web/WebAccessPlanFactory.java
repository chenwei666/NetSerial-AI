package com.chenwei666.netserial.web;

import com.chenwei666.netserial.device.Vendor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Builds reviewable vendor plans. It never sends commands or persists credentials. */
public final class WebAccessPlanFactory {
    public WebAccessPlan create(WebAccessRequest request) {
        char[] password = request.copyPassword();
        try {
            String secret = new String(password);
            List<String> commands = new ArrayList<>();
            List<String> redacted = new ArrayList<>();
            List<String> verify = new ArrayList<>();
            List<String> rollback = new ArrayList<>();
            switch (request.getVendor()) {
                case H3C_COMWARE:
                    h3c(request, secret, commands, redacted, verify, rollback);
                    break;
                case HUAWEI_VRP:
                    huawei(request, secret, commands, redacted, verify, rollback);
                    break;
                case CISCO_IOS:
                    cisco(request, secret, commands, redacted, verify, rollback);
                    break;
                case RUIJIE_RGOS:
                    ruijie(request, secret, commands, redacted, verify, rollback);
                    break;
                default:
                    throw new IllegalArgumentException("unsupported vendor");
            }
            return new WebAccessPlan(request.getVendor(), commands, redacted, verify, rollback,
                    request.isHttpEnabled());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static void h3c(WebAccessRequest r, String password, List<String> c, List<String> redacted,
                            List<String> verify, List<String> rollback) {
        add(c, redacted, "system-view");
        String user = r.getPlatform().toLowerCase().contains("comware 5")
                ? "local-user " + r.getUsername()
                : "local-user " + r.getUsername() + " class manage";
        add(c, redacted, user);
        secret(c, redacted, "password simple ", password);
        add(c, redacted, "service-type" + (r.isHttpEnabled() ? " http" : "")
                + (r.isHttpsEnabled() ? " https" : ""));
        add(c, redacted, r.getPlatform().toLowerCase().contains("comware 5")
                ? "authorization-attribute level 3" : "authorization-attribute user-role network-admin");
        add(c, redacted, "quit");
        if (r.isHttpsEnabled()) add(c, redacted, "ip https enable");
        if (r.isHttpEnabled()) add(c, redacted, "ip http enable");
        add(c, redacted, "return");
        verify.add("display ip https");
        if (r.isHttpEnabled()) verify.add("display ip http");
        rollback.add("system-view");
        if (r.isHttpEnabled()) rollback.add("undo ip http enable");
        if (r.isHttpsEnabled()) rollback.add("undo ip https enable");
        rollback.add("undo local-user " + r.getUsername());
        rollback.add("return");
    }

    private static void huawei(WebAccessRequest r, String password, List<String> c, List<String> redacted,
                               List<String> verify, List<String> rollback) {
        add(c, redacted, "system-view");
        if (r.isHttpsEnabled()) add(c, redacted, "http secure-server enable");
        if (r.isHttpEnabled()) add(c, redacted, "http server enable");
        add(c, redacted, "aaa");
        secret(c, redacted, "local-user " + r.getUsername() + " password irreversible-cipher ", password);
        add(c, redacted, "local-user " + r.getUsername() + " privilege level 15");
        add(c, redacted, "local-user " + r.getUsername() + " service-type http");
        add(c, redacted, "quit");
        add(c, redacted, "return");
        verify.add("display http server");
        rollback.add("system-view");
        if (r.isHttpEnabled()) rollback.add("undo http server enable");
        if (r.isHttpsEnabled()) rollback.add("undo http secure-server enable");
        rollback.add("aaa");
        rollback.add("undo local-user " + r.getUsername());
        rollback.add("quit");
        rollback.add("return");
    }

    private static void cisco(WebAccessRequest r, String password, List<String> c, List<String> redacted,
                              List<String> verify, List<String> rollback) {
        add(c, redacted, "configure terminal");
        secret(c, redacted, "username " + r.getUsername() + " privilege 15 secret ", password);
        add(c, redacted, "ip http authentication local");
        if (r.isHttpsEnabled()) add(c, redacted, "ip http secure-server");
        if (r.isHttpEnabled()) add(c, redacted, "ip http server");
        add(c, redacted, "end");
        verify.add("show running-config | include ^ip http|^username " + r.getUsername());
        rollback.add("configure terminal");
        if (r.isHttpEnabled()) rollback.add("no ip http server");
        if (r.isHttpsEnabled()) rollback.add("no ip http secure-server");
        rollback.add("no username " + r.getUsername());
        rollback.add("end");
    }

    private static void ruijie(WebAccessRequest r, String password, List<String> c, List<String> redacted,
                              List<String> verify, List<String> rollback) {
        add(c, redacted, "configure terminal");
        secret(c, redacted, "webmaster level 0 username " + r.getUsername() + " password ", password);
        if (r.isHttpsEnabled()) add(c, redacted, "enable service web-server https");
        if (r.isHttpEnabled()) add(c, redacted, "enable service web-server http");
        add(c, redacted, "end");
        verify.add("show web-server status");
        verify.add("show service");
        rollback.add("configure terminal");
        if (r.isHttpEnabled()) rollback.add("no enable service web-server http");
        if (r.isHttpsEnabled()) rollback.add("no enable service web-server https");
        rollback.add("end");
    }

    private static void add(List<String> commands, List<String> redacted, String command) {
        commands.add(command);
        redacted.add(command);
    }

    private static void secret(List<String> commands, List<String> redacted, String prefix, String password) {
        commands.add(prefix + password);
        redacted.add(prefix + "[REDACTED]");
    }
}
