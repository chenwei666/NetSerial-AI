package com.chenwei666.netserial.network;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NetworkTargetValidatorTest {
    @Test public void acceptsSingleTargetAndPort() {
        NetworkTargetValidator validator = new NetworkTargetValidator();
        assertEquals("switch01.example.com", validator.validate(" switch01.example.com "));
        assertEquals(22, validator.validatePort("22"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsShellLikeTarget() {
        new NetworkTargetValidator().validate("switch;reboot");
    }
}
