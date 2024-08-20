package org.example.whenwillwemeet.data.model.validation;

import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.TimeSlot;
import org.example.whenwillwemeet.data.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduleValidation {
    public List<String> validateSchedule(Schedule schedule) {
        List<String> errors = new ArrayList<>();

        if (isNullOrEmpty(schedule.getId())) {
            errors.add("ScheduleId is required");
        }

        if (isNullOrEmpty(schedule.getAppointmentId())) {
            errors.add("AppointmentId is required");
        }

        if (isNullOrEmpty(String.valueOf(schedule.getDate()))) {
            errors.add("Date is required");
        }

        if (schedule.getTimes().isEmpty()) {
            errors.add("Time Array is required");
        } else {
            for (int i = 0; i < schedule.getTimes().size(); i++) {
                TimeSlot timeSlot = schedule.getTimes().get(i);
                if (timeSlot.getTime() == null) {
                    errors.add("Time is required for TimeSlot at index " + i);
                    break;
                }
                if (timeSlot.getUsers() == null || timeSlot.getUsers().isEmpty()) {
                    errors.add("At least one user is required for TimeSlot at index " + i);
                    break;
                } else if (isNullOrEmpty(timeSlot.getUsers().getFirst())) {
                    errors.add("First user cannot be empty for TimeSlot at index " + i);
                    break;
                }
            }
        }

        return errors;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
