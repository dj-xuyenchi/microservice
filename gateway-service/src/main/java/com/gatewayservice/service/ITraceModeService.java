package com.gatewayservice.service;


import com.gateway.entity.TraceMode;

public interface ITraceModeService {
    Long logTrace(TraceMode traceMode);

    Long updateTrace(TraceMode trace, String result);
}
