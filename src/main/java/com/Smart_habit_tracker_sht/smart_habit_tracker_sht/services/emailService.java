package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.HabitReminderDTO;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class emailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.sender.name:Smart Habit Tracker}")
    private String senderName;



    @Value("${frontend.url}")
    private String baseUrl;

    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            log.info(" Sending Thymeleaf welcome email to: {}", toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Smart Habit Tracker! 🚀");
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info(" Thymeleaf welcome email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error(" Failed to send Thymeleaf welcome email to {}: {}", toEmail, e.getMessage());
            sendSimpleWelcomeEmail(toEmail, name);
        }
    }

    private void sendSimpleWelcomeEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Smart Habit Tracker");
            message.setText(
                    "Hello " + name + "!\n\n" +
                            "Welcome to Smart Habit Tracker! We're excited to have you on board.\n\n" +
                            "Start building better habits today:\n" +
                            "• Track daily habits\n" +
                            "• Set personal challenges\n" +
                            "• Monitor your progress\n\n" +
                            "Login here: " + baseUrl + "\n\n" +
                            "Best regards,\n" +
                            "Smart Habit Tracker Team");

            mailSender.send(message);
            log.info(" Simple welcome email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error(" Failed to send simple welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendChallengeCompletedEmail(String toEmail, String name, String habitName, int streak,
            int challengeDays) {
        try {
            log.info(" Sending challenge completion email to: {}", toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject(" Challenge Completed: " + habitName);
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("habitName", habitName);
            context.setVariable("streak", streak);
            context.setVariable("challengeDays", challengeDays);
            context.setVariable("baseUrl", baseUrl);
            String htmlContent = templateEngine.process("challenge-completed-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info(" Challenge completion email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error(" Failed to send challenge completion email to {}: {}", toEmail, e.getMessage());
            sendSimpleChallengeCompletedEmail(toEmail, name, habitName, streak, challengeDays);
        }
    }

    private void sendSimpleChallengeCompletedEmail(String toEmail, String name, String habitName, int streak,
            int challengeDays) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(" Challenge Completed: " + habitName);
            message.setText(
                    "Congratulations, " + name + "! 🏆\n\n" +
                            "You've successfully completed your " + challengeDays + "-day challenge for \"" + habitName
                            + "\"!\n\n" +
                            "Your Achievement:\n" +
                            "• Total Days: " + streak + "\n" +
                            "• Challenge Target: " + challengeDays + "\n\n" +
                            "This is an incredible achievement! Your consistency and dedication have paid off. 🚀\n\n" +
                            "Ready for your next challenge? Log in to set new goals!\n\n" +
                            "Login here: " + baseUrl + "\n\n" +
                            "Best regards,\n" +
                            "Smart Habit Tracker Team");

            mailSender.send(message);
            log.info(" Simple challenge completion email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error(" Failed to send simple challenge completion email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendDailyReminderEmail(String toEmail, String name, List<HabitReminderDTO> pendingHabits) {
        try {
            log.info(" Sending daily reminder email to: {} with {} habits", toEmail, pendingHabits.size());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject(" Your Daily Habit Reminder - "
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("pendingHabits", pendingHabits);
            context.setVariable("totalHabits", pendingHabits.size());
            context.setVariable("pendingHabitsCount", pendingHabits.size());
            context.setVariable("completedHabitsCount", 0);
            context.setVariable("date", LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
            context.setVariable("baseUrl", baseUrl);
            String htmlContent = templateEngine.process("daily-reminder-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info(" Daily reminder email sent successfully to: {} with {} habits", toEmail, pendingHabits.size());

        } catch (Exception e) {
            log.error(" Failed to send daily reminder email to {}: {}", toEmail, e.getMessage());
            sendSimpleDailyReminderEmail(toEmail, name, pendingHabits);
        }
    }

    private void sendSimpleDailyReminderEmail(String toEmail, String name, List<HabitReminderDTO> pendingHabits) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(" Your Daily Habit Reminder");

            StringBuilder emailText = new StringBuilder();
            emailText.append("Hello ").append(name).append("!\n\n");
            emailText.append("Here are your habits for today:\n\n");

            if (pendingHabits.isEmpty()) {
                emailText.append(" All caught up! You have no pending habits for today.\n");
            } else {
                emailText.append("You have ").append(pendingHabits.size()).append(" habits to complete today:\n\n");

                for (int i = 0; i < pendingHabits.size(); i++) {
                    HabitReminderDTO habit = pendingHabits.get(i);
                    emailText.append(i + 1).append(". ").append(habit.getTitle()).append("\n");
                    if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
                        emailText.append("   Description: ").append(habit.getDescription()).append("\n");
                    }
                    emailText.append("   Challenge: ").append(habit.getChallengeDays()).append(" days | ");
                    emailText.append("Current Streak: ").append(habit.getCurrentStreak()).append(" days\n\n");
                }
            }

            emailText.append("\nDon't forget to track your progress!\n");
            emailText.append("Login here: ").append(baseUrl).append("\n\n");
            emailText.append("Best regards,\n");
            emailText.append("Smart Habit Tracker Team");

            message.setText(emailText.toString());
            mailSender.send(message);
            log.info(" Simple daily reminder email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error(" Failed to send simple daily reminder email to {}: {}", toEmail, e.getMessage());
        }
    }
}