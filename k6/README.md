# Bar Scan k6 Load Tests

This project publishes `product.created` messages directly to RabbitMQ through the RabbitMQ management HTTP API.

It does not require building the backend or frontend. The backend must be running so the queue listener can consume the messages.
The commands below assume the terminal is open at the backend repository root.

## Prerequisites

- k6 installed and available as `k6`
- RabbitMQ management UI running on `http://localhost:15672`
- Backend running and listening to `bar-scan.product-created`
- An active `ROLE_USER` scanner account with an assigned shop (`USER_ID`)

The local Docker Compose setup already exposes RabbitMQ management on port `15672`.

## Run 1000 Messages

PowerShell:

```powershell
cd k6
$env:USER_ID="00000000-0000-0000-0000-000000000002"
k6 run product-created-queue.js
```

## Run App-Style Sync

This simulates the mobile app flow more closely:

1. Publish `sync-batch.started` messages to RabbitMQ with the expected message IDs.
2. Publish `product.created` messages to RabbitMQ.
3. Publish `sync-batch.publish-completed` messages to RabbitMQ with any message IDs that were not routed.
4. Poll RabbitMQ until the queue drains.

```powershell
cd k6
$env:USER_ID="00000000-0000-0000-0000-000000000002"
k6 run app-sync-product-created.js
```

## Verify Unassigned-User Message Rejection

This test publishes one `product.created` message for an existing user that has no shop assignment. It confirms the backend rejects the message without requeueing it. It does not purge messages and, by default, refuses to run until the product-created queue is empty.

```powershell
cd k6
$env:UNASSIGNED_USER_ID="USER-WITHOUT-A-SHOP-UUID"
k6 run reject-unassigned-user-product-created.js
```

## Useful Options

```powershell
$env:MESSAGES="1000"
$env:VUS="100"
$env:PURGE_BEFORE="true"
$env:WAIT_FOR_DRAIN="true"
$env:DRAIN_TIMEOUT_SECONDS="120"
$env:AMOUNT_COUNTED_MIN="1"
$env:AMOUNT_COUNTED_MAX="100"
$env:USER_ID="00000000-0000-0000-0000-000000000002"
k6 run product-created-queue.js
```

## Environment Variables

| Name | Default |
| --- | --- |
| `MESSAGES` | `1000` |
| `VUS` | `100` |
| `INSECURE_SKIP_TLS_VERIFY` | `true` |
| `RABBITMQ_MANAGEMENT_URL` | `http://localhost:15672` |
| `RABBITMQ_USERNAME` | `bar_scan` |
| `RABBITMQ_PASSWORD` | `bar_scan` |
| `RABBITMQ_VHOST` | `/` |
| `USER_ID` | `00000000-0000-0000-0000-000000000002` |
| `UNASSIGNED_USER_ID` | Required by `reject-unassigned-user-product-created.js` |
| `BARCODE_COUNTRY_PREFIX` | `57` |
| `STORE_SEGMENT_MIN` | `1` |
| `STORE_SEGMENT_MAX` | `99` |
| `SHELF_OF_ORIGIN` | `1` |
| `AMOUNT_COUNTED_MIN` | `1` |
| `AMOUNT_COUNTED_MAX` | `100` |
| `REPORT_CHUNK_SIZE` | `1000` |
| `PURGE_BEFORE` | `false` |
| `WAIT_FOR_DRAIN` | `true` |
| `DRAIN_TIMEOUT_SECONDS` | `120` |
| `DISCARD_TIMEOUT_SECONDS` | `30` |
| `REQUIRE_EMPTY_QUEUE` | `true` |

## Notes

- `VUS=100` means up to 100 virtual users publish the 1000 messages concurrently.
- For a harsher spike, set `VUS=1000`, but that also stress-tests RabbitMQ management HTTP and your local machine.
- Queue, exchange, and routing-key names are static app constants, not test-time configuration.
- Each message gets a random EAN-13 barcode with a valid check digit, a random two-digit store segment, and a random amount counted.
- `app-sync-product-created.js` intentionally uses one virtual user because the real mobile app publishes a sync batch sequentially.
- `WAIT_FOR_DRAIN=true` polls RabbitMQ after publishing and logs how long it took for the queue to reach zero messages.
- The chosen `USER_ID` must belong to an active `ROLE_USER` scanner account with an assigned shop. This is the only role that can use the mobile app, and the backend requires its shop assignment when creating scanned products.
- `reject-unassigned-user-product-created.js` requires an existing user with no shop assignment. It verifies the specific unassigned-shop message failure is discarded and does not test arbitrary invalid messages.
