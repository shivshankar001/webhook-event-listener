# 🔔 Spring Boot Webhook Listener — MongoDB + Signature Verification + Unit Tests

## Overview

A minimal Spring Boot service that receives webhook POSTs, verifies a Stripe-style HMAC-SHA256 signature, and persists the raw payload to MongoDB. Includes unit tests for signature verification and controller behavior.

---

## Project contents

* `pom.xml` — Maven configuration
* `src/main/java/...` — Application code

  * `WebhookListenerApplication.java` — Spring Boot entry point
  * `controller/WebhookController.java` — `/webhook` endpoint
  * `model/WebhookLog.java` — Mongo document model
  * `repository/WebhookLogRepository.java` — Spring Data Mongo repository
  * `service/StripeStyleSignatureVerifier.java` — Signature verification utility
* `src/test/java/...` — JUnit tests

  * Controller and signature verifier tests + helpers
* `src/main/resources/application.properties` — config (port, Mongo URI, webhook secret)

---

## Quick start

### Prerequisites

* Java 17+
* Maven 3.8+
* MongoDB local or Atlas
* Postman or curl (for manual testing)
* IntelliJ IDEA (Community / Ultimate) for development

### Configure

Edit `src/main/resources/application.properties`:

```properties
server.port=8080
spring.data.mongodb.uri=mongodb://localhost:27017/webhook_db
webhook.secret=whsec_123456789
```

Replace `webhook.secret` with your chosen secret. If using MongoDB Atlas, set the `spring.data.mongodb.uri` accordingly.

### Build & Run (CLI)

```bash
mvn clean install
mvn spring-boot:run
```

App will start on `http://localhost:8080`.

---

## Endpoint

**POST** `/webhook`

* Headers: `Content-Type: application/json`, `Stripe-Signature: t=<timestamp>,v1=<signature>`
* Body: JSON payload (any webhook JSON)

On successful signature verification the controller will persist a `WebhookLog` document in MongoDB.

---

## Postman — step-by-step process (recommended)

This section shows how to simulate signed webhook requests in Postman using a Pre-request Script that calculates an HMAC-SHA256 signature (Stripe-style) and sets the `Stripe-Signature` header.

### 1. Create a new Request

* Method: POST
* URL: `http://localhost:8080/webhook`
* Headers: `Content-Type: application/json` (Postman will add this automatically when body type is JSON)
* Body → raw → JSON (example):

```json
{
  "event": "payment_succeeded",
  "amount": 1000
}
```

### 2. Add environment variables (optional but convenient)

Create an environment (e.g., `Local`) with variables:

* `webhook_secret` = `whsec_123456789`
* `payload` = leave empty (we'll set body normally)

### 3. Add a Pre-request Script to compute signature

Open the **Pre-request Script** tab and paste:

```javascript
// Postman Pre-request: compute Stripe-style HMAC-SHA256 signature
const secret = pm.environment.get('webhook_secret') || 'whsec_123456789';
const timestamp = Math.floor(Date.now() / 1000).toString();
const body = pm.request.body.raw || ''; // raw JSON body
const payload = `${timestamp}.${body}`;

// Compute HMAC SHA256
const CryptoJS = require('crypto-js');
const hash = CryptoJS.HmacSHA256(payload, secret).toString();
const signature = `t=${timestamp},v1=${hash}`;

pm.environment.set('computed_signature', signature);
pm.request.headers.add({key: 'Stripe-Signature', value: signature});
```

Notes:

* Postman's built-in `crypto-js` can be used in the Pre-request Script.
* The script takes the raw request body and the secret, computes `HMAC_SHA256(timestamp + '.' + body)`, and adds the `Stripe-Signature` header.

### 4. Send the request

Click **Send**. You should get a successful response (e.g., 200 OK) if the signature matches and the payload is stored.

### 5. Troubleshooting

* If you get `401` or verification failure, ensure:

  * The `webhook.secret` in `application.properties` matches the one used in Postman.
  * The request body in Postman matches exactly what the server receives (no extra whitespace differences matter for signature).
  * The timestamp is recent (some verifiers reject old timestamps to prevent replay attacks).

---

## Using curl to test (alternative)

Compute signature locally (example using openssl on Unix/macOS):

```bash
SECRET=whsec_123456789
PAYLOAD='{"event":"payment_succeeded","amount":1000}'
TS=$(date +%s)
DATA="$TS.$PAYLOAD"
SIG=$(printf "%s" "$DATA" | openssl dgst -sha256 -hmac "$SECRET" -binary | xxd -p -c 256)
HEADER="t=$TS,v1=$SIG"

curl -X POST http://localhost:8080/webhook \
  -H "Content-Type: application/json" \
  -H "Stripe-Signature: $HEADER" \
  -d "$PAYLOAD"
```

Note: on Windows you can use PowerShell with `Get-FileHash`/`HMACSHA256` or use WSL.

---

## IntelliJ — Importing, Running & Debugging

### 1. Import project into IntelliJ

* Open IntelliJ IDEA → **File → Open...** → select project folder (where `pom.xml` is).
* IntelliJ will detect a Maven project and import dependencies.

### 2. Set JDK

* Ensure Project SDK is Java 17+: **File → Project Structure → Project SDK** → select installed JDK.

### 3. Configure application properties (run-time overrides)

You can override `application.properties` values in run configuration or using an external file:

* **Run → Edit Configurations → + → Spring Boot** (or `Application`)

  * Name: `WebhookListenerLocal`
  * Main class: `com.example.webhook.WebhookListenerApplication`
  * Use classpath of module: select main module
  * VM options (optional): `-Dspring.profiles.active=local`
  * Program arguments (optional): `--spring.config.location=classpath:/application.properties,./config/application-local.properties`
  * Environment variables: `SPRING_DATA_MONGODB_URI` or `WEBHOOK_SECRET` if you prefer env override

### 4. Run

* Click the green **Run** button for the configuration. Console shows Spring Boot startup logs.

### 5. Debugging

* Open a Java class (e.g., `WebhookController`) and set breakpoints (click gutter).
* Run the Spring Boot configuration in **Debug** mode (bug icon).
* Send a Postman request; IntelliJ will pause at your breakpoint. Inspect variables and step through code.

### 6. Running tests

* Right-click `src/test/java` or a specific test class → **Run 'All Tests'** or use Maven goals:

```
mvn test
```

IntelliJ's test runner will show green/red results and stack traces for failures.

---

## Extending the project

* Replace the signature verifier with other providers (Razorpay/PayPal) by implementing their verification scheme.
* Add business logic (e.g., update DB records, publish to a message queue, call downstream services).
* Add Dockerfile and docker-compose with MongoDB for easier local dev.

---

## Useful commands

* Build: `mvn clean install`
* Run: `mvn spring-boot:run`
* Tests: `mvn test`

---

## License

Open-source; adapt as needed.

---

*If you'd like, I can also generate a ready-to-copy `postman_collection.json` snippet or a Docker Compose file for running MongoDB + the app locally.*
