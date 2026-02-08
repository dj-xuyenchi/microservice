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
public class GlobalConfigData  {
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
