package com.erp.authenservice.dto.response.roleapply;


import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleApplyDTO {
    private Long roleApplyId;
    private Long roleId;
    private Long flowTypeId;
    private String flowType;
    private String effectType;
    private String assign;
    private Date effectFrom;
    private Date effectTo;
    private String status;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;
}
