package com.chenwei666.netserial.ai;

@FunctionalInterface
public interface CredentialOperation<T> {
    T execute(char[] credential) throws Exception;
}
