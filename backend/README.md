The backend is split into the following microservices:

- api-gateway (Port 8080) - API Gateway with authentication
- tariff-calculator (Port 8081) - Tariff calculation service
- session-management (Port 8082) - Session history management
- global-tariffs (Port 8083) - Global tariff definitions
- product-service (Port 8084) - Product catalog service
- csv-export (Port 8085) - CSV export functionality
- simulator-tariffs (Port 8086) - Simulator tariff definitions
- wits-api-integration (Port 8087) - WITS API integration

Commands to run backend: (remember to install and launch docker first)

cd backend
docker-compose up -d --build

or run every single microservice manually using:

mvnw spring-boot:run

## Running Tests

### Quick Start
```bash
# Option 1: Use the test script (RECOMMENDED)
cd backend
run-all-tests.bat          # Windows
./run-all-tests.sh         # Linux/Mac

# Option 2: Try Maven from root (may not work if modules not configured)
cd backend
mvn test

# Option 3: Run tests for a specific service
cd backend/tariff-calculator
mvn test

# Run only unit tests (fast, no Docker needed)
cd backend/<service-name>
mvn test -Dtest="*UnitTest"

# Run only integration tests (requires Docker)
cd backend/<service-name>
mvn test -Dtest="*IntegrationTest"
```

**Prerequisites**: Docker must be running (required for TestContainers integration tests)

See [TESTING_GUIDE.md](./TESTING_GUIDE.md) for detailed testing instructions.


Product endpoints:
GET /api/products → ProductRoutingController → product-service
GET /api/countries → ProductRoutingController → product-service

Tariff calculation:
GET /api/tariff?params... → TariffRoutingController → tariff-calculator

Tariff definitions:
GET /api/tariff-definitions/global → TariffRoutingController → global-tariffs
GET /api/tariff-definitions/modified → TariffRoutingController → global-tariffs
GET /api/tariff-definitions/user → TariffRoutingController → simulator-tariffs
POST /api/tariff-definitions/user → TariffRoutingController → simulator-tariffs
POST /api/tariff-definitions/modified → TariffRoutingController → global-tariffs
PUT /api/tariff-definitions/user/{id} → TariffRoutingController → simulator-tariffs
PUT /api/tariff-definitions/modified/{id} → TariffRoutingController → global-tariffs
DELETE /api/tariff-definitions/user/{id} → TariffRoutingController → simulator-tariffs
DELETE /api/tariff-definitions/modified/{id} → TariffRoutingController → global-tariffs
GET /api/tariff-definitions/export → TariffRoutingController → global-tariffs

Export cart:
GET /api/export-cart → ExportCartRoutingController → csv-export
POST /api/export-cart/add/{calculationId} → ExportCartRoutingController → csv-export
DELETE /api/export-cart/remove/{calculationId} → ExportCartRoutingController → csv-export
DELETE /api/export-cart/clear → ExportCartRoutingController → csv-export
GET /api/export-cart/export → ExportCartRoutingController → csv-export

Session history:
GET /api/tariff/history → SessionRoutingController → session-management
POST /api/tariff/history/save → SessionRoutingController → session-management (added)
DELETE /api/tariff/history/clear → SessionRoutingController → session-management



to add admin role using supabase:
curl -X PATCH 'https://eommtatuhdmnkakktghz.supabase.co/auth/v1/admin/users/195452f5-450d-4e67-a655-8db4da641550'   -H "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVvbW10YXR1aGRtbmtha2t0Z2h6Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1ODI5NjEzMCwiZXhwIjoyMDczODcyMTMwfQ.c9xhPA5Hfm9aXdfPhn20mzdlNcH3olXaiJ5o8EoydS8"   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVvbW10YXR1aGRtbmtha2t0Z2h6Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1ODI5NjEzMCwiZXhwIjoyMDczODcyMTMwfQ.c9xhPA5Hfm9aXdfPhn20mzdlNcH3olXaiJ5o8EoydS8"   -H "Content-Type: application/json"   -d '{
    "app_metadata": {
      "role": "admin"
    }
  }'

  or 

UPDATE auth.users
SET raw_app_meta_data = 
  jsonb_set(
    COALESCE(raw_app_meta_data, '{}'::jsonb),
    '{role}',
    '"admin"'
  )
WHERE email = 'kirthivasshni@gmail.com';

1. ApiGatewayToTariffCalculatorIntegrationTest
Tests API Gateway forwarding requests to Tariff Calculator
Verifies request parameters are passed correctly
Tests error propagation

2. TariffCalculatorToSessionManagementIntegrationTest
Tests Tariff Calculator calling Session Management to save calculations
Verifies session ID is passed correctly
Tests graceful error handling when Session Management fails

3. CsvExportToSessionManagementIntegrationTest
Tests CSV Export retrieving calculations from Session Management
Verifies service-to-service data retrieval

4. ProductServiceToGlobalTariffsIntegrationTest
Tests Product Service fetching countries/partners from Global Tariffs
Verifies data parsing and error handling

5. EndToEndTariffCalculationFlowIntegrationTest
Tests complete flow: API Gateway → Tariff Calculator → Session Management
Verifies the entire request chain works correctly


Unit tests created: 
Service unit tests (business logic)
1. TariffServiceUnitTest (tariff-calculator)
Tests tariff calculation logic
Tests FTA vs MFN rate selection
Tests currency conversion
Tests validation and error handling

2. SessionHistoryServiceUnitTest (session-management)
Tests saving calculations to session
Tests history retrieval
Tests history limits (100 items)
Tests Redis integration

3. ExportCartServiceUnitTest (csv-export)
Tests adding/removing items from cart
Tests duplicate prevention
Tests error handling

4. TariffServiceUnitTest (global-tariffs)
Tests tariff definition CRUD operations
Tests validation
Tests FTA logic

5. ProductServiceUnitTest (product-service)
Tests product/country/partner retrieval
Tests error handling

6. CurrencyServiceUnitTest (tariff-calculator)
Tests currency conversion
Tests exchange rate retrieval

7. TariffComparisonServiceUnitTest (tariff-calculator)
Tests multi-country comparison
Tests tariff history generation
Tests trends calculation

8. SessionTariffServiceUnitTest (simulator-tariffs)
Tests session-based tariff management
Tests CRUD operations

9. TradeInsightsServiceUnitTest (trade-insights)
Tests aggregation of news and agreements
Tests error 

10. NewsServiceUnitTest (trade-insights)
Tests news search functionality

11. AgreementServiceUnitTest (trade-insights)
Tests agreement search functionality

12. CsvExportServiceUnitTest (csv-export)
Tests CSV generation
Tests CSV escaping

13. RoutingServiceUnitTest (api-gateway)
Tests URL building
Tests request forwarding
Tests header/cookie forwarding

14. Controller unit tests (HTTP layer)
TariffCalculationControllerUnitTest
GlobalTariffControllerUnitTest
ProductControllerUnitTest
SessionHistoryControllerUnitTest
ExportCartControllerUnitTest
SimulatorTariffControllerUnitTest
TradeInsightsControllerUnitTest
NewsControllerUnitTest
AgreementControllerUnitTest

Test characteristics
All dependencies mocked (repositories, services, clients)
Fast execution (no databases, no network calls)
Tests business logic, validation, and error handling
Edge cases covered (null values, empty lists, invalid inputs)
Uses Mockito for mocking
Uses JUnit 5 for assertions