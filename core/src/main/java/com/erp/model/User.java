package com.erp.model;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class User  {
    private Long userId;
    private String username;
    private String password;
}
