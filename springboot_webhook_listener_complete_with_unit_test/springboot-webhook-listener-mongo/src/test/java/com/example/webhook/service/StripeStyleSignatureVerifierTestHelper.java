package com.example.webhook.service;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class StripeStyleSignatureVerifierTestHelper {
    public static String computeHmacSHA256Hex(String data, String secret) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] mac = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(2 * mac.length);
        for (byte b : mac) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String buildSignatureHeader(String payload, String secret, long tsSeconds) throws Exception {
        String signedPayload = tsSeconds + "." + payload;
        String v1 = computeHmacSHA256Hex(signedPayload, secret);
        return "t=" + tsSeconds + ",v1=" + v1 + ",v1=invalid";
    }
}
