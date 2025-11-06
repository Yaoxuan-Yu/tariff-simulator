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


Product endpoints:
GET /api/products → ProductRoutingController → product-service
GET /api/countries → ProductRoutingController → product-service
GET /api/brands?product=... → ProductRoutingController → product-service

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