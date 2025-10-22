package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SetChallengeRequest {
    @NotNull(message = "Habit ID is required")
    private Long habitId;

    @NotNull(message = "Challenge days is required")
    @Min(value = 1, message = "Challenge days must be at least 1")
    private Integer challengeDays;

    private LocalDate challengeStartDate;

    public LocalDate getChallengeStartDate() {
        return challengeStartDate != null ? challengeStartDate : LocalDate.now();
    }

    public boolean isValidStartDate() {
        LocalDate today = LocalDate.now();
        return getChallengeStartDate().isEqual(today) || getChallengeStartDate().isAfter(today);
    }

    public static SetChallengeRequestBuilder builder() {
        return new SetChallengeRequestBuilder();
    }

    public static class SetChallengeRequestBuilder {
        private Long habitId;
        private Integer challengeDays;
        private LocalDate challengeStartDate;

        public SetChallengeRequestBuilder habitId(Long habitId) {
            this.habitId = habitId;
            return this;
        }

        public SetChallengeRequestBuilder challengeDays(Integer challengeDays) {
            this.challengeDays = challengeDays;
            return this;
        }

        public SetChallengeRequestBuilder challengeStartDate(LocalDate challengeStartDate) {
            this.challengeStartDate = challengeStartDate;
            return this;
        }

        public SetChallengeRequest build() {
            SetChallengeRequest request = new SetChallengeRequest();
            request.setHabitId(habitId);
            request.setChallengeDays(challengeDays);
            request.setChallengeStartDate(challengeStartDate);
            return request;
        }
    }

    // toString for better logging
    @Override
    public String toString() {
        return "SetChallengeRequest{" +
                "habitId=" + habitId +
                ", challengeDays=" + challengeDays +
                ", challengeStartDate=" + challengeStartDate +
                '}';
    }
}