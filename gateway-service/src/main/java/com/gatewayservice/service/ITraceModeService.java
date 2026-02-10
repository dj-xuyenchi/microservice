package com.gatewayservice.service;

import com.erp.model.TraceMode;
import reactor.core.publisher.Mono;

public interface ITraceModeService {
    Mono<Void> logTrace(TraceMode traceMode);

    Long updateTrace(TraceMode trace, String result);
}
