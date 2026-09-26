# bar-scan

Monorepo for the Bar Scan backend and integration documentation.

## Structure

- `src/` - Spring Boot modular monolith backend.
- `docs/mobile-integration-context.md` - mobile/frontend integration contract.
- `docs/frontend-flow-context.md` - real frontend flow contract.
- `docs/backend-contact-signup-context.md` - contact-based signup requirements.
- `docker-compose.yml` - local MySQL and RabbitMQ stack, plus backend.

## Hosted Signup Flow

The hosted public signup flow is contact/manual-payment based:

1. Frontend calls `POST /signup/contact`.
2. Backend creates the company and first `ROLE_ADMIN` user.
3. Backend marks the customer as `CONTACT_REQUESTED` / `MANUAL_PAYMENT_REQUIRED`.
4. Backend stores a super-admin notification.
5. The super admin manually sets the customer's admin password.
6. The customer admin logs in with their email and assigned password.

The older `/signup/start` and `/payments/dev/**` flow is still available for local/dev payment experimentation and future payment-provider work.

## Access Model

- Companies own shops.
- Managerial users are company-scoped web users that can create shops and view/export company scan data.
- Shop admin users are shop-scoped users that can use phone and web flows and create scanner users.
- Scanner users are shop-scoped phone users.
- Product scan events are stored under the scanning user's shop and consolidated by shop/barcode.

## Local Run

Start infrastructure:

```powershell
.\scripts\setup-local-infra.cmd
```

Start backend:

```powershell
.\scripts\run-local.cmd
```
