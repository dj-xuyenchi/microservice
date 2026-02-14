package com.erp.model;

import lombok.*;

import java.util.Date;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatApi {
    private Long apiId;
    private String apiCode;
    private String apiName;
    private String apiDescription;
    private String uri;
    private Long systemId;
    private String method;
    private Date createdAt;
    private String maker;
    private Date updatedAt;
    private String updatedBy;
    private Boolean isWhiteEndPoint;
    private String status;
}
