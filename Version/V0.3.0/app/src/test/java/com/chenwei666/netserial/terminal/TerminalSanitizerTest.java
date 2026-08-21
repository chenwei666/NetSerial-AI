package com.chenwei666.netserial.terminal;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TerminalSanitizerTest {
    @Test
    public void stripsAnsiButKeepsReadableOutput() {
        String result = new AnsiTextSanitizer().sanitize("\u001B[31mERROR\u001B[0m\nnext");
        assertTrue(result.contains("ERROR\nnext"));
        assertFalse(result.contains("\u001B"));
    }

    @Test
    public void redactsCommonCredentials() {
        SensitiveTextRedactor redactor = new SensitiveTextRedactor();
        String result = redactor.redact("password: demo123 token=abcd1234 Bearer abcdefghijk");
        assertFalse(result.contains("demo123"));
        assertFalse(result.contains("abcd1234"));
        assertFalse(result.contains("abcdefghijk"));
    }
}
