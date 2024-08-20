package org.example.whenwillwemeet.data.model.validation;

import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.User;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserValidation {
    public List<String> validateUser(User user) {
        List<String> errors = new ArrayList<>();

        if (isNullOrEmpty(user.getName())) {
            errors.add("Name is required");
        }

        if (isNullOrEmpty(user.getPassword())) {
            errors.add("Password is required");
        }

        if (isNullOrEmpty(user.getAppointmentId())) {
            errors.add("AppointmentId is required");
        }

        return errors;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
