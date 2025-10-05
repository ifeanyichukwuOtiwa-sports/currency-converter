### Currency Converter API — README

A Spring Boot service that aggregates currency data from multiple providers (CurrencyAPI, Fixer, Open Exchange Rates) using a Chain of Responsibility. The service exposes simple endpoints to:
- List supported currencies
- Fetch latest exchange rates
- Fetch historical exchange rates for a specific date
- Convert between currencies

Only minimal data is returned by design (maps of codes → values or a small map with conversion result), keeping responses lightweight and consistent.

---

### Features
- Provider chain: tries CurrencyAPI → Fixer → OpenExchange automatically.
- Resilient: if a provider errors or has no data, the next provider is tried.
- Normalized responses:
    - `GET /currencies` → `Map<String,String>` of code → currency name
    - `GET /rates` → `Map<String,String>` of code → numeric rate (as string)
    - `GET /rate-history` → `Map<String,String>` of code → numeric historical rate (as string)
    - `POST /convert` → `{ from, to, result }`
- Server-side provider authentication. Your API keys are never exposed to clients.

---

### Requirements
- JDK 21+ (recommended by modern Spring Boot setups)
- Gradle 8.14.3+ (or use the included Maven Wrapper `./gradlew`)
- Network access to provider APIs (CurrencyAPI, Fixer, OpenExchange)

---

### Configuration
Add your API keys in `src/main/resources/application.yml` (or via environment variables). The service expects these properties under the `currency` prefix:

```yaml
currency:
  currencyAccessKey: YOUR_CURRENCYAPI_KEY        # for https://api.currencyapi.com
  fixerAccessKey: YOUR_FIXER_KEY                 # for https://data.fixer.io
  openExchangeAccessKey: YOUR_OPENEXCHANGE_APPID # for https://openexchangerates.org
server:
  port: 8080
```

Environment variable equivalents:
- `CURRENCY_CURRENCYACCESSKEY`
- `CURRENCY_FIXERACCESSKEY`
- `CURRENCY_OPENEXCHANGEACCESSKEY`

Example (Unix):
```bash
export CURRENCY_CURRENCYACCESSKEY=... \
       CURRENCY_FIXERACCESSKEY=... \
       CURRENCY_OPENEXCHANGEACCESSKEY=...
```

---

### Bootstrapping (Run Locally)
1) Clone the repository
    ```bash
    git clone 
    cd CurrencyConverter
    ```
2) Configure API keys
   - Update `src/main/resources/application.yml` as shown above, or export env vars.

3) Build and run (Gradle Wrapper)
   - Build the project:
    ```bash
    ./gradlew clean build
    ```
   - Run the application:
    ```bash
    ./gradlew bootRun
    ```
   - Or run the built jar:
    ```bash
    java -jar build/libs/*-SNAPSHOT.jar
    ```

    Notes:
    - On Windows, use `gradlew.bat` instead of `./gradlew`.

4) Verify the app started
   - Check logs for `Started ... in X seconds` and that it listens on your configured `server.port` (default `8080`).

---

### API Overview
Base path: `http://localhost:8080/api/v1/currency`

Endpoints:
- `GET /currencies` → Map of currency code → name
- `GET /rates?base=...&symbols=...` → Map of currency code → latest rate (string-encoded number)
- `GET /rate-history` (with JSON body) → Map of currency code → historical rate (string-encoded number)
- `POST /convert` (with JSON body) → `{ from, to, result }`

Provider order and behavior:
- The service tries providers in this order: CurrencyAPI → Fixer → OpenExchange
- If a provider throws or returns empty, the chain progresses to the next.
- If all providers fail or return empty, endpoints return a fallback message map or can be mapped by your controller to HTTP 417 (Expectation Failed) depending on your configuration.

---

### Request/Response Details

#### GET `/currencies`
Returns a `Map<String,String>` of currency codes to names.

Example response (truncated):
```json
{
  "USD": "United States Dollar",
  "EUR": "Euro",
  "GBP": "British Pound Sterling"
}
```

#### GET `/rates`
Query parameters:
- `base` (required): the base currency (e.g., `USD`, `EUR`). Depending on provider plan limitations, the base may be rebased client-side.
- `symbols` (required): comma-separated list (e.g., `USD,EUR,GBP`).

Response: `Map<String,String>` of code → latest rate.

Example request:
```
GET /api/v1/currency/rates?base=EUR&symbols=USD,GBP
```
Example response:
```json
{
  "USD": "1.08345",
  "GBP": "0.85521"
}
```

#### GET `/rate-history`
Note: This endpoint expects a JSON request body even though it’s a `GET`. Some HTTP clients and proxies may not support `GET` with a body; you can still use Postman or change to `POST` if needed.

Body schema (record `RateHistoryRequest`):
```json
{
  "date": "YYYY-MM-DD",  
  "base": "EUR",          
  "symbols": "USD,GBP"    
}
```
Response: `Map<String,String>` of code → historical rate.

Example response:
```json
{
  "USD": "1.12011",
  "GBP": "0.88234"
}
```

#### POST `/convert`
Body schema (record `ConvertRequest`):
```json
{
  "from": "USD",
  "to": "NGN",
  "amount": 100
}
```
Response: `Map<String,String>` with `from`, `to`, `result` (string-encoded number).

Example response:
```json
{
  "from": "USD",
  "to": "NGN",
  "result": "160000.25"
}
```

---

### Error Handling
- If a provider fails or returns no data, the chain tries the next provider automatically.
- If all providers fail, the controller currently returns a message map:
```json
{ "message": "service currently unavailable, please try again later.." }
```
- You can adapt the controller to return `417 Expectation Failed` (or another appropriate status) when the chain is exhausted.

---

### Testing with Postman

#### 1) Create a Postman environment
- Name: `CurrencyConverter Local`
- Variable: `baseUrl` → `http://localhost:8080`

Optionally add provider keys if you also test providers directly (not needed for calling this API):
- `currencyApiKey`, `fixerKey`, `openExchangeAppId` (only used for direct provider testing, not required for this service).

#### 2) Create a Postman collection `Currency Converter API`
Add the following requests (use `{{baseUrl}}`):

- Currencies
    - Method: `GET`
    - URL: `{{baseUrl}}/api/v1/currency/currencies`
    - Send. Expect a map of code → name.

- Latest Rates
    - Method: `GET`
    - URL: `{{baseUrl}}/api/v1/currency/rates`
    - Params: `base=EUR`, `symbols=USD,GBP,JPY`
    - Send. Expect a map of code → rate.

- Historical Rates
    - Method: `GET`
    - URL: `{{baseUrl}}/api/v1/currency/rate-history`
    - Headers: `Content-Type: application/json`
    - Body (raw JSON):
      ```json
      {
        "date": "2013-12-24",
        "base": "GBP",
        "symbols": "USD,EUR,CAD"
      }
      ```
    - Send. Expect a map of code → historical rate.

- Convert
    - Method: `POST`
    - URL: `{{baseUrl}}/api/v1/currency/convert`
    - Headers: `Content-Type: application/json`
    - Body (raw JSON):
      ```json
      {
        "from": "USD",
        "to": "NGN",
        "amount": 100
      }
      ```
    - Send. Expect `{ "from": "USD", "to": "NGN", "result": "..." }`.

Tips:
- If you get a fallback message, verify API keys in your `application.yml` or env vars.
- For `GET /rate-history`, if your tool blocks GET bodies, temporarily change to `POST` in your controller or use Postman as described.

---

### cURL Quickstart
Assuming `localhost:8080` and the app is running.

- Currencies
```bash
curl -s http://localhost:8080/api/v1/currency/currencies | jq
```

- Latest Rates
```bash
curl -s "http://localhost:8080/api/v1/currency/rates?base=EUR&symbols=USD,GBP" | jq
```

- Historical Rates
```bash
curl -s -X GET \
  -H "Content-Type: application/json" \
  -d '{"date":"2013-12-24","base":"GBP","symbols":"USD,EUR,CAD"}' \
  http://localhost:8080/api/v1/currency/rate-history | jq
```

- Convert
```bash
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"from":"USD","to":"NGN","amount":100}' \
  http://localhost:8080/api/v1/currency/convert | jq
```

---

### Project Structure (relevant parts)
- `CurrencyController` — REST endpoints under `/api/v1/currency`
- `CurrencyService` — facade that orchestrates provider chain calls
- `service/provider/chain/*Provider` — provider implementations (CurrencyAPI, Fixer, OpenExchange)
- `service/*Client` — HTTP clients for each upstream API
- `config/CurrencyConverterConfig` — Spring beans and configuration

---

### How the Provider Chain Works
- Each provider implements a common interface and returns `Optional<Map<String,String>>`.
- On any exception or empty data, the provider returns `Optional.empty()`.
- The `CurrencyService` iterates providers in order and returns the first successful result.
- This design is easily extensible: add a new provider bean with an `@Order` and no changes to the service.

---

### Notes & Limitations
- Base currency support may depend on provider plan (Fixer/OpenExchange). If a provider does not allow a custom base, the service may rebase locally (where possible) or try the next provider.
- Rate precision and weekend behavior follow provider documentation.
- `GET /rate-history` with a request body is supported by Postman but may be rejected by some proxies. If needed, change it to `POST` in your controller.