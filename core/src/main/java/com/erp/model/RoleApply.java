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
public class RoleApply {
    private Long roleApplyId;
    private Long roleId;
    private Long flowTypeId;
    private String flowType;
    private String effectType;
    private Date effectFrom;
    private Date effectTo;
    private String status;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;

    public static interface TypeApply {
        public final String APPLY_API = "APPLY_API";
        public final String APPLY_USER = "APPLY_USER";
        public final String APPLY_BTN = "APPLY_BTN";
        public final String APPLY_FEATURE = "APPLY_FEATURE";
        public final String APPLY_SYSTEM = "APPLY_SYSTEM";
    }
}
