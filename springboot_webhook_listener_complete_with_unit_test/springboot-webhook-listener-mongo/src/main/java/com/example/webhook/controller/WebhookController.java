package com.example.webhook.controller;

import com.example.webhook.model.WebhookLog;
import com.example.webhook.repository.WebhookLogRepository;
import com.example.webhook.service.StripeStyleSignatureVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class WebhookController {

    private final WebhookLogRepository repo;

    @Value("${app.webhook.endpoint-secret:whsec_testsecret}")
    private String endpointSecret;

    public WebhookController(WebhookLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/health")
    public String health() { return "OK"; }

    @PostMapping("/webhook")
    public ResponseEntity<String> handle(@RequestHeader(value = "Stripe-Signature", required = false) String sig,
                                         @RequestBody String payload) {
        boolean verified = StripeStyleSignatureVerifier.verify(payload, sig, endpointSecret, 300);
        String eventId = extractEventId(payload);
        WebhookLog log = new WebhookLog(eventId, payload, verified);
        repo.save(log);
        if (verified) return ResponseEntity.ok("received");
        return ResponseEntity.badRequest().body("signature verification failed");
    }

    private String extractEventId(String payload) {
        if (payload == null) return null;
        Pattern p = Pattern.compile("\"id\"\s*:\s*\"([^\"]+)\"");
        Matcher m = p.matcher(payload);
        if (m.find()) return m.group(1);
        return null;
    }
}
