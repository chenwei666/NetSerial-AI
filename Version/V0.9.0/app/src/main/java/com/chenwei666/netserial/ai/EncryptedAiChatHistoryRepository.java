package com.chenwei666.netserial.ai;

import java.util.Arrays;
import java.util.Objects;

/** Stores the complete bounded history as one Android-Keystore-protected record. */
public final class EncryptedAiChatHistoryRepository implements AiChatHistoryRepository {
    private static final String HISTORY_ALIAS = "netserial-ai-chat-history-v1";
    private final CredentialVault vault;
    private final AiChatHistoryCodec codec;

    public EncryptedAiChatHistoryRepository(CredentialVault vault) {
        this(vault, new AiChatHistoryCodec());
    }

    EncryptedAiChatHistoryRepository(CredentialVault vault, AiChatHistoryCodec codec) {
        this.vault = Objects.requireNonNull(vault, "vault");
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override public AiChatHistoryState load() {
        if (!vault.contains(HISTORY_ALIAS)) return AiChatHistoryState.empty();
        return vault.withCredential(HISTORY_ALIAS, value -> codec.decode(new String(value)));
    }

    @Override public void save(AiChatHistoryState state) {
        char[] serialized = codec.encode(Objects.requireNonNull(state, "state")).toCharArray();
        try {
            vault.store(HISTORY_ALIAS, serialized);
        } finally {
            Arrays.fill(serialized, '\0');
        }
    }

    @Override public void clear() {
        if (vault.contains(HISTORY_ALIAS)) vault.delete(HISTORY_ALIAS);
    }
}
