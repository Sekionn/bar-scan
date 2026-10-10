package com.juuls_trinkets.bar_scan.events.queuestatus;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.juuls_trinkets.bar_scan.config.RabbitMqTopology;
import com.juuls_trinkets.bar_scan.dto.SyncBatchStartedEventDTO;
import com.juuls_trinkets.bar_scan.service.queuestatus.QueueStatusApplicationService;

import jakarta.validation.Valid;

@Component
public class SyncBatchStartedMessageListener {

    private final QueueStatusApplicationService queueStatusService;

    public SyncBatchStartedMessageListener(QueueStatusApplicationService queueStatusService) {
        this.queueStatusService = queueStatusService;
    }

    @RabbitListener(queues = RabbitMqTopology.SYNC_BATCH_STARTED_QUEUE)
    public void handle(@Valid SyncBatchStartedEventDTO event) {
        queueStatusService.registerExpectedMessages(event.userId(), event.syncBatchId(), event.expectedMessageIds());
    }
}
