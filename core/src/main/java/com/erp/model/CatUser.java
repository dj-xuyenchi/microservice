package com.erp.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CatUser {
    private Long userId;
    private String username;
    private String password;
    private String lastName;
    private String firstName;
    private String email;
    private String phoneNumber;
    private String maker;
    private Date createdAt;
    private String updatedAt;
    private String updatedBy;
    private String status;

    public static interface UserStatus {
        public final String ACTIVE = "O";
        public final String LOCK = "C";
    }
}
