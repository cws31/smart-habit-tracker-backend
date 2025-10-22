package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.Certificate;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;

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
