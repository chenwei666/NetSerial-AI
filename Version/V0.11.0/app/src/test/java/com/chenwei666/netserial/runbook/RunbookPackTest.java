package com.chenwei666.netserial.runbook;

import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.Assert.*;

public class RunbookPackTest {
    @Test public void roundTripsAndVerifiesRsaSignature() throws Exception {
        RunbookPack pack = new RunbookPack("health-h3c", "1.0.0", "chenwei666",
                Arrays.asList("display cpu-usage", "display memory"));
        byte[] document = new RunbookPackCodec().encode(pack);
        assertEquals("health-h3c", new RunbookPackCodec().decode(document).getId());
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(pair.getPrivate());
        signer.update(document);
        String signature = Base64.getEncoder().encodeToString(signer.sign());
        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        assertTrue(new RunbookSignatureVerifier().verify(document, signature, publicKey));
        assertFalse(new RunbookSignatureVerifier().verify(document, "!!!!", publicKey));
        document[0] ^= 1;
        assertFalse(new RunbookSignatureVerifier().verify(document, signature, publicKey));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCredentialLikeCommands() {
        new RunbookPack("bad", "1", "tester", Arrays.asList("snmp-server community public ro"));
    }
}
