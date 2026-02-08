package com.erp.authenservice.dto.response;


import lombok.*;

import java.util.Date;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalConfigDataResponse {
    private Long globalConfigDataId;
    private Long globalConfigId;
    private String globalConfigDataName;
    private String globalConfigDataCode;
    private String globalConfigDataDescription;
    private String globalConfigDataValue;
    private String status;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;
}
