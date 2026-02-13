package com.erp.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Feature {
    private String featureId;
    private String featureCode;
    private String featureName;
    private Long parentId;
    private Long systemId;
    private Integer menuLevel;
    private String effectiveType;
    private Date effectiveFrom;
    private Date effectiveTo;
    private Date createdAt;
    private String maker;
    private String updatedBy;
    private Date updatedAt;
    private String status;

    public static interface FeatureStatus {
        public final String ACTIVE = "O";
        public final String LOCK = "C";
    }

    public static interface EffectiveRoleApplyType {
        public final String NO_EFFECTIVE = "NE";
        public final String EFFECTIVE = "E";
    }
}
