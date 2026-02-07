package com.gatewayservice.service;

import com.erp.model.TraceMode;

public interface ITraceModeService {
    Long logTrace(TraceMode traceMode);

    Long updateTrace(TraceMode trace, String result);
}
