package com.erp.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GlobalConfig {
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
