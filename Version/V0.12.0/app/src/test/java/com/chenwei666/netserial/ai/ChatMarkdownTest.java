package com.chenwei666.netserial.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChatMarkdownTest {
    @Test public void rendersStrongTextInsteadOfLiteralMarkers() {
        ChatMarkdown.Document doc = ChatMarkdown.parse("先检查 **连接状态**，再运行 `show version`。");
        ChatMarkdown.Block block = doc.blocks.get(0);
        assertEquals("先检查 连接状态，再运行 show version。", block.text);
        assertEquals(ChatMarkdown.Style.STRONG, block.spans.get(0).style);
        assertEquals("连接状态", block.text.substring(block.spans.get(0).start, block.spans.get(0).end));
        assertEquals(ChatMarkdown.Style.CODE, block.spans.get(1).style);
    }

    @Test public void fencedCommandsPreserveWildcardsSpacesAndRawNewlines() {
        ChatMarkdown.Document doc = ChatMarkdown.parse("检查：\n```text\r\n  display *\r\n** literal **\r\n```\n完成");
        assertEquals(3, doc.blocks.size());
        ChatMarkdown.Block code = doc.blocks.get(1);
        assertEquals(ChatMarkdown.Kind.CODE, code.kind);
        assertEquals("  display *\r\n** literal **\r\n", code.text);
        assertEquals("text", code.language);
        assertTrue(code.spans.isEmpty());
    }

    @Test public void headingsListsQuotesAndRulesBecomeReadableBlocks() {
        ChatMarkdown.Document doc = ChatMarkdown.parse("## 检查结果\n- **电源**正常\n2. 接口正常\n> 只读检查\n---\n普通说明");
        assertEquals(6, doc.blocks.size());
        assertEquals(ChatMarkdown.Kind.HEADING, doc.blocks.get(0).kind);
        assertEquals(2, doc.blocks.get(0).level);
        assertEquals("检查结果", doc.blocks.get(0).text);
        assertEquals("• 电源正常", doc.blocks.get(1).text);
        assertEquals(ChatMarkdown.Kind.LIST_ITEM, doc.blocks.get(2).kind);
        assertEquals("2. 接口正常", doc.blocks.get(2).text);
        assertEquals(ChatMarkdown.Kind.QUOTE, doc.blocks.get(3).kind);
        assertEquals("只读检查", doc.blocks.get(3).text);
        assertEquals(ChatMarkdown.Kind.RULE, doc.blocks.get(4).kind);
        assertEquals("普通说明", doc.blocks.get(5).text);
    }

    @Test(timeout = 2000) public void longAdversarialAnswersAreBoundedAndExplicitlyTruncated() {
        StringBuilder source = new StringBuilder();
        for (int i = 0; i < 10000; i++) source.append("- item\n");
        ChatMarkdown.Document list = ChatMarkdown.parse(source.toString());
        assertTrue(list.truncated);
        assertTrue(list.blocks.size() <= 96);
        source.setLength(0);
        for (int i = 0; i < 70000; i++) source.append('*');
        ChatMarkdown.Document stars = ChatMarkdown.parse(source.toString());
        assertTrue(stars.truncated);
        assertTrue(stars.blocks.size() <= 96);
    }

    @Test public void escapedMarkersRemainLiteralWhileUnderscoreStrongIsStyled() {
        ChatMarkdown.Block block = ChatMarkdown.parse("\\*\\*literal\\*\\* __结论__ config_file").blocks.get(0);
        assertEquals("**literal** 结论 config_file", block.text);
        assertEquals(1, block.spans.size());
        assertEquals(ChatMarkdown.Style.STRONG, block.spans.get(0).style);
    }

    @Test public void unclosedFenceIsReadableCodeWithoutInventingClosingText() {
        ChatMarkdown.Document doc = ChatMarkdown.parse("```cli\nshow *\n ** literal");
        assertEquals(1, doc.blocks.size());
        assertEquals(ChatMarkdown.Kind.CODE, doc.blocks.get(0).kind);
        assertEquals("show *\n ** literal", doc.blocks.get(0).text);
        assertFalse(doc.truncated);
    }

    @Test public void fenceMustMatchOpeningMarkerAndMinimumLength() {
        ChatMarkdown.Document doc = ChatMarkdown.parse("````text\n```\n~~~\n````\nlast");
        assertEquals(2, doc.blocks.size());
        assertEquals("```\n~~~\n", doc.blocks.get(0).text);
        assertEquals("last", doc.blocks.get(1).text);
    }

    @Test public void tildeFenceAndInlineCodePreserveMarkdownCharacters() {
        ChatMarkdown.Document doc = ChatMarkdown.parse("`display * ** _test_`\n\n~~~\n* ** __ # >\n~~~");
        assertEquals("display * ** _test_", doc.blocks.get(0).text);
        assertEquals(1, doc.blocks.get(0).spans.size());
        assertEquals(ChatMarkdown.Style.CODE, doc.blocks.get(0).spans.get(0).style);
        assertEquals("* ** __ # >\n", doc.blocks.get(1).text);
    }

    @Test public void unsupportedHtmlImagesAndLinksStayInertLiteralText() {
        String source = "<script>alert(1)</script> ![tracking](https://example.invalid/image) [link](file:///data/private)";
        ChatMarkdown.Block block = ChatMarkdown.parse(source).blocks.get(0);
        assertEquals(source, block.text);
        assertTrue(block.spans.isEmpty());
    }

    @Test public void malformedInlineSyntaxDoesNotLoseUserText() {
        String source = "display *; **not closed; `not closed; under_score";
        ChatMarkdown.Block block = ChatMarkdown.parse(source).blocks.get(0);
        assertEquals(source, block.text);
        assertTrue(block.spans.isEmpty());
    }

    @Test public void nullAndBlankInputHaveNoBlocks() {
        assertTrue(ChatMarkdown.parse(null).blocks.isEmpty());
        assertTrue(ChatMarkdown.parse("\r\n \n").blocks.isEmpty());
    }

    @Test public void tooManyInlineDecorationsFallBackToLiteralTextWithoutLosingContent() {
        StringBuilder source = new StringBuilder();
        for (int i = 0; i < 1000; i++) source.append("**a** ");
        ChatMarkdown.Block block = ChatMarkdown.parse(source.toString()).blocks.get(0);
        assertTrue(block.spans.size() <= 256);
        assertTrue(block.text.endsWith("**a** "));
    }

    @Test public void characterLimitDoesNotSplitEmojiSurrogatePair() {
        StringBuilder source = new StringBuilder();
        for (int i = 0; i < 47999; i++) source.append('a');
        source.append("😀tail");
        ChatMarkdown.Document doc = ChatMarkdown.parse(source.toString());
        assertTrue(doc.truncated);
        assertEquals(47999, doc.blocks.get(0).text.length());
        assertEquals('a', doc.blocks.get(0).text.charAt(47998));
    }
}
