package com.erp.model;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseRequest {
    private String ip;
    private String os;
    private String browser;
}
