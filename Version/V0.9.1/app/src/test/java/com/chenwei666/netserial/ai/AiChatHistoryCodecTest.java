package com.chenwei666.netserial.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.Arrays;

public class AiChatHistoryCodecTest {
    @Test public void roundTripsConversationAndEncryptedRepository() {
        AiChatConversation conversation = new AiChatConversation("chat-1", "故障分析", "core-1",
                1, 3, Arrays.asList(new AiChatMessage(AiChatRole.USER, "接口为什么丢包", 2),
                new AiChatMessage(AiChatRole.ASSISTANT, "先检查错误计数", 3)));
        AiChatHistoryState state = new AiChatHistoryState(Arrays.asList(conversation), "chat-1");
        AiChatHistoryCodec codec = new AiChatHistoryCodec();

        AiChatHistoryState decoded = codec.decode(codec.encode(state));

        assertEquals("故障分析", decoded.active().getTitle());
        assertEquals(2, decoded.active().getMessages().size());

        SecureCredentialVault vault = new SecureCredentialVault(
                new TestCredentialComponents.XorSecretCipher(),
                new TestCredentialComponents.InMemoryRecordStore());
        EncryptedAiChatHistoryRepository repository = new EncryptedAiChatHistoryRepository(vault);
        repository.save(state);
        assertEquals("chat-1", repository.load().getActiveConversationId());
        repository.clear();
        assertTrue(repository.load().getConversations().isEmpty());
    }

    @Test public void corruptedHistoryFailsClosed() {
        try {
            new AiChatHistoryCodec().decode("{\"version\":1,\"conversations\":[{}]}");
            fail("Expected invalid history");
        } catch (IllegalArgumentException expected) {
            assertFalse(expected.getMessage().isEmpty());
        }
    }

    @Test public void stateCapsConversationCount() {
        java.util.List<AiChatConversation> values = new java.util.ArrayList<>();
        for (int index = 0; index <= AiChatHistoryState.MAX_CONVERSATIONS; index++) {
            values.add(AiChatConversation.create("chat-" + index, "device", index + 1));
        }
        try {
            new AiChatHistoryState(values, null);
            fail("Expected conversation cap");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("many"));
        }
    }
}
