package com.smart_habit_tracker20.services;

import com.smart_habit_tracker20.models.Certificate;
import com.smart_habit_tracker20.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void sendCertificateNotification(User user, Certificate certificate) {

        log.info("📧 SENDING CERTIFICATE NOTIFICATION:");
        log.info("To: {}", user.getEmail());
        log.info("Subject: 🎉 Congratulations on Your Habit Achievement!");
        log.info("Message: {}", certificate.getCertificateMessage());
        log.info("Habit: {}", certificate.getHabit().getTitle());
        log.info("Streak: {} days", certificate.getStreakAchieved());

    }
}