package com.gatewayservice.dto;

import lombok.*;

import java.util.Date;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoleUriDTO {
    private String apiUriId;
    private String roleCode;
    private String roleName;
    private String uri;
    private String applicationId;
    private String roleId;
    private String action;
    private String method;
    private String description;
    private String roleType;
    private Date roleFrom;
    private Date roleTo;
    private String applyType;
    private Date applyFrom;
    private Date applyTo;
}
