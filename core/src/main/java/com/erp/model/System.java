package com.erp.model;


import lombok.*;

import java.util.Date;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class System {
    private Long systemId;
    private String systemCode;
    private String systemName;
    private String status;
    private Date createdAt;
    private String maker;
    private Date updatedAt;
    private String updatedBy;
}
