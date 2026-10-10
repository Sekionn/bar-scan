package com.juuls_trinkets.bar_scan.model;

public enum QueueMessageStatus {
    PENDING,
    PROCESSED,
    CANCELED;

    public static QueueMessageStatus fromDatabase(String value) {
        return QueueMessageStatus.valueOf(value);
    }

    public String toDatabase() {
        return name();
    }
}
