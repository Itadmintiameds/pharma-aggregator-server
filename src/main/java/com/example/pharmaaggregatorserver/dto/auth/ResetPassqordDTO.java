package com.example.pharmaaggregatorserver.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPassqordDTO {
    private String username;
    private String currentPassword;
    private String newPassword;
}
