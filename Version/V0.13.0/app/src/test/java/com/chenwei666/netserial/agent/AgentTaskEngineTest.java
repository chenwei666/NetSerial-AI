package com.chenwei666.netserial.agent;

import com.chenwei666.netserial.ai.AiChatMessage;
import com.chenwei666.netserial.ai.AiChatRole;
import com.chenwei666.netserial.ai.RequestCancellation;
import com.chenwei666.netserial.device.Vendor;
import com.google.gson.JsonObject;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

/** No real transport, credentials or model: exercises actual orchestration with scripted peers. */
public class AgentTaskEngineTest {
    private static final String VERSION = "Cisco IOS XE Software, Version 17.9\r\nSwitch#";

    @Test public void identifiesReadsCreatesVlanAndVerifiesWithoutConfirmation() throws Exception {
        Fixture f = fixture(command("show version"), command("show vlan brief"),
                command("configure terminal"), command("vlan 120"), command("description Office"),
                command("end"), command("show vlan 120"), finish());
        f.run();
        assertEquals(7, f.sent.size());
        assertEquals(0, f.confirmations.size());
        assertTrue(f.events.stream().anyMatch(v -> v.contains("NOT proof")));
    }

    @Test public void configurationRequiresIdentifiedVersion() {
        Fixture f = fixture(command("configure terminal"));
        fails(f);
        assertTrue(f.sent.isEmpty());
    }

    @Test public void ambiguousVersionCannotAuthorizeConfiguration() {
        Fixture f = fixture(command("show version"), command("show vlan brief"), command("configure terminal"));
        f.outputs.put("show version", "Switch#");
        fails(f);
        assertFalse(f.sent.contains("configure terminal"));
    }

    @Test public void writeRequiresCurrentStatePrecheck() {
        Fixture f = fixture(command("show version"), command("configure terminal"), command("vlan 120"));
        fails(f);
        assertFalse(f.sent.contains("vlan 120"));
    }

    @Test public void unrelatedClockReadIsNotVlanPrecheck() {
        Fixture f = fixture(command("show version"), command("show clock"),
                command("configure terminal"), command("vlan 120"));
        fails(f);
        assertFalse(f.sent.contains("vlan 120"));
    }

    @Test public void missingConfigurationPromptCannotAuthorizeVlanWrite() {
        Fixture f = fixture(command("show version"), command("show vlan brief"),
                command("configure terminal"), command("vlan 120"));
        f.outputs.put("configure terminal", "Switch#");
        fails(f);
        assertFalse(f.sent.contains("vlan 120"));
    }

    @Test public void deniedHighRiskCommandIsNeverSent() {
        Fixture f = fixture(command("show version"), command("show running-config"), command("reload"));
        f.approve = false;
        fails(f);
        assertEquals(Arrays.asList("reload"), f.confirmations);
        assertFalse(f.sent.contains("reload"));
    }

    @Test public void descriptionOutsideVlanNeedsConfirmation() {
        Fixture f = fixture(command("show version"), command("show running-config"),
                command("configure terminal"), command("description Office"));
        f.approve = false;
        fails(f);
        assertFalse(f.sent.contains("description Office"));
    }

    @Test public void sessionChangeDuringApprovalPreventsSend() {
        Fixture f = fixture(command("show version"), command("show running-config"), command("reload"));
        f.onConfirm = () -> f.valid = false;
        fails(f);
        assertFalse(f.sent.contains("reload"));
    }

    @Test public void expiredTargetGateDuringApprovalPreventsSend() {
        Fixture f = fixture(command("show version"), command("show running-config"), command("reload"));
        f.onConfirm = () -> f.gateAllowed = false;
        fails(f);
        assertFalse(f.sent.contains("reload"));
    }

    @Test public void cancellationWhileModelThinksPreventsNextCommand() {
        Fixture f = fixture(command("show version"), command("show vlan brief"),
                command("configure terminal"), command("vlan 120"));
        f.cancelAtModelCall = 4;
        fails(f);
        assertFalse(f.sent.contains("vlan 120"));
    }

    @Test public void preCancelledTaskDoesNotCallModelOrTransport() {
        Fixture f = fixture(command("show version"));
        f.cancel.cancel();
        fails(f);
        assertEquals(0, f.modelCalls);
        assertTrue(f.sent.isEmpty());
    }

    @Test public void writeCannotBeRepeatedEvenWithDifferentWhitespace() {
        Fixture f = fixture(command("show version"), command("show vlan brief"),
                command("configure terminal"), command("vlan 120"), command("end"),
                command("configure terminal"), command("VLAN  120"));
        fails(f);
        assertFalse(f.sent.contains("VLAN  120"));
    }

    @Test public void finishWithoutReadbackAfterWriteFails() {
        Fixture f = fixture(command("show version"), command("show vlan brief"),
                command("configure terminal"), command("vlan 120"), finish());
        fails(f);
        assertEquals(4, f.sent.size());
    }

    @Test public void unrelatedClockCannotStandInForConfigurationVerification() {
        Fixture f = fixture(command("show version"), command("show vlan brief"),
                command("configure terminal"), command("vlan 120"), command("end"),
                command("show clock"), finish());
        fails(f);
    }

    @Test public void deviceErrorStopsBeforeAnotherAction() {
        Fixture f = fixture(command("show version"), command("show vlan brief"),
                command("configure terminal"), command("vlan 120"), command("description Office"));
        f.outputs.put("vlan 120", "% Invalid input detected at '^' marker.\r\nSwitch(config)#");
        fails(f);
        assertFalse(f.sent.contains("description Office"));
    }

    @Test public void versionIdentityCannotChangeMidTask() {
        Fixture f = fixture(command("show version"), command("display version"), finish());
        f.outputs.put("display version", "H3C Comware Software, Version 7.1\r\n<H3C>");
        fails(f);
    }

    @Test public void answerIsIncludedInNextModelContext() throws Exception {
        Fixture f = fixture(ask("Which VLAN?"), command("show version"), finish());
        f.answer = "VLAN 120 for Office";
        f.run();
        assertTrue(f.contexts.get(1).stream().anyMatch(v -> v.getContent().contains("VLAN 120 for Office")));
        assertTrue(f.contexts.get(2).stream().anyMatch(v -> v.getContent().contains("<untrusted-device-output>")));
    }

    @Test public void oversizedAnswerDoesNotReachModel() {
        Fixture f = fixture(ask("Which VLAN?"));
        f.answer = repeat("x", 2001);
        fails(f);
        assertEquals(1, f.modelCalls);
        assertTrue(f.sent.isEmpty());
    }

    @Test public void ordinaryModelProseCannotBecomeCommands() {
        Fixture f = fixture("Run show version now.");
        fails(f);
        assertTrue(f.sent.isEmpty());
    }

    @Test public void modelCommandInjectionCannotReachTransport() {
        Fixture f = fixture(command("show version\nreload"));
        fails(f);
        assertTrue(f.sent.isEmpty());
    }

    @Test public void modelCannotLoopBeyondStepBudget() {
        String[] actions = new String[40];
        Arrays.fill(actions, command("show version"));
        Fixture f = fixture(actions);
        fails(f);
        assertEquals(32, f.modelCalls);
        assertEquals(32, f.sent.size());
    }

    @Test public void transportFailureIsNeverRetried() {
        Fixture f = fixture(command("show version"), command("show version"));
        f.transportFailure = true;
        fails(f);
        assertEquals(1, f.sent.size());
        assertEquals(1, f.modelCalls);
    }

    @Test public void outputIsBoundedBeforeSendingToModel() throws Exception {
        Fixture f = fixture(command("show version"), command("show running-config"), finish());
        f.outputs.put("show running-config", repeat("hostname office\n", 1000) + "\nSwitch#");
        f.run();
        AiChatMessage observation = f.contexts.get(2).get(f.contexts.get(2).size() - 1);
        assertTrue(observation.getContent().contains("TRUNCATED"));
        assertTrue(observation.getContent().length() < 9000);
    }

    @Test public void approvedHighRiskWriteStillRequiresReadback() throws Exception {
        Fixture f = fixture(command("show version"), command("show running-config"),
                command("configure terminal"), command("ip http secure-server"), command("end"),
                command("show running-config"), finish());
        f.outputs.put("ip http secure-server", "Switch(config)#");
        f.run();
        assertEquals(Arrays.asList("ip http secure-server"), f.confirmations);
        assertTrue(f.sent.contains("ip http secure-server"));
    }

    @Test public void laterWriteInvalidatesEarlierVerification() {
        Fixture f = fixture(command("show version"), command("show vlan brief"),
                command("configure terminal"), command("vlan 120"), command("end"),
                command("show vlan 120"), command("configure terminal"), command("vlan 121"), finish());
        f.outputs.put("vlan 121", "Switch(config-vlan)#");
        fails(f);
    }

    @Test public void deviceCredentialsAreRedactedFromModelContextAndEvents() throws Exception {
        Fixture f = fixture(command("show version"), command("show running-config"), finish());
        String synthetic = "unit-test-placeholder-not-a-secret";
        f.outputs.put("show running-config", "hostname Switch\nusername demo secret 0 " + synthetic + "\nSwitch#");
        f.run();
        for (List<AiChatMessage> context : f.contexts) {
            for (AiChatMessage message : context) assertFalse(message.getContent().contains(synthetic));
        }
        for (String event : f.events) assertFalse(event.contains(synthetic));
        assertTrue(f.events.stream().anyMatch(v -> v.contains("[REDACTED]")));
    }

    @Test public void chatAnswerContainingCredentialIsRejectedLocally() {
        Fixture f = fixture(ask("Which account?"));
        f.answer = "password=unit-test-placeholder";
        fails(f);
        assertEquals(1, f.modelCalls);
        assertTrue(f.sent.isEmpty());
    }

    @Test public void assistantHistoryUsesTheSameStrictProtocolAsModelResponses() throws Exception {
        Fixture f = fixture(command("show version"), finish());
        f.run();
        int encodedActions = 0;
        for (AiChatMessage message : f.contexts.get(1)) {
            if (message.getRole() == AiChatRole.ASSISTANT) {
                AgentAction restored = AgentActionCodec.parse(message.getContent());
                assertEquals(AgentAction.Type.COMMAND, restored.getType());
                assertEquals("show version", restored.getCommand());
                encodedActions++;
            }
        }
        assertEquals(1, encodedActions);
    }

    @Test public void webAccessRequiresIdentificationBeforeLocalCallback() {
        Fixture f = webFixture(command("show running-config"), web());
        fails(f);
        assertEquals(0, f.webCalls);
    }

    @Test public void webAccessRequiresRelevantPrecheckBeforeLocalCallback() {
        Fixture f = webFixture(command("show version"), command("show clock"), web());
        fails(f);
        assertEquals(0, f.webCalls);
    }

    @Test public void webAccessCallsLocalHandlerOnceThenEndsModelLoop() throws Exception {
        Fixture f = webFixture(command("show version"), command("show running-config"), web(), command("reload"));
        f.run();
        assertEquals(1, f.webCalls);
        assertEquals(3, f.modelCalls);
        assertEquals(Arrays.asList("show version", "show running-config"), f.sent);
        assertEquals(Vendor.CISCO_IOS, f.webVendor);
        assertEquals("netadmin", f.webAction.getUsername());
        assertTrue(f.webAction.isHttps());
        assertFalse(f.webAction.isHttp());
        assertTrue(f.events.contains("Local Web execution complete; verification pending"));
    }

    @Test public void webAccessAcceptsWebStatusPrecheckWithoutFullConfig() throws Exception {
        Fixture f = webFixture(command("show version"), command("show ip http server status"), web());
        f.outputs.put("show ip http server status", "HTTP secure server status: Disabled\nSwitch#");
        f.run();
        assertEquals(1, f.webCalls);
    }

    @Test public void webAccessDefaultListenerFailsClosed() {
        Fixture f = webFixture(command("show version"), command("show running-config"), web());
        f.webAvailable = false;
        fails(f);
        assertEquals(0, f.webCalls);
    }

    @Test public void webAccessCannotBypassProtectedTargetGate() {
        Fixture f = webFixture(command("show version"), command("show running-config"), web());
        f.highGateAllowed = false;
        fails(f);
        assertEquals(0, f.webCalls);
    }

    @Test public void modelCannotPutPasswordInWebAction() {
        String invalid = web().replace("\"username\":", "\"password\":\"synthetic-placeholder\",\"username\":");
        Fixture f = webFixture(command("show version"), command("show running-config"), invalid);
        fails(f);
        assertEquals(0, f.webCalls);
        assertEquals(2, f.sent.size());
    }

    @Test public void modelCannotAutoCreateVlanOutsideExplicitTaskScope() {
        Fixture f = fixture(command("show version"), command("show vlan brief"),
                command("configure terminal"), command("vlan 121"));
        f.outputs.put("vlan 121", "Switch(config-vlan)#");
        f.approve = false;
        fails(f);
        assertFalse(f.sent.contains("vlan 121"));
    }

    @Test public void eachChangedDomainNeedsVerificationNotJustLastWrite() {
        Fixture f = fixture(command("show version"), command("show running-config"),
                command("configure terminal"), command("vlan 120"), command("end"),
                command("configure terminal"), command("ip http secure-server"), command("end"),
                command("show ip http server status"), finish());
        f.goal = "Create VLAN 120 and enable HTTPS Web management; verify both changes";
        f.outputs.put("ip http secure-server", "Switch(config)#");
        f.outputs.put("show ip http server status", "HTTP secure server status: Enabled\nSwitch#");
        fails(f);
        assertTrue(f.sent.contains("vlan 120"));
        assertTrue(f.sent.contains("ip http secure-server"));
        assertTrue(f.sent.contains("show ip http server status"));
    }

    @Test public void h3cInterfacePromptIsNotSystemConfigurationContext() {
        Fixture f = fixture(command("display version"), command("display current-configuration"),
                command("system-view"), command("interface GigabitEthernet1/0/1"), command("vlan 120"));
        f.outputs.put("display version", "H3C Comware Software, Version 7.1\n<H3C>");
        f.outputs.put("display current-configuration", "sysname H3C\n#\n<H3C>");
        f.outputs.put("system-view", "[H3C]");
        f.outputs.put("interface GigabitEthernet1/0/1", "[H3C-GigabitEthernet1/0/1]");
        fails(f);
        assertTrue(f.sent.contains("interface GigabitEthernet1/0/1"));
        assertFalse(f.sent.contains("vlan 120"));
    }

    private static Fixture fixture(String... actions) { return new Fixture(Arrays.asList(actions)); }
    private static Fixture webFixture(String... actions) {
        Fixture f = fixture(actions);
        f.goal = "Enable HTTPS Web management and create account netadmin";
        f.webAvailable = true;
        return f;
    }
    private static String web() {
        return "{\"type\":\"WEB_ACCESS\",\"message\":\"Enable requested Web account\","
                + "\"username\":\"netadmin\",\"https\":true,\"http\":false}";
    }
    private static String command(String value) { return action("COMMAND", value, "Perform the requested step"); }
    private static String ask(String value) { return action("ASK", null, value); }
    private static String finish() { return action("FINISH", null, "Task output collected"); }
    private static String action(String type, String command, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("message", message);
        if (command != null) json.addProperty("command", command);
        return json.toString();
    }
    private static String repeat(String value, int count) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < count; i++) out.append(value);
        return out.toString();
    }
    private static void fails(Fixture fixture) {
        try { fixture.run(); fail("Expected task to fail closed"); }
        catch (Exception expected) { assertNotNull(expected.getMessage()); }
    }

    private static final class Fixture implements AgentTaskEngine.Session, AgentTaskEngine.Listener {
        final List<String> actions;
        final List<String> sent = new ArrayList<>(), events = new ArrayList<>(), confirmations = new ArrayList<>();
        final List<List<AiChatMessage>> contexts = new ArrayList<>();
        final Map<String, String> outputs = new HashMap<>();
        final RequestCancellation cancel = new RequestCancellation();
        boolean valid = true, approve = true, gateAllowed = true, highGateAllowed = true, transportFailure, webAvailable;
        int modelCalls, cancelAtModelCall = -1, webCalls;
        Vendor webVendor;
        AgentAction webAction;
        String goal = "Create VLAN 120 with description Office and verify it";
        String answer = "VLAN 120";
        Runnable onConfirm = () -> { };
        Fixture(List<String> actions) {
            this.actions = actions;
            outputs.put("show version", VERSION);
            outputs.put("show vlan brief", "VLAN Name Status Ports\r\n1 default active\r\nSwitch#");
            outputs.put("show running-config", "hostname Switch\r\n!\r\nend\r\nSwitch#");
            outputs.put("show clock", "12:00:00 UTC Wed Oct 14 2026\r\nSwitch#");
            outputs.put("configure terminal", "Enter configuration commands, one per line.\r\nSwitch(config)#");
            outputs.put("vlan 120", "Switch(config-vlan)#");
            outputs.put("description Office", "Switch(config-vlan)#");
            outputs.put("end", "Switch#");
            outputs.put("show vlan 120", "VLAN Name Status Ports\r\n120 Office active\r\nSwitch#");
        }
        void run() throws Exception {
            new AgentTaskEngine(messages -> {
                contexts.add(new ArrayList<>(messages));
                modelCalls++;
                if (modelCalls == cancelAtModelCall) cancel.cancel();
                return modelCalls <= actions.size() ? actions.get(modelCalls - 1) : finish();
            }, this, this, high -> {
                if (!gateAllowed) throw new IllegalStateException("Target authorization expired");
                if (high && !highGateAllowed) throw new IllegalStateException("Protected target change not authorized");
            }, false).run(goal, cancel);
        }
        @Override public String target() { return "lab-switch"; }
        @Override public boolean valid() { return valid; }
        @Override public String execute(String command, RequestCancellation cancellation) throws Exception {
            sent.add(command);
            if (transportFailure) throw new IllegalStateException("Transport disconnected");
            return outputs.containsKey(command) ? outputs.get(command) : "Switch#";
        }
        @Override public void onEvent(String text) { events.add(text); }
        @Override public String ask(String question, RequestCancellation cancellation) { return answer; }
        @Override public boolean confirm(String target, String command, RequestCancellation cancellation) {
            assertEquals("lab-switch", target);
            confirmations.add(command);
            onConfirm.run();
            return approve;
        }
        @Override public String webAccess(AgentAction action, Vendor vendor, String platform, RequestCancellation cancellation) throws Exception {
            if (!webAvailable) return AgentTaskEngine.Listener.super.webAccess(action, vendor, platform, cancellation);
            webCalls++;
            webVendor = vendor;
            webAction = action;
            return "Local Web execution complete; verification pending";
        }
    }
}
