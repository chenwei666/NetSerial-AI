package com.chenwei666.netserial.guided;

/** Stable identities for novice tasks; values are also used by internal navigation. */
public enum GuidedScenario {
    NO_INTERNET, SLOW_NETWORK, DNS_FAILURE, SWITCH_LOGIN,
    PORT_LINK, VLAN_GATEWAY, IDENTIFY_DEVICE, SWITCH_HEALTH,
    CONFIG_BACKUP, ASK_AI;

    public boolean isNetwork() {
        return this == NO_INTERNET || this == SLOW_NETWORK
                || this == DNS_FAILURE || this == SWITCH_LOGIN;
    }

    public static GuidedScenario fromId(String id) {
        if (id != null) {
            try { return valueOf(id); } catch (IllegalArgumentException ignored) { }
        }
        return NO_INTERNET;
    }
}
