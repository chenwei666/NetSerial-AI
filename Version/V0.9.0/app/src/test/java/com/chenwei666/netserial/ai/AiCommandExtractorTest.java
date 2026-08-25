package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.safety.RiskLevel;

import org.junit.Test;

import java.util.List;

public class AiCommandExtractorTest {
    @Test public void extractsOnlyFencedCommandsAndReclassifiesRisk() {
        String response = "Run these only after review:\n```text\nshow version\nreload\n```\nDo not run show clock.";
        List<AiSuggestedCommand> commands = new AiCommandExtractor().extract(response,
                Vendor.CISCO_IOS, CliMode.USER_VIEW);
        assertEquals(2, commands.size());
        assertEquals(RiskLevel.R1_READ_ONLY, commands.get(0).getRisk());
        assertEquals(RiskLevel.R4_CRITICAL, commands.get(1).getRisk());
    }

    @Test public void excludesCredentialBearingCommand() {
        String response = "```text\nusername admin password secret-value\nshow clock\n```";
        List<AiSuggestedCommand> commands = new AiCommandExtractor().extract(response,
                Vendor.CISCO_IOS, CliMode.SYSTEM_VIEW);
        assertEquals(1, commands.size());
        assertEquals("show clock", commands.get(0).getCommand());
    }
}
