package com.smart_habit_tracker20.DTOs;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String email;
    private String password;
}
