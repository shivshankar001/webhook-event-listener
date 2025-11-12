package com.example.webhook.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StripeStyleSignatureVerifier {

    public static boolean verify(String payload, String signatureHeader, String secret, long toleranceSeconds) {
        if (signatureHeader == null || signatureHeader.isEmpty()) return false;
        try {
            String[] parts = signatureHeader.split(",");
            String tsPart = null;
            List<String> v1List = new ArrayList<>();
            for (String p : parts) {
                String trimmed = p.trim();
                if (trimmed.startsWith("t=")) tsPart = trimmed.substring(2);
                if (trimmed.startsWith("v1=")) v1List.add(trimmed.substring(3));
            }
            if (tsPart == null) return false;
            long ts = Long.parseLong(tsPart);
            long now = System.currentTimeMillis() / 1000L;
            if (Math.abs(now - ts) > toleranceSeconds) return false;
            String signed = ts + "." + payload;
            String computed = hmacSha256Hex(signed, secret);
            for (String v1 : v1List) {
                if (constantTimeEquals(computed, v1)) return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static String hmacSha256Hex(String payload, String secret) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] mac = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(2 * mac.length);
        for (byte b : mac) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
