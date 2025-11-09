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