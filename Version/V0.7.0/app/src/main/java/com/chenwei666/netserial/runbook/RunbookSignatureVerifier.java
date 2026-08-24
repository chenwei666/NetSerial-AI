package com.chenwei666.netserial.runbook;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.io.ByteArrayOutputStream;

/** Verifies externally signed team packs; the app never imports or stores private signing keys. */
public final class RunbookSignatureVerifier {
    public boolean verify(byte[] document, String signatureBase64, String publicKeyPem) {
        try {
            if (document == null || document.length == 0 || document.length > 1_000_000) return false;
            byte[] signature = decodeBase64(signatureBase64 == null ? "" : signatureBase64);
            String normalized = publicKeyPem == null ? "" : publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            PublicKey key = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(decodeBase64(normalized)));
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(key);
            verifier.update(document);
            return verifier.verify(signature);
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Small strict decoder keeps signature verification compatible with Android 5-7. */
    private static byte[] decodeBase64(String value) {
        String text = value == null ? "" : value.replaceAll("\\s", "");
        if (text.isEmpty() || (text.length() & 3) != 0) throw new IllegalArgumentException("Invalid Base64");
        ByteArrayOutputStream output = new ByteArrayOutputStream(text.length() * 3 / 4);
        for (int offset = 0; offset < text.length(); offset += 4) {
            int a = digit(text.charAt(offset));
            int b = digit(text.charAt(offset + 1));
            boolean cPadding = text.charAt(offset + 2) == '=';
            boolean dPadding = text.charAt(offset + 3) == '=';
            int c = cPadding ? -1 : digit(text.charAt(offset + 2));
            int d = dPadding ? -1 : digit(text.charAt(offset + 3));
            boolean last = offset + 4 == text.length();
            if (a < 0 || b < 0 || (!cPadding && c < 0) || (!dPadding && d < 0)
                    || (cPadding && !dPadding) || (!last && (cPadding || dPadding))) {
                throw new IllegalArgumentException("Invalid Base64");
            }
            output.write((a << 2) | (b >> 4));
            if (c >= 0) output.write(((b & 15) << 4) | (c >> 2));
            if (d >= 0) output.write(((c & 3) << 6) | d);
        }
        return output.toByteArray();
    }

    private static int digit(char value) {
        if (value >= 'A' && value <= 'Z') return value - 'A';
        if (value >= 'a' && value <= 'z') return value - 'a' + 26;
        if (value >= '0' && value <= '9') return value - '0' + 52;
        if (value == '+') return 62;
        if (value == '/') return 63;
        return -1;
    }
}
