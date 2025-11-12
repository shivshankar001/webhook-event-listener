package com.example.webhook.repository;

import com.example.webhook.model.WebhookLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookLogRepository extends MongoRepository<WebhookLog, String> {
    // additional query methods can be added here if needed
}
