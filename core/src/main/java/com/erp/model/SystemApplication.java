package com.erp.model;


import lombok.*;


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
}
