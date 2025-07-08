package org.example.whenwillwemeet.data.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.repository.AppointmentRepository;
import org.example.whenwillwemeet.domain.entity.Appointment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentDAO {

    private final AppointmentRepository appointmentRepository;

    public void saveAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);
    }


}
