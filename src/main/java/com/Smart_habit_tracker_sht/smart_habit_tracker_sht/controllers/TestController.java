package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/email")
    public String testEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("gautamrocky909621@gmail.com");
            message.setSubject("Test Email from Smart Habit Tracker");
            message.setText("This is a test email to verify email configuration is working.");

            mailSender.send(message);
            return "Test email sent successfully!";
        } catch (Exception e) {
            return "Failed to send test email: " + e.getMessage();
        }
    }
}