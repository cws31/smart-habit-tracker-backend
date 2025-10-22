package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.CertificateDTO;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs.StreakCalculationDTO;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.Certificate;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.Habit;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.HabitLog;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.models.User;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.CertificateRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitLogRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.HabitRepository;
import com.Smart_habit_tracker_sht.smart_habit_tracker_sht.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final HabitLogRepository habitLogRepository;
    private final NotificationService notificationService;

    // Calculate longest streak with end date
    private StreakCalculationDTO getStreakDataWithDate(Habit habit) {
        List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateAsc(habit);
        if (logs == null || logs.isEmpty()) {
            return StreakCalculationDTO.builder().habitId(habit.getId()).habitTitle(habit.getTitle()).longestStreak(0)
                    .streakEndDate(null).build();
        }

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate streakEndDate = null;
        LocalDate currentStreakEndDate = null;
        LocalDate previousDate = null;

        for (HabitLog log : logs) {
            LocalDate currentDate = log.getLogDate();
            if (log.getStatus() == HabitLog.Status.DONE) {
                if (previousDate == null || currentDate.equals(previousDate.plusDays(1))) {
                    currentStreak++;
                    currentStreakEndDate = currentDate;
                } else {
                    currentStreak = 1;
                    currentStreakEndDate = currentDate;
                }
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak;
                    streakEndDate = currentStreakEndDate;
                }
            } else {
                currentStreak = 0;
                currentStreakEndDate = null;
            }
            previousDate = currentDate;
        }

        return StreakCalculationDTO.builder().habitId(habit.getId()).habitTitle(habit.getTitle())
                .longestStreak(longestStreak).streakEndDate(streakEndDate).build();
    }

    // Generate motivational certificate messages
    private String generateStreakCertificateMessage(User user, Habit habit, int streak, LocalDate completionDate) {
        String userName = user.getName() != null ? user.getName() : user.getEmail();
        String habitName = habit.getTitle();
        String formattedDate = completionDate != null ? completionDate.toString() : LocalDate.now().toString();

        String[] messages = {
                String.format(
                        "🏆 **CERTIFICATE OF EARLY CONSISTENCY!** Congratulations, **%s**! You've built a solid **%d-day streak** for **%s** completed on **%s**. This is the foundation of lasting change! Every master was once a beginner. Keep this momentum going! 🌟",
                        userName, streak, habitName, formattedDate),
                String.format(
                        "🎯 **EARLY ACHIEVEMENT UNLOCKED!** Amazing work, **%s**! Your **%d-day streak** with **%s** achieved on **%s** shows incredible commitment. Remember: Small, consistent actions lead to massive transformations. You're on the right path! 💪",
                        userName, streak, habitName, formattedDate),
                String.format(
                        "✨ **CONSISTENCY MILESTONE!** Bravo, **%s**! **%d consecutive days** of **%s** completed on **%s** is no small feat. You've proven your dedication and built the foundation for success. The journey of a thousand miles begins with a single step! 🚶‍♂️⭐",
                        userName, streak, habitName, formattedDate),
                String.format(
                        "🌟 **HABIT FORMATION IN PROGRESS!** Outstanding, **%s**! Your **%d-day streak** for **%s** achieved on **%s** demonstrates remarkable discipline. Neuroscience shows it takes 21 days to form a habit - you're 10%% there! Keep building! 📈",
                        userName, streak, habitName, formattedDate),
                String.format(
                        "💎 **DIAMOND IN THE ROUGH AWARD!** Exceptional start, **%s**! **%s** for **%d days straight** completed on **%s** shows you have what it takes. Consistency turns ordinary into extraordinary. You're creating your future self! 🔮",
                        userName, habitName, streak, formattedDate)
        };

        return messages[streak % messages.length];
    }

    // Generate certificates for specific streak milestones
    @Transactional
    public void generateCertificatesForStreakMilestones(int targetStreak) {
        log.info("Starting certificate generation for streak milestone: {} days", targetStreak);
        List<User> allUsers = userRepository.findAll();
        int certificatesGenerated = 0;

        for (User user : allUsers) {
            for (Habit habit : habitRepository.findByUser(user)) {
                try {
                    if (certificateRepository.existsByHabitIdAndStreakAchieved(habit.getId(), targetStreak))
                        continue;

                    StreakCalculationDTO streakData = getStreakDataWithDate(habit);
                    if (streakData.getLongestStreak() == targetStreak) {
                        LocalDate completionDate = getCompletionDate(habit, streakData);
                        String certificateMessage = generateStreakCertificateMessage(user, habit, targetStreak,
                                completionDate);

                        Certificate certificate = Certificate.builder()
                                .user(user).habit(habit).certificateMessage(certificateMessage)
                                .streakAchieved(targetStreak).awardedAt(LocalDateTime.now())
                                .streakCompletionDate(completionDate).delivered(false)
                                .type(Certificate.CertificateType.STREAK_MILESTONE).build();

                        certificateRepository.save(certificate);
                        certificatesGenerated++;
                    }
                } catch (Exception e) {
                    log.error("Error generating certificate for user: {}, habit: {}", user.getEmail(), habit.getTitle(),
                            e);
                }
            }
        }
        log.info("Certificate generation completed. Generated {} certificates for {}-day streak", certificatesGenerated,
                targetStreak);
    }

    private LocalDate getCompletionDate(Habit habit, StreakCalculationDTO streakData) {
        LocalDate completionDate = streakData.getStreakEndDate();
        if (completionDate == null) {
            List<HabitLog> logs = habitLogRepository.findByHabitOrderByLogDateAsc(habit);
            completionDate = !logs.isEmpty() ? logs.get(logs.size() - 1).getLogDate() : LocalDate.now();
            log.warn("No streak end date found for habit {}, using: {}", habit.getTitle(), completionDate);
        }
        return completionDate;
    }

    // Scheduled certificate generation
    @Scheduled(cron = "0 0 5 * * *")
    public void scheduleTwoDayStreakCertificates() {
        log.info("Starting scheduled certificate generation for 2-day streaks");
        generateCertificatesForStreakMilestones(21);
    }

    // Send notifications for undelivered certificates
    @Scheduled(cron = "0 30 16 * * *")
    @Transactional
    public void deliverPendingCertificates() {
        log.info("Starting delivery of pending certificates");
        int notificationsSent = 0;

        for (User user : userRepository.findAll()) {
            for (Certificate certificate : certificateRepository.findByUserAndDeliveredFalse(user)) {
                try {
                    notificationService.sendCertificateNotification(user, certificate);
                    certificate.setDelivered(true);
                    certificateRepository.save(certificate);
                    notificationsSent++;
                } catch (Exception e) {
                    log.error("Error delivering certificate to user: {}", user.getEmail(), e);
                }
            }
        }
        log.info("Certificate delivery completed. Sent {} notifications", notificationsSent);
    }

    // Get user certificates as DTOs
    @Transactional(readOnly = true)
    public List<CertificateDTO> getUserCertificates(User user) {
        return certificateRepository.findByUser(user).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get certificates by user and specific streak
    @Transactional(readOnly = true)
    public List<CertificateDTO> getCertificatesByUserAndStreak(User user, int streak) {
        return certificateRepository.findByUser(user).stream()
                .filter(cert -> cert.getStreakAchieved() == streak)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Convert entity to DTO
    private CertificateDTO convertToDTO(Certificate certificate) {
        return CertificateDTO.builder()
                .id(certificate.getId()).userId(certificate.getUser().getId()).habitId(certificate.getHabit().getId())
                .habitTitle(certificate.getHabit().getTitle())
                .userName(certificate.getUser().getName() != null ? certificate.getUser().getName()
                        : certificate.getUser().getEmail())
                .certificateMessage(certificate.getCertificateMessage()).streakAchieved(certificate.getStreakAchieved())
                .awardedAt(certificate.getAwardedAt()).streakCompletionDate(certificate.getStreakCompletionDate())
                .delivered(certificate.isDelivered()).type(certificate.getType().name()).build();
    }

    @Transactional
    public void migrateExistingCertificates() {
        log.info("Starting migration of existing certificates");
        int updatedCount = 0;

        for (Certificate certificate : certificateRepository.findAll()) {
            try {
                if (certificate.getStreakCompletionDate() == null) {
                    StreakCalculationDTO streakData = getStreakDataWithDate(certificate.getHabit());
                    LocalDate completionDate = streakData.getStreakEndDate() != null ? streakData.getStreakEndDate()
                            : certificate.getAwardedAt().toLocalDate();
                    certificate.setStreakCompletionDate(completionDate);
                    certificateRepository.save(certificate);
                    updatedCount++;
                }
            } catch (Exception e) {
                log.error("Error migrating certificate {}: {}", certificate.getId(), e.getMessage());
            }
        }
        log.info("Certificate migration completed. Updated {} certificates", updatedCount);
    }
}