package com.erp.authenservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Builder
public class GlobalConfigResponse {
    private Long globalConfigId;
    private String globalConfigName;
    private String globalConfigDescription;
    private String globalConfigCode;
    private String globalConfigValue;
    private String status;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;
}
