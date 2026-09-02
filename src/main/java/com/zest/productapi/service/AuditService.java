package com.zest.productapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditService {

    @Async("auditExecutor")
    public void logProductAction(String action, Long productId, String performedBy) {
        log.info("AUDIT | action={} | productId={} | performedBy={}", action, productId, performedBy);
    }
}
