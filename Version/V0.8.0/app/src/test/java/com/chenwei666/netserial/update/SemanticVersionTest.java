package com.chenwei666.netserial.update;

import org.junit.Test;

import static org.junit.Assert.*;

public class SemanticVersionTest {
    @Test public void comparesVersionsNumerically() {
        assertTrue(SemanticVersion.parse("v0.6.0").compareTo(SemanticVersion.parse("0.5.9")) > 0);
        assertTrue(SemanticVersion.parse("1.10.0").compareTo(SemanticVersion.parse("1.9.9")) > 0);
        assertEquals(0, SemanticVersion.parse("v1.2").compareTo(SemanticVersion.parse("1.2.0")));
    }
}
