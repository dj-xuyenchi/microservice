package com.erp.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TraceMode {
    private Long traceModeId;
    private String traceCode;
    private Long userId;
    private String userName;
    private String action;
    private Date createdAt;
    private Date updatedAt;
    private String objectParams;
    private String rolesActionMoment;
    private String method;
    private Long systemId;
    private String result;
    private String ip;
    private String os;
    private String browser;
    private String sessionId;
    private String uri;
    private Long apiId;
    private Boolean isError;
    private Long milliTimeCost;
}
