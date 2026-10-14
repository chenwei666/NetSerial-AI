package com.chenwei666.netserial.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small, inert Markdown subset. No HTML, images, link activation, or command execution. */
public final class ChatMarkdown {
    public static final int MAX_CHARACTERS = 48000;
    public static final int MAX_BLOCKS = 96;
    private static final int MAX_SPANS = 256;
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern LIST = Pattern.compile("^([-+*]|[0-9]{1,6}[.)])\\s+(.+)$");
    public enum Kind { PARAGRAPH, HEADING, LIST_ITEM, QUOTE, RULE, CODE }
    public enum Style { STRONG, CODE }
    public static final class Span {
        public final int start, end;
        public final Style style;
        Span(int start, int end, Style style) { this.start = start; this.end = end; this.style = style; }
    }
    public static final class Block {
        public final Kind kind;
        public final String text, language;
        public final int level;
        public final List<Span> spans;
        Block(Kind kind, String text, String language, int level, List<Span> spans) {
            this.kind = kind; this.text = text; this.language = language; this.level = level;
            this.spans = Collections.unmodifiableList(spans);
        }
    }
    public static final class Document {
        public final List<Block> blocks;
        public final boolean truncated;
        Document(List<Block> blocks, boolean truncated) {
            this.blocks = Collections.unmodifiableList(blocks); this.truncated = truncated;
        }
    }
    private ChatMarkdown() { }

    public static Document parse(String source) {
        if (source == null) source = "";
        boolean truncated = source.length() > MAX_CHARACTERS;
        if (truncated) {
            int end = Character.isHighSurrogate(source.charAt(MAX_CHARACTERS - 1))
                    ? MAX_CHARACTERS - 1 : MAX_CHARACTERS;
            source = source.substring(0, end);
        }
        List<Block> blocks = new ArrayList<>();
        StringBuilder paragraph = new StringBuilder();
        StringBuilder code = null;
        String language = "";
        char fence = 0;
        int fenceSize = 0;
        int position = 0;
        while (position < source.length() && blocks.size() < MAX_BLOCKS - 2) {
            int newline = source.indexOf('\n', position);
            int next = newline < 0 ? source.length() : newline + 1;
            String raw = source.substring(position, next);
            String line = raw.replaceFirst("\\r?\\n$", "");
            String trimmed = line.trim();
            if (code != null) {
                if (closingFence(trimmed, fence, fenceSize)) {
                    blocks.add(new Block(Kind.CODE, code.toString(), language, 0, new ArrayList<>()));
                    code = null;
                } else code.append(raw);
            } else if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                flush(paragraph, blocks);
                fence = trimmed.charAt(0);
                fenceSize = 0;
                while (fenceSize < trimmed.length() && trimmed.charAt(fenceSize) == fence) fenceSize++;
                language = trimmed.substring(fenceSize).trim();
                if (language.length() > 32) language = language.substring(0, 32);
                code = new StringBuilder();
            } else if (trimmed.isEmpty()) flush(paragraph, blocks);
            else {
                Matcher heading = HEADING.matcher(trimmed);
                Matcher list = LIST.matcher(trimmed);
                if (heading.matches()) {
                    flush(paragraph, blocks);
                    blocks.add(inline(Kind.HEADING, heading.group(2), heading.group(1).length()));
                } else if (isRule(trimmed)) {
                    flush(paragraph, blocks);
                    blocks.add(new Block(Kind.RULE, "", "", 0, new ArrayList<>()));
                } else if (list.matches()) {
                    flush(paragraph, blocks);
                    String marker = list.group(1);
                    blocks.add(inline(Kind.LIST_ITEM, (marker.length() == 1 ? "•" : marker) + " " + list.group(2), 0));
                } else if (trimmed.startsWith("> ") || trimmed.equals(">")) {
                    flush(paragraph, blocks);
                    blocks.add(inline(Kind.QUOTE, trimmed.substring(1).trim(), 0));
                } else {
                    if (paragraph.length() > 0) paragraph.append('\n');
                    paragraph.append(line);
                }
            }
            position = next;
        }
        if (code != null) blocks.add(new Block(Kind.CODE, code.toString(), language, 0, new ArrayList<>()));
        flush(paragraph, blocks);
        return new Document(blocks, truncated || position < source.length());
    }

    private static boolean isRule(String text) {
        if (text.isEmpty() || "-*_".indexOf(text.charAt(0)) < 0) return false;
        char marker = text.charAt(0);
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char value = text.charAt(i);
            if (value == marker) count++;
            else if (value != ' ' && value != '\t') return false;
        }
        return count >= 3;
    }

    private static boolean closingFence(String text, char marker, int size) {
        int count = 0;
        while (count < text.length() && text.charAt(count) == marker) count++;
        return count >= size && text.substring(count).trim().isEmpty();
    }

    private static void flush(StringBuilder paragraph, List<Block> blocks) {
        if (paragraph.length() == 0) return;
        blocks.add(inline(Kind.PARAGRAPH, paragraph.toString(), 0));
        paragraph.setLength(0);
    }

    private static Block inline(Kind kind, String text, int level) {
        StringBuilder out = new StringBuilder();
        List<Span> spans = new ArrayList<>();
        int cursor = 0;
        while (cursor < text.length()) {
            if (spans.size() >= MAX_SPANS) { out.append(text, cursor, text.length()); break; }
            if (text.charAt(cursor) == '\\' && cursor + 1 < text.length()
                    && "*_`\\".indexOf(text.charAt(cursor + 1)) >= 0) {
                out.append(text.charAt(cursor + 1)); cursor += 2; continue;
            }
            String delimiter = text.startsWith("**", cursor) ? "**"
                    : text.startsWith("__", cursor) ? "__"
                    : text.charAt(cursor) == '`' ? "`" : null;
            int end = delimiter == null ? -1 : text.indexOf(delimiter, cursor + delimiter.length());
            if (delimiter != null && end > cursor + delimiter.length()) {
                int start = out.length();
                out.append(text, cursor + delimiter.length(), end);
                spans.add(new Span(start, out.length(), "`".equals(delimiter) ? Style.CODE : Style.STRONG));
                cursor = end + delimiter.length();
            } else { out.append(text.charAt(cursor++)); }
        }
        return new Block(kind, out.toString(), "", level, spans);
    }
}
