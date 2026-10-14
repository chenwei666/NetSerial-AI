package com.chenwei666.netserial.network.toolbox;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.IDN;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Bounded DNS wire decoder. No recursive lookups or authority/additional record execution. */
public final class DnsWire {
    private DnsWire() { }
    public static final class Answer {
        public final String name, type, value;
        public final long ttl;
        Answer(String name, String type, String value, long ttl) {
            this.name = name; this.type = type; this.value = value; this.ttl = ttl;
        }
    }
    public static final class Reply {
        public final int rcode;
        public final boolean truncated;
        public final List<Answer> answers;
        Reply(int rcode, boolean truncated, List<Answer> answers) {
            this.rcode = rcode; this.truncated = truncated;
            this.answers = Collections.unmodifiableList(answers);
        }
    }
    public static int type(String name) {
        switch (name.toUpperCase(Locale.ROOT)) {
            case "A": return 1; case "NS": return 2; case "CNAME": return 5;
            case "PTR": return 12; case "MX": return 15; case "TXT": return 16; case "AAAA": return 28;
            default: throw new IllegalArgumentException("A / AAAA / CNAME / MX / TXT / NS / PTR");
        }
    }
    static String typeName(int type) {
        switch (type) {
            case 1: return "A"; case 2: return "NS"; case 5: return "CNAME";
            case 12: return "PTR"; case 15: return "MX"; case 16: return "TXT"; case 28: return "AAAA";
            default: return "TYPE" + type;
        }
    }
    public static String normalizeName(String value) {
        if (value == null || value.length() > 1024) throw new IllegalArgumentException("Invalid DNS name");
        String name = value.endsWith(".") ? value.substring(0, value.length() - 1) : value;
        // TXT owners such as _dmarc and _domainkey are DNS names, not hostnames.
        name = IDN.toASCII(name).toLowerCase(Locale.ROOT);
        if (name.isEmpty() || name.length() > 253) throw new IllegalArgumentException("Invalid DNS name");
        for (String label : name.split("\\.", -1))
            if (!label.matches("[a-z0-9_-]{1,63}")) throw new IllegalArgumentException("Invalid DNS label");
        return name;
    }
    public static byte[] query(int id, String name, int type) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeShort(id); out.writeShort(0x0100); out.writeShort(1);
        out.writeShort(0); out.writeShort(0); out.writeShort(0);
        for (String label : normalizeName(name).split("\\.")) {
            byte[] data = label.getBytes(StandardCharsets.US_ASCII); out.writeByte(data.length); out.write(data);
        }
        out.writeByte(0); out.writeShort(type); out.writeShort(1);
        return bytes.toByteArray();
    }
    public static Reply parse(byte[] data, int id, String qname, int qtype) throws IOException {
        Cursor c = new Cursor(data);
        if (c.u16() != (id & 65535)) throw new IOException("DNS transaction mismatch");
        int flags = c.u16();
        if ((flags & 0x8000) == 0 || (flags & 0x7800) != 0) throw new IOException("Invalid DNS response flags");
        int questions = c.u16(), answers = c.u16(), authorities = c.u16(), additional = c.u16();
        if (questions != 1 || answers + authorities + additional > 256) throw new IOException("DNS record limit");
        if (!c.name().equalsIgnoreCase(normalizeName(qname)) || c.u16() != qtype || c.u16() != 1)
            throw new IOException("DNS question mismatch");
        if ((flags & 0x0200) != 0) return new Reply(flags & 15, true, new ArrayList<>());
        List<Answer> result = new ArrayList<>();
        for (int i = 0; i < answers + authorities + additional; i++) {
            String name = c.name(); int type = c.u16(), klass = c.u16();
            long ttl = ((long) c.u16() << 16) | c.u16();
            int len = c.u16(), start = c.pos, end = start + len;
            c.require(len);
            String value = null;
            if (i < answers && klass == 1) {
                if (type == 1 || type == 28) {
                    if (len != (type == 1 ? 4 : 16)) throw new IOException("Invalid DNS address length");
                    byte[] address = new byte[len]; System.arraycopy(data, start, address, 0, len);
                    value = InetAddress.getByAddress(address).getHostAddress();
                    c.pos = end;
                } else if (type == 2 || type == 5 || type == 12) value = c.name();
                else if (type == 15) value = c.u16() + " " + c.name();
                else if (type == 16) {
                    StringBuilder text = new StringBuilder();
                    while (c.pos < end) {
                        int n = c.u8(); if (c.pos + n > end) throw new IOException("Invalid TXT length");
                        if (text.length() > 0) text.append(" ");
                        text.append(new String(data, c.pos, n, StandardCharsets.UTF_8)); c.pos += n;
                    }
                    value = text.toString();
                }
                if (value != null && c.pos != end) throw new IOException("DNS RDATA boundary mismatch");
                if (value != null) result.add(new Answer(name, typeName(type), printable(value, 2048), ttl));
            }
            c.pos = end;
        }
        if (c.pos != data.length) throw new IOException("Trailing DNS data");
        return new Reply(flags & 15, false, result);
    }
    static String printable(String s, int max) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length() && out.length() < max; i++) {
            char ch = s.charAt(i);
            out.append(Character.isISOControl(ch) || Character.getType(ch) == Character.FORMAT ? ' ' : ch);
        }
        return out.toString();
    }
    private static final class Cursor {
        final byte[] data; int pos;
        Cursor(byte[] data) throws IOException {
            if (data == null || data.length < 12 || data.length > 16384) throw new IOException("DNS frame size");
            this.data = data;
        }
        void require(int n) throws IOException { if (n < 0 || pos + n > data.length) throw new IOException("Truncated DNS frame"); }
        int u8() throws IOException { require(1); return data[pos++] & 255; }
        int u16() throws IOException { return (u8() << 8) | u8(); }
        String name() throws IOException {
            int at = pos, next = -1, steps = 0, total = 0;
            StringBuilder name = new StringBuilder();
            while (true) {
                if (at >= data.length || ++steps > 128) throw new IOException("DNS name pointer loop");
                int size = data[at++] & 255;
                if ((size & 0xc0) == 0xc0) {
                    if (at >= data.length) throw new IOException("Truncated DNS pointer");
                    int pointer = ((size & 63) << 8) | (data[at++] & 255);
                    if (pointer >= at - 2 || pointer < 12) throw new IOException("Invalid DNS pointer");
                    if (next < 0) next = at;
                    at = pointer; continue;
                }
                if ((size & 0xc0) != 0 || at + size > data.length) throw new IOException("Invalid DNS label");
                if (size == 0) { pos = next < 0 ? at : next; return name.toString(); }
                total += size + 1; if (total > 254) throw new IOException("DNS name too long");
                if (name.length() > 0) name.append('.');
                for (int i = 0; i < size; i++) {
                    int ch = data[at++] & 255;
                    if (ch <= 32 || ch >= 127 || ch == '.') throw new IOException("Unsupported DNS label encoding");
                    name.append((char) ch);
                }
            }
        }
    }
}
