# Selenium Note

Selenium is not added to `bidmart-listing-query-service` because this repository is a backend Spring Boot service with no browser-rendered UI.

For BidMart, Selenium is more relevant in:

- `bidmart-frontend`
- an end-to-end test suite that launches frontend + gateway + services

Recommended Selenium/E2E scenarios for the frontend repo:

- seller login
- create listing
- buyer views listing
- buyer bids
- listing price/status updates
- invalid bid error handling

For this backend service, functional API testing is covered with Spring Boot + MockMvc.
