package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HabitResponse {
    private Long id;
    private String title;
    private String description;
    private String frequency;
    private LocalDate startDate;
    private String currentStatus;
    private Long userId;
    private Integer challengeDays;

    private Integer challengeLevel;
    private LocalDate currentChallengeStartDate;

    public boolean isCurrentChallengeCompleted(int currentStreak) {
        return challengeDays != null && currentStreak >= challengeDays;
    }

    public LocalDate getEffectiveStartDate() {
        return currentChallengeStartDate != null ? currentChallengeStartDate : startDate;
    }

    public boolean canMarkToday() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(getEffectiveStartDate()) &&
                !"DONE".equalsIgnoreCase(currentStatus) &&
                !"SKIPPED".equalsIgnoreCase(currentStatus);
    }

    public static HabitResponseBuilder builder() {
        return new HabitResponseBuilder();
    }

    public static class HabitResponseBuilder {
        private Long id;
        private String title;
        private String description;
        private String frequency;
        private LocalDate startDate;
        private String currentStatus;
        private Long userId;
        private Integer challengeDays;
        private Integer challengeLevel;
        private LocalDate currentChallengeStartDate;

        public HabitResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public HabitResponseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public HabitResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public HabitResponseBuilder frequency(String frequency) {
            this.frequency = frequency;
            return this;
        }

        public HabitResponseBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public HabitResponseBuilder currentStatus(String currentStatus) {
            this.currentStatus = currentStatus;
            return this;
        }

        public HabitResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public HabitResponseBuilder challengeDays(Integer challengeDays) {
            this.challengeDays = challengeDays;
            return this;
        }

        public HabitResponseBuilder challengeLevel(Integer challengeLevel) {
            this.challengeLevel = challengeLevel;
            return this;
        }

        public HabitResponseBuilder currentChallengeStartDate(LocalDate currentChallengeStartDate) {
            this.currentChallengeStartDate = currentChallengeStartDate;
            return this;
        }

        public HabitResponse build() {
            HabitResponse response = new HabitResponse();
            response.setId(id);
            response.setTitle(title);
            response.setDescription(description);
            response.setFrequency(frequency);
            response.setStartDate(startDate);
            response.setCurrentStatus(currentStatus);
            response.setUserId(userId);
            response.setChallengeDays(challengeDays);
            response.setChallengeLevel(challengeLevel);
            response.setCurrentChallengeStartDate(currentChallengeStartDate);
            return response;
        }
    }

    @Override
    public String toString() {
        return "HabitResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", challengeLevel=" + challengeLevel +
                ", challengeDays=" + challengeDays +
                ", currentChallengeStartDate=" + currentChallengeStartDate +
                ", currentStatus='" + currentStatus + '\'' +
                '}';
    }
}
