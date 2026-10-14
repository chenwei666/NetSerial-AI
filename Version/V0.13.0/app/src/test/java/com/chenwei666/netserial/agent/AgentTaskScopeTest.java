package com.chenwei666.netserial.agent;

import com.chenwei666.netserial.ai.RequestCancellation;
import com.google.gson.JsonObject;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

/** Focused scope/evidence regressions; no real device or model is contacted. */
public class AgentTaskScopeTest {
    @Test public void chineseConfigurationNounDoesNotAuthorizeDiagnosticWrite() {
        Harness h = new Harness("帮我检查 VLAN 120 的配置", "show version", "show vlan brief", "configure terminal", "vlan 120");
        h.reject();
        assertFalse(h.sent.contains("vlan 120"));
    }
    @Test public void readOnlyGoalCannotAutoCreateVlanMentionedInGoal() {
        Harness h = new Harness("Check VLAN 120; read-only diagnostics",
                "show version", "show vlan brief", "configure terminal", "vlan 120");
        h.reject();
        assertFalse(h.sent.contains("vlan 120"));
    }

    @Test public void negativeInstructionCannotBecomeChangeAuthorization() {
        Harness h = new Harness("Don't change VLAN 120; inspect the configuration only",
                "show version", "show vlan brief", "configure terminal", "vlan 120");
        h.reject();
        assertFalse(h.sent.contains("vlan 120"));
    }

    @Test public void chineseReadOnlyInstructionCannotBecomeChangeAuthorization() {
        Harness h = new Harness("只读检查 VLAN 120 的配置，不要修改",
                "show version", "show vlan brief", "configure terminal", "vlan 120");
        h.reject();
        assertFalse(h.sent.contains("vlan 120"));
    }

    @Test public void explicitClarificationCanAuthorizeRequestedVlan() throws Exception {
        Harness h = new Harness("Create the VLAN I specify after you ask",
                "?Which VLAN should be created?", "show version", "show vlan brief",
                "configure terminal", "vlan 120", "end", "show vlan 120", "!finish");
        h.answer = "Create VLAN 120";
        h.run();
        assertTrue(h.sent.contains("vlan 120"));
        assertEquals(0, h.confirmed);
    }

    @Test public void unrelatedReadDomainCannotClearPendingWrite() {
        Harness h = new Harness("Create VLAN 120", "show version", "show vlan brief",
                "configure terminal", "vlan 120", "end", "show clock", "!finish");
        h.reject();
        assertTrue(h.sent.contains("vlan 120"));
        assertTrue(h.sent.contains("show clock"));
    }

    @Test public void differentVlanReadbackCannotVerifyChangedVlan() {
        Harness h = new Harness("Create VLAN 120", "show version", "show vlan brief",
                "configure terminal", "vlan 120", "end", "show vlan 121", "!finish");
        h.reject();
        assertTrue(h.sent.contains("vlan 120"));
        assertTrue(h.sent.contains("show vlan 121"));
    }

    @Test public void promptOnlyReadCannotAuthorizeConfiguration() {
        Harness h = new Harness("Create VLAN 120", "show version", "show vlan brief",
                "configure terminal", "vlan 120");
        h.output.put("show vlan brief", "Switch#");
        h.reject();
        assertFalse(h.sent.contains("vlan 120"));
    }

    @Test public void promptOnlyReadbackCannotVerifyConfiguration() {
        Harness h = new Harness("Create VLAN 120", "show version", "show vlan brief",
                "configure terminal", "vlan 120", "end", "show vlan 120", "!finish");
        h.output.put("show vlan 120", "Switch#");
        h.reject();
        assertTrue(h.sent.contains("vlan 120"));
    }

    @Test public void startupConfigurationCannotVerifyRunningChange() {
        Harness h = new Harness("Create VLAN 120", "show version", "show vlan brief",
                "configure terminal", "vlan 120", "end", "show startup-config", "!finish");
        h.output.put("show startup-config", "hostname Switch\n!\nvlan 1\nend\nSwitch#");
        h.reject();
        assertTrue(h.sent.contains("vlan 120"));
        assertTrue(h.sent.contains("show startup-config"));
    }

    @Test public void preexistingVlanContextDoesNotAuthorizeAutomaticDescription() {
        Harness h = new Harness("Create VLAN 120 with description Office", "show version",
                "show vlan brief", "description Office");
        h.output.put("show version", "Cisco IOS XE Software, Version 17.9\nSwitch(config-vlan)#");
        h.output.put("show vlan brief", "VLAN Name Status Ports\n10 existing active\nSwitch(config-vlan)#");
        h.reject();
        assertEquals(1, h.confirmed);
        assertFalse(h.sent.contains("description Office"));
    }

    @Test public void h3cAaaSubviewCannotMasqueradeAsSystemConfiguration() {
        Harness h = new Harness("Create VLAN 120", "display version", "display current-configuration",
                "system-view", "aaa", "vlan 120");
        h.output.put("display version", "H3C Comware Software, Version 7.1\n<H3C>");
        h.output.put("display current-configuration", "sysname H3C\n#\n<H3C>");
        h.output.put("system-view", "[H3C]");
        h.output.put("aaa", "[H3C-aaa]");
        h.approve = true;
        h.reject();
        assertTrue(h.sent.contains("aaa"));
        assertFalse(h.sent.contains("vlan 120"));
    }

    private static final class Harness implements AgentTaskEngine.Session, AgentTaskEngine.Listener {
        final String goal;
        final List<String> script, sent = new ArrayList<>();
        final Map<String, String> output = new HashMap<>();
        String answer = "";
        boolean approve;
        int index, confirmed;
        Harness(String goal, String... script) {
            this.goal = goal;
            this.script = Arrays.asList(script);
            output.put("show version", "Cisco IOS XE Software, Version 17.9\nSwitch#");
            output.put("show vlan brief", "VLAN Name Status Ports\n1 default active\nSwitch#");
            output.put("configure terminal", "Enter configuration commands, one per line.\nSwitch(config)#");
            output.put("vlan 120", "Switch(config-vlan)#");
            output.put("end", "Switch#");
            output.put("show vlan 120", "VLAN Name Status Ports\n120 office active\nSwitch#");
            output.put("show vlan 121", "VLAN Name Status Ports\n121 guest active\nSwitch#");
            output.put("show clock", "12:00:00 UTC Wed Oct 14 2026\nSwitch#");
        }
        void run() throws Exception {
            new AgentTaskEngine(messages -> encode(index < script.size() ? script.get(index++) : "!finish"),
                    this, this, high -> { }, false).run(goal, new RequestCancellation());
        }
        void reject() {
            try { run(); fail("Expected task to fail closed"); }
            catch (Exception expected) { assertNotNull(expected.getMessage()); }
        }
        @Override public String target() { return "lab-switch"; }
        @Override public boolean valid() { return true; }
        @Override public String execute(String command, RequestCancellation cancel) {
            sent.add(command);
            return output.getOrDefault(command, "Switch#");
        }
        @Override public void onEvent(String text) { }
        @Override public String ask(String question, RequestCancellation cancel) { return answer; }
        @Override public boolean confirm(String target, String command, RequestCancellation cancel) {
            confirmed++;
            return approve;
        }
    }
    private static String encode(String step) {
        JsonObject json = new JsonObject();
        if (step.startsWith("?")) {
            json.addProperty("type", "ASK");
            json.addProperty("message", step.substring(1));
        } else if (step.equals("!finish")) {
            json.addProperty("type", "FINISH");
            json.addProperty("message", "Evidence collected");
        } else {
            json.addProperty("type", "COMMAND");
            json.addProperty("command", step);
            json.addProperty("message", "Perform task step");
        }
        return json.toString();
    }
}
