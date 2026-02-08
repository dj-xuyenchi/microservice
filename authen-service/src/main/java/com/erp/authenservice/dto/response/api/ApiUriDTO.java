package com.erp.authenservice.dto.response.api;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiUriDTO {
    private Long apiUriId;
    private Long applicationId;
    private String applicationName;
    private String action;
    private String apiName;
    private String description;
    private String uri;
    private String method;
    private String isWhiteEndPoint;
    private String status;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;
}
