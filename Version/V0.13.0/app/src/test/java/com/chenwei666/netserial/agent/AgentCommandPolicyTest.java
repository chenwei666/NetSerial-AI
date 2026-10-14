package com.chenwei666.netserial.agent;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.CommandEvaluationRequest;
import com.chenwei666.netserial.safety.RiskLevel;
import com.chenwei666.netserial.safety.RuleBasedExecutionGuard;
import org.junit.Test;
import static org.junit.Assert.*;

public class AgentCommandPolicyTest {
    private final AgentCommandPolicy policy = new AgentCommandPolicy();
    @Test public void knownQueriesAndNavigationAutoExecute() {
        for (String command : new String[]{"display version", "show version", "display current-configuration",
                "show running-config", "show interfaces status", "display interface brief", "show vlan 4094",
                "display vlan 1", "show ip http server status", "display ip https", "show ip interface brief"}) {
            assertTrue(command, AgentCommandPolicy.isReadOnly(command));
            assertEquals(command, AgentCommandPolicy.Decision.AUTO, policy.evaluate(command));
        }
        for (String command : new String[]{"system-view", "configure terminal", "quit", "return", "end", "exit"}) {
            assertTrue(command, AgentCommandPolicy.isNavigation(command));
            assertEquals(command, AgentCommandPolicy.Decision.AUTO, policy.evaluate(command));
        }
    }
    @Test public void explicitRoutineVlanRulesAreBounded() {
        for (String command : new String[]{"vlan 1", "vlan 4094", "description Office_VLAN-100"})
            assertEquals(command, AgentCommandPolicy.Decision.AUTO, policy.evaluate(command));
        for (String command : new String[]{"vlan 0", "vlan 4095", "vlan 1 2", "vlan 1-4094", "description"})
            assertNotEquals(command, AgentCommandPolicy.Decision.AUTO, policy.evaluate(command));
    }
    @Test public void unknownCommandsAndSecurityNeedConfirmation() {
        for (String command : new String[]{"unknown command", "save", "write memory", "ip http enable",
                "ip https enable", "ip http secure-server", "local-user admin class manage", "aaa",
                "interface GigabitEthernet1/0/1", "port link-type access", "shutdown", "undo shutdown",
                "ip route 0.0.0.0 0.0.0.0 192.0.2.1", "reboot", "reload", "reset saved-configuration",
                "erase startup-config", "write erase"})
            assertEquals(command, AgentCommandPolicy.Decision.CONFIRM, policy.evaluate(command));
    }
    @Test public void existingHighAndCriticalRiskFloorNeverDowngraded() {
        for (String command : new String[]{"reboot", "reload", "reset saved-configuration", "erase startup-config",
                "write erase", "format flash:", "upgrade system", "bootrom update", "delete /unreserved flash:a",
                "shutdown", "undo interface GigabitEthernet1/0/1", "no interface Gi1/0/1", "aaa",
                "local-user admin", "radius-server host 192.0.2.1", "tacacs server local", "ip route 1.0.0.0",
                "ip route-static 1.0.0.0", "stp root primary", "undo stp", "no spanning-tree"}) {
            RiskLevel floor = RuleBasedExecutionGuard.createDefault().evaluate(new CommandEvaluationRequest(
                    Vendor.GENERIC, CliMode.UNKNOWN, command, RiskLevel.R0_INFORMATIONAL)).getEffectiveRisk();
            assertTrue(command, floor.ordinal() >= RiskLevel.R3_HIGH.ordinal());
            assertNotEquals(command, AgentCommandPolicy.Decision.AUTO, policy.evaluate(command));
        }
    }
    @Test public void rejectsInjectionControlsCredentialsAndInterpreters() {
        for (String command : new String[]{"show version\nreload", "show version\rreload", "show\tversion",
                "show version; reload", "show version | redirect flash:a", "show version > flash:a",
                "show version && reload", "show `reload`", "show $(reload)", "show version\u001b[A",
                "display version｜reboot", "do reload", "run bash", "shell", "python print(1)",
                "tclsh", "event manager applet bad", "copy running-config tftp", "delete flash:a",
                "username admin secret 0 synthetic", "password cipher synthetic", "snmp-server community synthetic",
                "sh ver", "dis ver", "sys", "conf t", "q"}) {
            assertEquals(command, AgentCommandPolicy.Decision.BLOCKED, policy.evaluate(command));
            assertFalse(command, AgentCommandPolicy.isReadOnly(command));
            assertFalse(command, AgentCommandPolicy.isNavigation(command));
        }
    }
    @Test public void doesNotTreatArbitraryShowPrefixAsSafe() {
        for (String command : new String[]{"show", "display", "show reload", "display shell", "show version extra",
                "show running-config delete", "display current-configuration save", "display vlan 0",
                "show vlan 4095", "show vlan 100000", "show version brief", "sho ver", "display ip https enable"}) {
            assertFalse(command, AgentCommandPolicy.isReadOnly(command));
            assertNotEquals(command, AgentCommandPolicy.Decision.AUTO, policy.evaluate(command));
        }
    }
    @Test public void rejectsEmptyOversizedAndNonAscii() {
        assertEquals(AgentCommandPolicy.Decision.BLOCKED, policy.evaluate(null));
        assertEquals(AgentCommandPolicy.Decision.BLOCKED, policy.evaluate(" "));
        assertEquals(AgentCommandPolicy.Decision.BLOCKED, policy.evaluate(new String(new char[513]).replace('\0', 'a')));
        assertEquals(AgentCommandPolicy.Decision.BLOCKED, policy.evaluate("description 中文"));
    }
}
