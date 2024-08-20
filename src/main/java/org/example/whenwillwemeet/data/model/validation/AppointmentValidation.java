package org.example.whenwillwemeet.data.model.validation;

import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.Schedule;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class AppointmentValidation {
    public List<String> validateAppointmentModel(AppointmentModel appointmentModel) {
        List<String> errors = new ArrayList<>();

        // name 검증
        if (isNullOrEmpty(appointmentModel.getName())) {
            errors.add("Name is required");
        }

        // schedules 검증
        if (appointmentModel.getSchedules() == null || appointmentModel.getSchedules().isEmpty()) {
            errors.add("At least one schedule is required");
        } else {
            for (Schedule schedule : appointmentModel.getSchedules()) {
                if (schedule.getDate() == null) {
                    errors.add("Schedule date is required");
                    break;
                }
            }
        }

        // startTime과 endTime 검증
        if (appointmentModel.getStartTime() == null) {
            errors.add("Start time is required");
        }
        if (appointmentModel.getEndTime() == null) {
            errors.add("End time is required");
        }
        if (appointmentModel.getStartTime() != null && appointmentModel.getEndTime() != null
                && appointmentModel.getStartTime().isAfter(appointmentModel.getEndTime())) {
            errors.add("Start time must be before end time");
        }

        // timeZone 검증
        if (isNullOrEmpty(appointmentModel.getTimeZone())) {
            errors.add("Time zone is required");
        } else {
            try {
                ZoneId.of(appointmentModel.getTimeZone());
            } catch (Exception e) {
                errors.add("Invalid time zone");
            }
        }

        return errors;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
