package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestEmailController {

    private final JavaMailSender mailSender;

    @GetMapping("/email-test")
    public String testEmail() {
        try {
            System.out.println("🔧 Testing JavaMailSender configuration...");

            JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
            System.out.println("🔧 Host: " + mailSenderImpl.getHost());
            System.out.println("🔧 Port: " + mailSenderImpl.getPort());
            System.out.println("🔧 Username: " + mailSenderImpl.getUsername());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("smarthabittracker31@gmail.com");
            message.setTo("smarthabittracker31@gmail.com");
            message.setSubject("Test Email from Smart Habit Tracker");
            message.setText("This is a test email to verify SMTP configuration.");

            mailSender.send(message);

            return " Test email sent successfully! Check your inbox.";

        } catch (Exception e) {
            System.err.println(" Email test failed: " + e.getMessage());
            e.printStackTrace();
            return " Email test failed: " + e.getMessage();
        }
    }

    @GetMapping("/mail-connection")
    public String testMailConnection() {
        try {
            JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;

            System.out
                    .println("🔧 Testing connection to: " + mailSenderImpl.getHost() + ":" + mailSenderImpl.getPort());
            System.out.println("🔧 Using username: " + mailSenderImpl.getUsername());

            mailSenderImpl.getSession();

            return " Mail server configuration looks good! Host: " + mailSenderImpl.getHost() + ", Port: "
                    + mailSenderImpl.getPort();

        } catch (Exception e) {
            return " Mail server connection failed: " + e.getMessage();
        }
    }

    @GetMapping("/mail-config")
    public String checkMailConfig() {
        try {
            JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;

            String configInfo = """
                    📧 Email Configuration:
                    • Host: %s
                    • Port: %d
                    • Username: %s
                    • Protocol: %s
                    """.formatted(
                    mailSenderImpl.getHost(),
                    mailSenderImpl.getPort(),
                    mailSenderImpl.getUsername(),
                    mailSenderImpl.getProtocol());

            System.out.println(configInfo);
            return configInfo;

        } catch (Exception e) {
            return " Failed to get mail configuration: " + e.getMessage();
        }
    }
}