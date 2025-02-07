package org.example.whenwillwemeet.data.dao;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.TimeZoneConverter;
import org.example.whenwillwemeet.common.constant.ConstantVariables;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.TimeSlot;
import org.example.whenwillwemeet.data.model.User;
import org.example.whenwillwemeet.data.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class AppointmentDAO {
    @Autowired
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentDAO(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Optional<AppointmentModel> getAppointmentModelById(String id){
        try{
            Optional<AppointmentModel> appointmentModel = appointmentRepository.findById(id);
            log.info("[AppointmentDAO]-[getAppointmentModelById] Successfully fetched appointment [{}]",
                    appointmentModel.get().getId());
            return Optional.of(TimeZoneConverter.convertToUTC(appointmentModel.get()));
        }catch (Exception e){
            log.error("[AppointmentDAO]-[getAppointmentModelById] Appointment {} doesn't exists", id);
            return Optional.empty();
        }
    }

    public CommonResponse getAppointmentById(String id) {
        try{
            Optional<AppointmentModel> appointmentModel = appointmentRepository.findById(id);
            if(appointmentModel.isPresent()) {
                // appointmentModel이 이미 isPresent하기 때문에 객체를 직접 넣어서 UTC로 변환
                AppointmentModel convertedAppointment = TimeZoneConverter.convertToUTC(appointmentModel.get());
                log.info("[AppointmentDAO]-[getAppointmentById] Successfully fetched aappointment [{}]", convertedAppointment.getId());
                return new CommonResponse(true, HttpStatus.OK, "Appointment fetched", convertedAppointment);
            }else
                throw new RuntimeException("Appointment not found with id: " + id);
        }catch (Exception e){
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Appointment fetch failed with : [" + e + "]");
        }
    }

    public CommonResponse createAppointment(AppointmentModel appointment) {
        try {
            log.info("[AppointmentDAO]-[createAppointment] Current Appointment Expiration time : {} days", ConstantVariables.APPOINTMENT_EXPIRATION_TIME);

            appointment.initializeTimes();

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
            log.info("[AppointmentDAO]-[createAppointment] Appointment created [{}]", savedAppointment.getId());
            return new CommonResponse(true, HttpStatus.OK, "Appointment created", savedAppointment);
        } catch (Exception e) {
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Appointment creation failed with : [" + e + "]");
        }
    }

    private List<TimeSlot> createTimeSlots(LocalDateTime startTime, LocalDateTime endTime) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalDateTime current = startTime;
        while (current.isBefore(endTime)) {
            slots.add(new TimeSlot(current, new ArrayList<>()));
            // 10분 단위로 TimeSlot을 생성하도록 변경
            current = current.plusMinutes(10);
        }
        return slots;
    }

    public CommonResponse updateAppointment(AppointmentModel appointment) {
        try {
            if (appointmentRepository.existsById(appointment.getId())) {
                AppointmentModel updatedAppointment = appointmentRepository.save(appointment);
                log.info("[AppointmentDAO]-[updateAppointment] Appointment updated [{}]", updatedAppointment.getId());
                return new CommonResponse(true, HttpStatus.OK, "Appointment updated", updatedAppointment);
            } else {
                throw new RuntimeException("Appointment not found with id: " + appointment.getId());
            }
        } catch (Exception e){
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Appointment update failed with : [" + e + "]");
        }
    }

    @Transactional
    public CommonResponse addUserToAppointment(String appointmentId, User user) {
        try {
            User newUser = new User();
            newUser.setName(user.getName());
            newUser.setPassword(user.getPassword());
            newUser.setEmail(user.getEmail());
            newUser.setPhoneNumber(user.getPhoneNumber());

            appointmentRepository.addUser(appointmentId, newUser);

            log.info("[AppointmentDAO]-[addUserToAppointment] User [{}] added to [{}]", user.getName(), appointmentId);
            return new CommonResponse(true, HttpStatus.OK, "User added to appointment successfully");
        } catch (Exception e) {
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR,"Failed to add user to appointment: " + e.getMessage());
        }
    }
}
