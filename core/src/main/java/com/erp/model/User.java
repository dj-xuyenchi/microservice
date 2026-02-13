package com.erp.model;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class User {
    private Long userId;
    private String username;
    private String password;
    private String status;

    public static interface UserStatus {
        public final String ACTIVE = "O";
        public final String LOCK = "C";
    }
}
