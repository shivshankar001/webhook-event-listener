package com.example.webhook.controller;

import com.example.webhook.model.WebhookLog;
import com.example.webhook.repository.WebhookLogRepository;
import com.example.webhook.service.StripeStyleSignatureVerifierTestHelper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.example.webhook.controller.WebhookController.class)
public class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebhookLogRepository repo;

    @Value("${app.webhook.endpoint-secret:whsec_testsecret}")
    private String endpointSecret;

    @Test
    public void health_shouldReturnOK() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    public void postWebhook_validSignature_shouldReturn200_andSaveVerifiedLog() throws Exception {
        String payload = "{\"id\":\"evt_abc\",\"type\":\"payment_intent.succeeded\"}";
        long ts = System.currentTimeMillis() / 1000L;
        String header = StripeStyleSignatureVerifierTestHelper.buildSignatureHeader(payload, endpointSecret, ts);

        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Stripe-Signature", header))
                .andExpect(status().isOk())
                .andExpect(content().string("received"));

        ArgumentCaptor<WebhookLog> captor = ArgumentCaptor.forClass(WebhookLog.class);
        verify(repo, times(1)).save(captor.capture());
        WebhookLog saved = captor.getValue();
        assertThat(saved.isVerified()).isTrue();
        assertThat(saved.getEventId()).isEqualTo("evt_abc");
    }
}
