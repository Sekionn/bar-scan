package com.juuls_trinkets.bar_scan.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.dto.QueueStatusResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;
import com.juuls_trinkets.bar_scan.service.queuestatus.QueueStatusApplicationService;

@RestController
@RequestMapping("/queue-status")
public class QueueStatusController {

    private final QueueStatusApplicationService queueStatusService;

    public QueueStatusController(QueueStatusApplicationService queueStatusService) {
        this.queueStatusService = queueStatusService;
    }

    @GetMapping
    public QueueStatusResponseDTO getStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) UUID shopId
    ) {
        return queueStatusService.getStatus(user, shopId);
    }

    @GetMapping("/shops")
    public List<QueueStatusResponseDTO> getVisibleStatuses(@AuthenticationPrincipal CustomUserDetails user) {
        return queueStatusService.getVisibleStatuses(user);
    }
}
