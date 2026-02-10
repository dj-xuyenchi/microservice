package com.erp.model;

import lombok.*;

import java.util.Date;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiUri {
    private Long apiUriId;
    private Long applicationId;
    private String action;
    private String apiName;
    private String description;
    private String uri;
    private String method;
    private Boolean isWhiteEndPoint;
    private String status;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;

}
