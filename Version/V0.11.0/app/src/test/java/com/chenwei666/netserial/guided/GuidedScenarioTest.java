package com.chenwei666.netserial.guided;

import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.*;

public class GuidedScenarioTest {
    @Test public void everyScenarioRoundTripsThroughItsNavigationId() {
        for (GuidedScenario scenario : GuidedScenario.values()) {
            assertEquals(scenario, GuidedScenario.fromId(scenario.name()));
        }
    }

    @Test public void missingOrInvalidNavigationIdsHaveSafeKnownFallback() {
        for (String id : new String[]{null, "", "invalid", "PORT_LINK\n", "port_link", "PORT_LINK;reload"}) {
            assertEquals(GuidedScenario.NO_INTERNET, GuidedScenario.fromId(id));
        }
    }

    @Test public void onlyLocalNetworkCasesAreClassifiedAsNetworkChecks() {
        Set<GuidedScenario> network = EnumSet.of(GuidedScenario.NO_INTERNET, GuidedScenario.SLOW_NETWORK,
                GuidedScenario.DNS_FAILURE, GuidedScenario.SWITCH_LOGIN);
        for (GuidedScenario scenario : GuidedScenario.values()) {
            assertEquals(scenario.name(), network.contains(scenario), scenario.isNetwork());
        }
        assertEquals("Approved beginner scope", 10, GuidedScenario.values().length);
    }
}
