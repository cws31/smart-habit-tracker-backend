package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String email;
    private String password;
}
