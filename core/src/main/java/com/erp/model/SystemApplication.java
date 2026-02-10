package com.erp.model;


import lombok.*;

import java.util.Date;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemApplication {
    private Long applicationId;
    private String applicationName;
    private String applicationIcon;
    private String serviceUriGateway;
    private String status;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;
}
