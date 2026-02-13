package com.erp.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Btn {
    private Long btnId;
    private String btnCode;
    private String btnName;
    private String btnDescription;
    private Long featureId;
    private String maker;
    private String updatedBy;
    private Date createdAt;
    private Date updatedAt;
    private String status;
}
