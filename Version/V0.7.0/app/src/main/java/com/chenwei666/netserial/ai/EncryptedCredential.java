package com.chenwei666.netserial.ai;

import java.util.Arrays;

public final class EncryptedCredential {
    private final int version;
    private final byte[] initializationVector;
    private final byte[] ciphertext;

    public EncryptedCredential(int version, byte[] initializationVector, byte[] ciphertext) {
        if (version <= 0) {
            throw new IllegalArgumentException("version must be positive");
        }
        if (initializationVector == null || initializationVector.length == 0) {
            throw new IllegalArgumentException("initializationVector must not be empty");
        }
        if (ciphertext == null || ciphertext.length == 0) {
            throw new IllegalArgumentException("ciphertext must not be empty");
        }
        this.version = version;
        this.initializationVector = Arrays.copyOf(
                initializationVector,
                initializationVector.length
        );
        this.ciphertext = Arrays.copyOf(ciphertext, ciphertext.length);
    }

    public int getVersion() {
        return version;
    }

    public byte[] getInitializationVector() {
        return Arrays.copyOf(initializationVector, initializationVector.length);
    }

    public byte[] getCiphertext() {
        return Arrays.copyOf(ciphertext, ciphertext.length);
    }
}
