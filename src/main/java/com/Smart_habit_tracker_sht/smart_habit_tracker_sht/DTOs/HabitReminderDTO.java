package com.Smart_habit_tracker_sht.smart_habit_tracker_sht.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitReminderDTO {
    private String title;
    private String description;
    private Integer challengeDays;
    private Integer currentStreak;
    private String frequency;

    // Manual builder implementation if Lombok not working
    public static HabitReminderDTOBuilder builder() {
        return new HabitReminderDTOBuilder();
    }

    public static class HabitReminderDTOBuilder {
        private String title;
        private String description;
        private Integer challengeDays;
        private Integer currentStreak;
        private String frequency;

        public HabitReminderDTOBuilder title(String title) {
            this.title = title;
            return this;
        }

        public HabitReminderDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public HabitReminderDTOBuilder challengeDays(Integer challengeDays) {
            this.challengeDays = challengeDays;
            return this;
        }

        public HabitReminderDTOBuilder currentStreak(Integer currentStreak) {
            this.currentStreak = currentStreak;
            return this;
        }

        public HabitReminderDTOBuilder frequency(String frequency) {
            this.frequency = frequency;
            return this;
        }

        public HabitReminderDTO build() {
            return new HabitReminderDTO(title, description, challengeDays, currentStreak, frequency);
        }
    }
}
