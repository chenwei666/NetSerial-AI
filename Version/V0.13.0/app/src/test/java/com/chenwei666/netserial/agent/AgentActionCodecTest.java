package com.chenwei666.netserial.agent;

import org.junit.Test;
import static org.junit.Assert.*;

public class AgentActionCodecTest {
    @Test public void parsesExactlyOneCommandWithMessage() {
        AgentAction action = AgentActionCodec.parse("{\"type\":\"COMMAND\",\"command\":\"display version\",\"message\":\"identify device\"}");
        assertEquals(AgentAction.Type.COMMAND, action.getType());
        assertEquals("display version", action.getCommand());
        assertEquals("identify device", action.getMessage());
    }
    @Test public void parsesAskAndFinishWithoutCommand() {
        for (String type : new String[]{"ASK", "FINISH"}) {
            AgentAction action = AgentActionCodec.parse("{\"type\":\"" + type + "\",\"message\":\"details\"}");
            assertEquals(type, action.getType().name());
            assertEquals("", action.getCommand());
        }
    }
    @Test public void rejectsDuplicateFieldsIncludingEscapedNames() {
        invalid("{\"type\":\"ASK\",\"type\":\"FINISH\",\"message\":\"x\"}");
        invalid("{\"type\":\"COMMAND\",\"command\":\"show version\",\"comm\\u0061nd\":\"reload\",\"message\":\"x\"}");
        invalid("{\"type\":\"ASK\",\"message\":\"x\",\"message\":\"y\"}");
    }
    @Test public void rejectsProseMarkdownTrailingObjectsAndComments() {
        for (String text : new String[]{
                "Here is JSON {\"type\":\"ASK\",\"message\":\"x\"}",
                "```json\n{\"type\":\"ASK\",\"message\":\"x\"}\n```",
                "{\"type\":\"ASK\",\"message\":\"x\"} {}",
                "{\"type\":\"ASK\",\"message\":\"x\"} null",
                "/* trusted */{\"type\":\"ASK\",\"message\":\"x\"}",
                "{type:'ASK',message:'x'}", "{\"type\":\"ASK\",\"message\":\"x\",}"}) invalid(text);
    }
    @Test public void rejectsSchemaDriftCoercionsAndMissingFields() {
        for (String text : new String[]{null, "", "[]", "null", "{}",
                "{\"type\":\"command\",\"message\":\"x\"}",
                "{\"type\":\"ASK\",\"message\":\"x\",\"safe\":true}",
                "{\"type\":\"ASK\",\"message\":42}",
                "{\"type\":\"ASK\",\"message\":null}",
                "{\"type\":\"ASK\",\"message\":\"\"}",
                "{\"type\":\"ASK\",\"message\":\"x\",\"command\":\"\"}",
                "{\"type\":\"COMMAND\",\"message\":\"x\"}",
                "{\"type\":\"COMMAND\",\"message\":\"x\",\"command\":\" \"}",
                "{\"type\":\"COMMAND\",\"message\":\"x\",\"command\":[\"reload\"]}"}) invalid(text);
    }
    @Test public void boundsDocumentCommandAndMessage() {
        invalid(repeat(' ', 8193));
        invalid("{\"type\":\"COMMAND\",\"message\":\"x\",\"command\":\"" + repeat('a', 513) + "\"}");
        invalid("{\"type\":\"ASK\",\"message\":\"" + repeat('x', 4097) + "\"}");
    }
    @Test public void parsingDoesNotGrantPermissionToCommands() {
        AgentAction action = AgentActionCodec.parse("{\"type\":\"COMMAND\",\"command\":\"show version\\nreload\",\"message\":\"x\"}");
        assertEquals(AgentCommandPolicy.Decision.BLOCKED, new AgentCommandPolicy().evaluate(action.getCommand()));
    }
    @Test public void parsesWebAccessOnlyWithTypedProtocolsAndNoSecret() {
        AgentAction action = AgentActionCodec.parse(web("netadmin", "true", "false"));
        assertEquals(AgentAction.Type.WEB_ACCESS, action.getType());
        assertEquals("netadmin", action.getUsername());
        assertEquals("", action.getCommand());
        assertTrue(action.isHttps());
        assertFalse(action.isHttp());
        assertTrue(AgentActionCodec.parse(web("ops-1.example", "false", "true")).isHttp());
        assertTrue(AgentActionCodec.parse(web("ops_1", "true", "true")).isHttps());
    }
    @Test public void rejectsMalformedWebProtocolsAndUsernames() {
        for (String username : new String[]{"", "user name", "用户", "user;reboot", "user\\nnext", repeat('a', 33)})
            invalid(web(username, "true", "false"));
        for (String bool : new String[]{"\"true\"", "1", "null", "[]", "{}"}) {
            invalid(web("ops", bool, "false"));
            invalid(web("ops", "true", bool));
        }
        invalid(web("ops", "false", "false"));
        invalid("{\"type\":\"WEB_ACCESS\",\"message\":\"web\",\"username\":\"ops\",\"https\":true}");
    }
    @Test public void rejectsAnyWebSecretCommandOrDuplicateProtocol() {
        String base = web("ops", "true", "false");
        for (String extra : new String[]{"\"password\":\"synthetic\"", "\"secret\":\"synthetic\"",
                "\"command\":\"display version\"", "\"http\":true", "\"https\":false",
                "\"user\\u006eame\":\"other\""}) invalid(base.substring(0, base.length() - 1) + "," + extra + "}");
        invalid(base.replace("WEB_ACCESS", "web_access"));
        invalid(base.replace("\"message\":\"web\",", ""));
    }
    private static String web(String username, String https, String http) {
        return "{\"type\":\"WEB_ACCESS\",\"message\":\"web\",\"username\":\"" + username
                + "\",\"https\":" + https + ",\"http\":" + http + "}";
    }
    private static String repeat(char character, int count) {
        char[] value = new char[count]; java.util.Arrays.fill(value, character); return new String(value);
    }
    private static void invalid(String json) {
        try { AgentActionCodec.parse(json); fail("must reject schema"); }
        catch (IllegalArgumentException expected) { }
    }
}
