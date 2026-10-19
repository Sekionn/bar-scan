import encoding from 'k6/encoding';
import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Counter, Gauge, Trend } from 'k6/metrics';

const messages = numberEnv('MESSAGES', 10000);
const rabbitUrl = stringEnv('RABBITMQ_MANAGEMENT_URL', 'http://localhost:15672').replace(/\/$/, '');
const rabbitUser = stringEnv('RABBITMQ_USERNAME', 'bar_scan');
const rabbitPassword = stringEnv('RABBITMQ_PASSWORD', 'bar_scan');
const vhost = stringEnv('RABBITMQ_VHOST', '/');
const exchange = 'bar-scan.products';
const queue = 'bar-scan.product-created';
const routingKey = 'product.created';
const syncBatchExchange = 'bar-scan.sync-batches';
const syncBatchStartedQueue = 'bar-scan.sync-batch-started';
const syncBatchStartedRoutingKey = 'sync-batch.started';
const syncBatchPublishCompletedQueue = 'bar-scan.sync-batch-publish-completed';
const syncBatchPublishCompletedRoutingKey = 'sync-batch.publish-completed';
const userId = stringEnv('USER_ID', '00000000-0000-0000-0000-000000000002');
const barcodeCountryPrefix = stringEnv('BARCODE_COUNTRY_PREFIX', '57');
const storeSegmentMin = numberEnv('STORE_SEGMENT_MIN', 1);
const storeSegmentMax = numberEnv('STORE_SEGMENT_MAX', 99);
const shelfOfOrigin = numberEnv('SHELF_OF_ORIGIN', 1);
const amountCountedMin = numberEnv('AMOUNT_COUNTED_MIN', 1);
const amountCountedMax = numberEnv('AMOUNT_COUNTED_MAX', 100);
const purgeBefore = booleanEnv('PURGE_BEFORE', false);
const waitForDrain = booleanEnv('WAIT_FOR_DRAIN', true);
const drainTimeoutSeconds = numberEnv('DRAIN_TIMEOUT_SECONDS', 120);
const reportChunkSize = numberEnv('REPORT_CHUNK_SIZE', 1000);
const expectedMessageReportRequests = Math.ceil(messages / reportChunkSize);

const publishSuccess = new Counter('publish_success');
const publishRouted = new Counter('publish_routed');
const syncBatchStartedPublished = new Counter('sync_batch_started_published');
const syncBatchStartedRouted = new Counter('sync_batch_started_routed');
const syncBatchPublishCompletedPublished = new Counter('sync_batch_publish_completed_published');
const syncBatchPublishCompletedRouted = new Counter('sync_batch_publish_completed_routed');
const queueReady = new Gauge('queue_messages_ready');
const queueUnacknowledged = new Gauge('queue_messages_unacknowledged');
const queueTotal = new Gauge('queue_messages_total');
const publishDuration = new Trend('app_publish_duration_ms');
const reportDuration = new Trend('app_queue_status_report_duration_ms');
const drainDuration = new Trend('queue_drain_duration_ms');

export const options = {
  insecureSkipTLSVerify: booleanEnv('INSECURE_SKIP_TLS_VERIFY', true),
  scenarios: {
    app_sync_product_created_events: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: stringEnv('MAX_DURATION', '5m'),
    },
  },
  thresholds: {
    publish_success: [`count==${messages}`],
    publish_routed: [`count==${messages}`],
    sync_batch_started_published: [`count==${expectedMessageReportRequests}`],
    sync_batch_started_routed: [`count==${expectedMessageReportRequests}`],
  },
};

export function setup() {
  const rabbitHeaders = rabbitAuthHeaders();
  const overview = http.get(`${rabbitUrl}/api/overview`, { headers: rabbitHeaders });

  if (overview.status !== 200) {
    fail(`RabbitMQ management API is not reachable: ${overview.status} ${overview.body}`);
  }

  if (purgeBefore) {
    const purge = http.delete(queueUrl('/contents'), { headers: rabbitHeaders });
    if (purge.status !== 204) {
      fail(`Could not purge queue before test: ${purge.status} ${purge.body}`);
    }
  }

  return {
    rabbitHeaders,
  };
}

export default function (data) {
  const syncBatchId = randomUuid();
  const expectedMessageIds = Array.from({ length: messages }, () => randomUuid());
  const publishedMessageIds = [];
  const unpublishedMessageIds = [];

  const expectedReported = publishMessageIdChunks({
    headers: data.rabbitHeaders,
    queueName: syncBatchStartedQueue,
    exchangeName: syncBatchExchange,
    routingKey: syncBatchStartedRoutingKey,
    syncBatchId,
    messageIds: expectedMessageIds,
    buildPayload: (chunk) => ({
      messageId: randomUuid(),
      syncBatchId,
      userId,
      expectedMessageIds: chunk,
    }),
    publishedCounter: syncBatchStartedPublished,
    routedCounter: syncBatchStartedRouted,
  });

  if (!expectedReported) {
    fail('Could not report expected messages to backend before publishing.');
  }

  const startedPublishingAt = Date.now();

  for (const messageId of expectedMessageIds) {
    const response = publishProductCreated(data.rabbitHeaders, syncBatchId, messageId);
    const wasPublished = response.status === 200;
    const wasRouted = routed(response);

    check(response, {
      'publish returned 200': () => wasPublished,
      'message was routed': () => wasRouted,
    });

    if (wasPublished) {
      publishSuccess.add(1);
    }

    if (wasRouted) {
      publishRouted.add(1);
      publishedMessageIds.push(messageId);
    } else {
      unpublishedMessageIds.push(messageId);
    }
  }

  publishDuration.add(Date.now() - startedPublishingAt);

  const unpublishedReported = publishMessageIdChunks({
    headers: data.rabbitHeaders,
    queueName: syncBatchPublishCompletedQueue,
    exchangeName: syncBatchExchange,
    routingKey: syncBatchPublishCompletedRoutingKey,
    syncBatchId,
    messageIds: unpublishedMessageIds,
    buildPayload: (chunk) => ({
      messageId: randomUuid(),
      syncBatchId,
      userId,
      unpublishedMessageIds: chunk,
    }),
    publishedCounter: syncBatchPublishCompletedPublished,
    routedCounter: syncBatchPublishCompletedRouted,
  });

  console.log(`Reserved ${expectedMessageIds.length} messages, published ${publishedMessageIds.length}, canceled ${unpublishedMessageIds.length}.`);

  if (publishedMessageIds.length !== messages) {
    fail(`Only ${publishedMessageIds.length}/${messages} messages were routed.`);
  }

  if (!unpublishedReported) {
    fail('Could not report unpublished messages to backend.');
  }
}

function publishMessageIdChunks({
  headers,
  queueName,
  exchangeName,
  routingKey,
  messageIds,
  buildPayload,
  publishedCounter,
  routedCounter,
}) {
  if (messageIds.length === 0) {
    return true;
  }

  let allSucceeded = true;

  for (let start = 0; start < messageIds.length; start += reportChunkSize) {
    const chunk = messageIds.slice(start, start + reportChunkSize);
    const startedReportingAt = Date.now();
    const response = publishJson(headers, exchangeName, queueName, routingKey, buildPayload(chunk));
    reportDuration.add(Date.now() - startedReportingAt);

    const succeeded = check(response, {
      'sync batch message publish returned 200': (res) => res.status === 200,
      'sync batch message was routed': (res) => routed(res),
    });

    if (succeeded && routed(response)) {
      publishedCounter.add(1);
      routedCounter.add(1);
    } else {
      allSucceeded = false;
      console.error(`Could not report message chunk ${start}-${start + chunk.length}: ${response.status} ${response.body}`);
    }
  }

  return allSucceeded;
}

export function teardown(data) {
  const state = readQueue(data.rabbitHeaders);
  recordQueueState(state);

  if (!waitForDrain) {
    return;
  }

  const startedAt = Date.now();
  const deadline = startedAt + drainTimeoutSeconds * 1000;
  let current = state;

  while (Date.now() < deadline) {
    current = readQueue(data.rabbitHeaders);
    recordQueueState(current);

    if (current.total === 0) {
      drainDuration.add(Date.now() - startedAt);
      console.log(`Queue drained in ${Date.now() - startedAt} ms.`);
      return;
    }

    sleep(1);
  }

  drainDuration.add(Date.now() - startedAt);
  console.warn(`Queue did not drain within ${drainTimeoutSeconds}s. Remaining messages: ${current.total}.`);
}

function publishProductCreated(headers, syncBatchId, messageId) {
  const payload = {
    messageId,
    syncBatchId,
    userId,
    barcode: randomEan13(),
    shelfOfOrigin,
    storeSegment: randomStoreSegment(),
    amountCounted: randomInt(amountCountedMin, amountCountedMax),
  };

  return publishJson(headers, exchange, queue, routingKey, payload);
}

function publishJson(headers, exchangeName, queueName, routingKeyName, payload) {
  ensureQueueBinding(headers, exchangeName, queueName, routingKeyName);
  return http.post(
    `${rabbitUrl}/api/exchanges/${pathPart(vhost)}/${pathPart(exchangeName)}/publish`,
    JSON.stringify({
      properties: {
        content_type: 'application/json',
        delivery_mode: 2,
      },
      routing_key: routingKeyName,
      payload: JSON.stringify(payload),
      payload_encoding: 'string',
    }),
    { headers }
  );
}

function ensureQueueBinding(headers, exchangeName, queueName, routingKeyName) {
  http.put(
    `${rabbitUrl}/api/queues/${pathPart(vhost)}/${pathPart(queueName)}`,
    JSON.stringify({ durable: true }),
    { headers }
  );
  http.post(
    `${rabbitUrl}/api/bindings/${pathPart(vhost)}/e/${pathPart(exchangeName)}/q/${pathPart(queueName)}`,
    JSON.stringify({ routing_key: routingKeyName, arguments: {} }),
    { headers }
  );
}

function readQueue(headers) {
  const response = http.get(queueUrl(), { headers });

  if (response.status !== 200) {
    console.warn(`Could not read queue state: ${response.status} ${response.body}`);
    return { ready: 0, unacknowledged: 0, total: 0 };
  }

  const body = response.json();
  const ready = body.messages_ready || 0;
  const unacknowledged = body.messages_unacknowledged || 0;
  const total = body.messages || ready + unacknowledged;
  return { ready, unacknowledged, total };
}

function recordQueueState(state) {
  queueReady.add(state.ready);
  queueUnacknowledged.add(state.unacknowledged);
  queueTotal.add(state.total);
}

function routed(response) {
  if (response.status !== 200) {
    return false;
  }

  try {
    return response.json('routed') === true;
  } catch {
    return false;
  }
}

function randomEan13() {
  const prefix = digitsOnly(barcodeCountryPrefix).slice(0, 12);
  const bodyLength = 12 - prefix.length;
  const body = `${prefix}${randomDigits(bodyLength)}`;
  return `${body}${ean13CheckDigit(body)}`;
}

function randomStoreSegment() {
  return String(randomInt(storeSegmentMin, storeSegmentMax)).padStart(2, '0');
}

function randomDigits(length) {
  let value = '';
  for (let index = 0; index < length; index++) {
    value += randomInt(0, 9);
  }
  return value;
}

function ean13CheckDigit(firstTwelveDigits) {
  const sum = firstTwelveDigits
    .split('')
    .map((digit) => Number(digit))
    .reduce((total, digit, index) => total + digit * (index % 2 === 0 ? 1 : 3), 0);
  return String((10 - (sum % 10)) % 10);
}

function randomInt(min, max) {
  const lower = Math.min(min, max);
  const upper = Math.max(min, max);
  return Math.floor(Math.random() * (upper - lower + 1)) + lower;
}

function digitsOnly(value) {
  return value.replace(/\D/g, '');
}

function randomUuid() {
  const bytes = Array.from({ length: 16 }, () => randomInt(0, 255));
  bytes[6] = (bytes[6] & 0x0f) | 0x40;
  bytes[8] = (bytes[8] & 0x3f) | 0x80;
  const hex = bytes.map((byte) => byte.toString(16).padStart(2, '0')).join('');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function rabbitAuthHeaders() {
  const credentials = encoding.b64encode(`${rabbitUser}:${rabbitPassword}`);
  return {
    Authorization: `Basic ${credentials}`,
    'Content-Type': 'application/json',
  };
}

function queueUrl(suffix = '') {
  return `${rabbitUrl}/api/queues/${pathPart(vhost)}/${pathPart(queue)}${suffix}`;
}

function pathPart(value) {
  return encodeURIComponent(value);
}

function stringEnv(name, fallback) {
  return __ENV[name] && __ENV[name].trim() !== '' ? __ENV[name] : fallback;
}

function numberEnv(name, fallback) {
  const value = Number(__ENV[name]);
  return Number.isFinite(value) && value > 0 ? value : fallback;
}

function booleanEnv(name, fallback) {
  if (__ENV[name] === undefined) {
    return fallback;
  }

  return ['1', 'true', 'yes', 'on'].includes(__ENV[name].toLowerCase());
}
