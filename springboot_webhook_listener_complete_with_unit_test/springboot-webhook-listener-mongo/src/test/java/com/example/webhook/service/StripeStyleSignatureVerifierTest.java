package com.example.webhook.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StripeStyleSignatureVerifierTest {

    @Test
    public void verify_validSignature_shouldReturnTrue() throws Exception {
        String secret = "whsec_testsecret";
        String payload = "{\"id\":\"evt_123\",\"type\":\"payment_intent.succeeded\"}";
        long ts = System.currentTimeMillis() / 1000L;
        String header = StripeStyleSignatureVerifierTestHelper.buildSignatureHeader(payload, secret, ts);
        boolean ok = StripeStyleSignatureVerifier.verify(payload, header, secret, 300);
        assertTrue(ok);
    }

    @Test
    public void verify_missingHeader_shouldReturnFalse() {
        String secret = "whsec_testsecret";
        String payload = "{\"id\":\"evt_123\"}";
        boolean ok = StripeStyleSignatureVerifier.verify(payload, null, secret, 300);
        assertFalse(ok);
    }
}
