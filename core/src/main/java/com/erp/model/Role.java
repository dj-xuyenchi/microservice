package com.erp.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    private Long roleId;
    private String roleCode;
    private String roleName;
    private String roleDescription;
    private String effectType;
    private Date effectFrom;
    private Date effectTo;
    private String status;
    private Date createdAt;
    private String maker;
    private Date updatedAt;
    private String updatedBy;
}
