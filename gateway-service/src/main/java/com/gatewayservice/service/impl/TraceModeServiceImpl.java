package com.gatewayservice.service.impl;

import com.erp.model.TraceMode;
import com.gatewayservice.service.ITraceModeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraceModeServiceImpl implements ITraceModeService {
    @Override
    public Mono<Void> logTrace(TraceMode traceMode) {
//        _traceModeRepo.save(traceMode);
        log.info("TRACE MODE: {}", traceMode);
        return Mono.empty();
    }

    @Override
    public Long updateTrace(TraceMode traceMode, String result) {
        try {
            traceMode.setUpdatedAt(new Date());
            traceMode.setResult(result);
//            _traceModeRepo.save(traceMode);
            return traceMode.getTraceModeId();
        } catch (Exception e) {
            log.error("//updateTrace -> {}", e.getMessage());
            return null;
        }
    }
}
