package com.gatewayservice.service.impl;

import com.gateway.entity.TraceMode;
import com.gateway.repository.ITraceModeRepo;
import com.gateway.service.ITraceModeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraceModeServiceImpl implements ITraceModeService {
    private final ITraceModeRepo _traceModeRepo;

    @Override
    public Long logTrace(TraceMode traceMode) {
        _traceModeRepo.save(traceMode);
        return traceMode.getTraceModeId();
    }

    @Override
    public Long updateTrace(TraceMode traceMode, String result) {
        try {
            traceMode.setUpdatedAt(new Date());
            traceMode.setResult(result);
            _traceModeRepo.save(traceMode);
            return traceMode.getTraceModeId();
        } catch (Exception e) {
            log.error("//updateTrace -> {}", e.getMessage());
            return null;
        }
    }
}
