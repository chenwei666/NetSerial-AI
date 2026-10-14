package com.chenwei666.netserial.network.toolbox;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses packet evidence, never a subprocess duration or ping's summary as a reply. */
final class PingOutput {
    private static final Pattern RTT = Pattern.compile("(?i)\\btime\\s*([=<])\\s*([0-9]+(?:\\.[0-9]+)?)\\s*ms\\b");
    private static final Pattern FROM = Pattern.compile("(?i)\\bfrom\\s+([^\\r\\n]+)");
    final boolean reply, lessThan, unsupported, timeExceeded, tooLarge;
    final double rtt;
    final String router;

    private PingOutput(boolean reply, boolean lessThan, double rtt, boolean unsupported,
                       boolean timeExceeded, boolean tooLarge, String router) {
        this.reply = reply; this.lessThan = lessThan; this.rtt = rtt;
        this.unsupported = unsupported; this.timeExceeded = timeExceeded;
        this.tooLarge = tooLarge; this.router = router;
    }

    static PingOutput parse(String output) {
        String text = output == null ? "" : output;
        String lower = text.toLowerCase(Locale.ROOT);
        boolean unsupported = lower.contains("operation not permitted") || lower.contains("permission denied")
                || lower.contains("not found") || lower.contains("unknown option") || lower.contains("invalid option")
                || lower.contains("unrecognized option") || lower.contains("bad option")
                || lower.contains("not supported") || lower.contains("usage: ping");
        boolean exceeded = lower.contains("time to live exceeded") || lower.contains("time exceeded")
                || lower.contains("ttl expired") || lower.contains("ttl exceeded") || lower.contains("hop limit exceeded");
        boolean large = lower.contains("message too long") || lower.contains("frag needed")
                || lower.contains("fragmentation needed") || lower.contains("packet too big");
        double value = Double.NaN;
        boolean less = false;
        String router = "";
        for (String line : text.split("\\r?\\n")) {
            String lineLower = line.toLowerCase(Locale.ROOT);
            if (lineLower.contains("bytes from") && !lineLower.contains("exceeded") && !lineLower.contains("unreachable")) {
                Matcher match = RTT.matcher(line);
                if (match.find()) {
                    double parsed = Double.parseDouble(match.group(2));
                    if (!Double.isNaN(parsed) && !Double.isInfinite(parsed) && parsed >= 0 && parsed <= 120000) {
                        value = parsed; less = "<".equals(match.group(1));
                    }
                }
            }
            if (exceeded && (lineLower.contains("exceeded") || lineLower.contains("expired"))) {
                Matcher from = FROM.matcher(line);
                if (from.find()) router = literalRouter(from.group(1));
            }
        }
        return new PingOutput(!Double.isNaN(value) && !unsupported, less, value, unsupported, exceeded, large, router);
    }

    private static String literalRouter(String text) {
        // Numeric ping (-n) normally prints a literal; parenthesized literal handles vendor variants.
        Matcher parenthesized = Pattern.compile("\\(([0-9a-fA-F:.]+)\\)").matcher(text);
        String candidate = parenthesized.find() ? parenthesized.group(1) : text.split("\\s+")[0];
        if (candidate.endsWith(":")) candidate = candidate.substring(0, candidate.length() - 1);
        if (candidate.matches("[0-9]{1,3}(?:\\.[0-9]{1,3}){3}")) {
            for (String part : candidate.split("\\.")) if (Integer.parseInt(part) > 255) return "";
            return candidate;
        }
        return candidate.contains(":") && candidate.matches("[0-9a-fA-F:]+") ? candidate : "";
    }

    String rttText() { return (lessThan ? "<" : "") + String.format(Locale.ROOT, "%.3f ms", rtt); }
}
