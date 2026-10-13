package com.chenwei666.netserial.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SnmpV3QueryPlan {
    private final String cidr;
    private final String securityName;
    private final String authenticationProtocol;
    private final String privacyProtocol;
    private final List<String> objectIdentifiers;

    SnmpV3QueryPlan(String cidr, String securityName, String authenticationProtocol,
                    String privacyProtocol, List<String> objectIdentifiers) {
        this.cidr = cidr;
        this.securityName = securityName;
        this.authenticationProtocol = authenticationProtocol;
        this.privacyProtocol = privacyProtocol;
        this.objectIdentifiers = Collections.unmodifiableList(new ArrayList<>(objectIdentifiers));
    }
    public String getCidr() { return cidr; }
    public String getSecurityName() { return securityName; }
    public String getAuthenticationProtocol() { return authenticationProtocol; }
    public String getPrivacyProtocol() { return privacyProtocol; }
    public List<String> getObjectIdentifiers() { return objectIdentifiers; }
    public boolean storesSecrets() { return false; }
}
