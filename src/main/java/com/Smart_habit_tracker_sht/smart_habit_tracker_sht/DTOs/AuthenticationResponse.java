package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private String token;
    private String tokenType = "Bearer";
    private String name;
    private String email;

    public AuthenticationResponse(String token, String tokenType, String name, String email) {
        this.token = token;
        this.tokenType = tokenType;
        this.name = name;
        this.email = email;
    }

}
