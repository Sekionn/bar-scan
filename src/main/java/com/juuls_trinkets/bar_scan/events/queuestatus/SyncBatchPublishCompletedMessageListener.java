package com.juuls_trinkets.bar_scan.events.queuestatus;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.juuls_trinkets.bar_scan.dto.SyncBatchPublishCompletedEventDTO;
import com.juuls_trinkets.bar_scan.service.queuestatus.QueueStatusApplicationService;

import jakarta.validation.Valid;

@Component
public class SyncBatchPublishCompletedMessageListener {

    private final QueueStatusApplicationService queueStatusService;

    public SyncBatchPublishCompletedMessageListener(QueueStatusApplicationService queueStatusService) {
        this.queueStatusService = queueStatusService;
    }

    @RabbitListener(queues = "${app.messaging.sync-batch-publish-completed-queue}")
    public void handle(@Valid SyncBatchPublishCompletedEventDTO event) {
        queueStatusService.cancelUnpublishedMessages(event.userId(), event.syncBatchId(), event.unpublishedMessageIds());
    }
}
