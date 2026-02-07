package com.erp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SystemRole {
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Date effectFrom;
    private Date effectTo;
    private String effectType;
    private String description;
}
