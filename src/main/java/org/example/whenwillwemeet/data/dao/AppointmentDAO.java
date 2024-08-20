package org.example.whenwillwemeet.data.dao;

import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.constant.ConstantVariables;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.TimeSlot;
import org.example.whenwillwemeet.data.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class AppointmentDAO {
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentDAO(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Optional<AppointmentModel> getAppointmentModelById(String id){
        try{
            return appointmentRepository.findById(id);
        }catch (Exception e){
            log.error("[AppointmentDAO]-[getAppointmentModelById] Appointment {} doesn't exists", id);
            return Optional.empty();
        }
    }

    public CommonResponse getAppointmentById(String id) {
        try{
            return new CommonResponse(true, HttpStatus.OK, "Appointment fetched", appointmentRepository.findById(id));
        }catch (Exception e){
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Appointment fetch failed with : [" + e + "]");
        }
    }

    public CommonResponse createAppointment(AppointmentModel appointment) {
        try {
            log.info("[AppointmentDAO]-[createAppointment] Current Appointment Expiration time : {}h", ConstantVariables.APPOINTMENT_EXPIRATION_TIME);
            // APPOINTMENT_EXPIRATION_TIME static 상수를 통해 ExpireAt 설정
            appointment.setExpireAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(ConstantVariables.APPOINTMENT_EXPIRATION_TIME));

            // startTime부터 endTime까지 15분 단위로 TimeSlot 생성
            List<TimeSlot> timeSlots = createTimeSlots(appointment.getStartTime(), appointment.getEndTime());
            log.info("[AppointmentDAO]-[createAppointment] {} time slots created", timeSlots.size());

            // 각 Schedule에 대해 TimeSlot 설정
            if (appointment.getSchedules() != null) {
                for (Schedule schedule : appointment.getSchedules()) {
                    schedule.setId(UUID.randomUUID().toString());
                    schedule.setTimes(new ArrayList<>(timeSlots));
                }
            }

            AppointmentModel savedAppointment = appointmentRepository.save(appointment);
            log.info("[AppointmentDAO]-[createAppointment] Appointment [{}] created", savedAppointment.getId());
            return new CommonResponse(true, HttpStatus.OK, "Appointment created", savedAppointment);
        } catch (Exception e) {
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Appointment creation failed with : [" + e + "]");
        }
    }

    private List<TimeSlot> createTimeSlots(LocalDateTime startTime, LocalDateTime endTime) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        LocalDateTime currentTime = startTime;

        while (currentTime.isBefore(endTime) || currentTime.equals(endTime)) {
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setTime(currentTime);
            timeSlot.setUsers(new ArrayList<>());
            timeSlots.add(timeSlot);

            currentTime = currentTime.plus(15, ChronoUnit.MINUTES);
        }

        return timeSlots;
    }

    public CommonResponse updateAppointment(AppointmentModel appointment) {
        try {
            if (appointmentRepository.existsById(appointment.getId())) {
                AppointmentModel updatedAppointment = appointmentRepository.save(appointment);
                log.info("[AppointmentDAO]-[updateAppointment] Appointment [{}] updated", updatedAppointment.getId());
                return new CommonResponse(true, HttpStatus.OK, "Appointment updated", updatedAppointment);
            } else {
                throw new RuntimeException("Appointment not found with id: " + appointment.getId());
            }
        } catch (Exception e){
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Appointment update failed with : [" + e + "]");
        }
    }
}
