import encoding from 'k6/encoding';
import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Counter, Gauge, Trend } from 'k6/metrics';

const rabbitUrl = stringEnv('RABBITMQ_MANAGEMENT_URL', 'http://localhost:15672').replace(/\/$/, '');
const rabbitUser = stringEnv('RABBITMQ_USERNAME', 'bar_scan');
const rabbitPassword = stringEnv('RABBITMQ_PASSWORD', 'bar_scan');
const vhost = stringEnv('RABBITMQ_VHOST', '/');
const unassignedUserId = requiredEnv('UNASSIGNED_USER_ID');
const discardTimeoutSeconds = numberEnv('DISCARD_TIMEOUT_SECONDS', 30);
const requireEmptyQueue = booleanEnv('REQUIRE_EMPTY_QUEUE', true);
const exchange = 'bar-scan.products';
const queue = 'bar-scan.product-created';
const routingKey = 'product.created';

const poisonMessagePublished = new Counter('poison_message_published');
const poisonMessageRouted = new Counter('poison_message_routed');
const poisonMessageDiscarded = new Counter('poison_message_discarded');
const queueMessages = new Gauge('queue_messages');
const discardDuration = new Trend('poison_message_discard_duration_ms');

export const options = {
  insecureSkipTLSVerify: booleanEnv('INSECURE_SKIP_TLS_VERIFY', true),
  scenarios: {
    reject_unassigned_user_product_created_event: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: stringEnv('MAX_DURATION', '1m'),
    },
  },
  thresholds: {
    poison_message_published: ['count==1'],
    poison_message_routed: ['count==1'],
    poison_message_discarded: ['count==1'],
  },
};

export function setup() {
  const headers = rabbitAuthHeaders();
  const overview = http.get(`${rabbitUrl}/api/overview`, { headers });
  if (overview.status !== 200) {
    fail(`RabbitMQ management API is not reachable: ${overview.status} ${overview.body}`);
  }

  const state = readQueue(headers);
  if (requireEmptyQueue && state.total !== 0) {
    fail(
      `Refusing to run because ${queue} contains ${state.total} message(s). ` +
        'Wait for normal processing to finish or set REQUIRE_EMPTY_QUEUE=false in an isolated environment.'
    );
  }

  return { headers };
}

export default function (data) {
  const response = publishJson(data.headers, {
    messageId: randomUuid(),
    syncBatchId: randomUuid(),
    userId: unassignedUserId,
    barcode: '5713428006725',
    shelfOfOrigin: 1,
    storeSegment: '01',
    amountCounted: 1,
  });

  const succeeded = check(response, {
    'poison message publish returned 200': (res) => res.status === 200,
    'poison message was routed': (res) => routed(res),
  });

  if (!succeeded) {
    fail(`Could not publish the unassigned-user test message: ${response.status} ${response.body}`);
  }

  poisonMessagePublished.add(1);
  poisonMessageRouted.add(1);
}

export function teardown(data) {
  const startedAt = Date.now();
  const deadline = startedAt + discardTimeoutSeconds * 1000;

  while (Date.now() < deadline) {
    const state = readQueue(data.headers);
    queueMessages.add(state.total);

    if (state.total === 0) {
      poisonMessageDiscarded.add(1);
      discardDuration.add(Date.now() - startedAt);
      console.log(`Unassigned-user message was discarded in ${Date.now() - startedAt} ms.`);
      return;
    }

    sleep(1);
  }

  throw new Error(
    `Unassigned-user message was not discarded within ${discardTimeoutSeconds}s. ` +
      `${queue} still contains messages.`
  );
}

function publishJson(headers, payload) {
  return http.post(
    `${rabbitUrl}/api/exchanges/${pathPart(vhost)}/${pathPart(exchange)}/publish`,
    JSON.stringify({
      properties: {
        content_type: 'application/json',
        delivery_mode: 2,
      },
      routing_key: routingKey,
      payload: JSON.stringify(payload),
      payload_encoding: 'string',
    }),
    { headers }
  );
}

function readQueue(headers) {
  const response = http.get(
    `${rabbitUrl}/api/queues/${pathPart(vhost)}/${pathPart(queue)}`,
    { headers }
  );

  if (response.status !== 200) {
    fail(`Could not read queue state: ${response.status} ${response.body}`);
  }

  const body = response.json();
  const ready = body.messages_ready || 0;
  const unacknowledged = body.messages_unacknowledged || 0;
  return { total: body.messages || ready + unacknowledged };
}

function routed(response) {
  try {
    return response.json('routed') === true;
  } catch {
    return false;
  }
}

function rabbitAuthHeaders() {
  const credentials = encoding.b64encode(`${rabbitUser}:${rabbitPassword}`);
  return {
    Authorization: `Basic ${credentials}`,
    'Content-Type': 'application/json',
  };
}

function pathPart(value) {
  return encodeURIComponent(value);
}

function randomUuid() {
  const bytes = Array.from({ length: 16 }, () => randomInt(0, 255));
  bytes[6] = (bytes[6] & 0x0f) | 0x40;
  bytes[8] = (bytes[8] & 0x3f) | 0x80;
  const hex = bytes.map((byte) => byte.toString(16).padStart(2, '0')).join('');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function requiredEnv(name) {
  const value = __ENV[name];
  if (!value || value.trim() === '') {
    throw new Error(`${name} must be set to an existing user ID without a shop assignment.`);
  }
  return value;
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
