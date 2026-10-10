package com.juuls_trinkets.bar_scan.config;

public final class RabbitMqTopology {

    public static final String PRODUCT_CREATED_EXCHANGE = "bar-scan.products";
    public static final String PRODUCT_CREATED_QUEUE = "bar-scan.product-created";
    public static final String PRODUCT_CREATED_ROUTING_KEY = "product.created";

    public static final String SYNC_BATCH_EXCHANGE = "bar-scan.sync-batches";
    public static final String SYNC_BATCH_STARTED_QUEUE = "bar-scan.sync-batch-started";
    public static final String SYNC_BATCH_STARTED_ROUTING_KEY = "sync-batch.started";
    public static final String SYNC_BATCH_PUBLISH_COMPLETED_QUEUE = "bar-scan.sync-batch-publish-completed";
    public static final String SYNC_BATCH_PUBLISH_COMPLETED_ROUTING_KEY = "sync-batch.publish-completed";

    public static final String SIGNUP_NOTIFICATION_EXCHANGE = "bar-scan.signup-notifications";
    public static final String SIGNUP_NOTIFICATION_QUEUE = "bar-scan.signup-notification-email";
    public static final String SIGNUP_NOTIFICATION_ROUTING_KEY = "signup.notification.email";

    private RabbitMqTopology() {
    }
}
