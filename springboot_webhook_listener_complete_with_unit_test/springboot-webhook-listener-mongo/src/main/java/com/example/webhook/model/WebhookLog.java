package com.example.webhook.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "webhook_logs")
public class WebhookLog {
    @Id
    private String id;
    private String eventId;
    private String payload;
    private boolean verified;

    public WebhookLog() {}

    public WebhookLog(String eventId, String payload, boolean verified) {
        this.eventId = eventId;
        this.payload = payload;
        this.verified = verified;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
