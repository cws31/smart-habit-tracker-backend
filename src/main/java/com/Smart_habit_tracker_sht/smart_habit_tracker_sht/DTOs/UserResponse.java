package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import lombok.Data;

@Data
public class UserResponse {
    private String status;
    private String message;
    private Long id;
    private String name;
    private String email;

    public UserResponse() {
    }

    public UserResponse(String status, String message, Long id, String name, String email) {
        this.status = status;
        this.message = message;
        this.id = id;
        this.name = name;
        this.email = email;
    }

}