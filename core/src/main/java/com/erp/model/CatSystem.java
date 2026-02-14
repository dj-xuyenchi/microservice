package com.erp.model;


import lombok.*;

import java.util.Date;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatSystem {
    private Long systemId;
    private String systemCode;
    private String systemName;
    private String systemUriGateway;
    private String status;
    private Date createdAt;
    private String maker;
    private Date updatedAt;
    private String updatedBy;
}
