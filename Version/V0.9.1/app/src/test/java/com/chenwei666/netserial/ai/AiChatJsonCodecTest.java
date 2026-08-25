package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AiChatJsonCodecTest {
    private final AiChatJsonCodec codec = new AiChatJsonCodec();

    @Test public void encodesMultiTurnContextAndRedactsSecrets() {
        ProviderProfile profile = ProviderProfile.remote("zhipu", "https://open.bigmodel.cn/api/paas/v4",
                "glm-4", "zhipu-main");
        AiChatRequest request = new AiChatRequest(Arrays.asList(
                new AiChatMessage(AiChatRole.USER, "检查接口", 1),
                new AiChatMessage(AiChatRole.ASSISTANT, "请提供输出", 2),
                new AiChatMessage(AiChatRole.USER, "分析错误包", 3)),
                Vendor.H3C_COMWARE, CliMode.USER_VIEW, "core-1",
                "display interface\npassword=example-sensitive", "confirmed uplink", "Chinese");

        String json = new String(codec.encodeOpenAi(profile, request), StandardCharsets.UTF_8);

        assertTrue(json.contains("glm-4"));
        assertTrue(json.contains("检查接口"));
        assertTrue(json.contains("[REDACTED]"));
        assertFalse(json.contains("example-sensitive"));
        assertTrue(json.contains("untrusted reference data"));
    }

    @Test public void decodesOpenAiTextParts() {
        String response = "{\"choices\":[{\"message\":{\"content\":["
                + "{\"type\":\"text\",\"text\":\"检查结果\"},"
                + "{\"type\":\"text\",\"text\":\"```text\\nshow version\\n```\"}]}}]}";
        AiChatResponse decoded = codec.decodeOpenAi(response.getBytes(StandardCharsets.UTF_8));
        assertTrue(decoded.getContent().contains("show version"));
    }

    @Test public void decodesAnthropicContentAndRedactsCredential() {
        String response = "{\"content\":[{\"type\":\"text\",\"text\":"
                + "\"token=example-sensitive\\nUse SSH.\"}]}";
        AiChatResponse decoded = codec.decodeAnthropic(response.getBytes(StandardCharsets.UTF_8));
        assertEquals("token=[REDACTED]\nUse SSH.", decoded.getContent());
    }
}
