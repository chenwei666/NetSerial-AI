package com.chenwei666.netserial.remote;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;

final class RemoteTextDecoder {
    private final CharsetDecoder decoder;
    private byte[] pending = new byte[0];

    RemoteTextDecoder(Charset charset) {
        decoder = charset.newDecoder();
    }

    synchronized String decode(byte[] bytes) {
        byte[] combined = new byte[pending.length + bytes.length];
        System.arraycopy(pending, 0, combined, 0, pending.length);
        System.arraycopy(bytes, 0, combined, pending.length, bytes.length);
        ByteBuffer input = ByteBuffer.wrap(combined);
        CharBuffer output = CharBuffer.allocate(Math.max(16, combined.length * 2));
        CoderResult result = decoder.decode(input, output, false);
        if (result.isError()) {
            decoder.reset();
            pending = new byte[0];
            return new String(combined, decoder.charset());
        }
        pending = input.hasRemaining()
                ? Arrays.copyOfRange(combined, input.position(), combined.length)
                : new byte[0];
        output.flip();
        return output.toString();
    }
}
