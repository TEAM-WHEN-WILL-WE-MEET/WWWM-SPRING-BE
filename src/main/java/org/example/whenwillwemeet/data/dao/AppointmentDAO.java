package org.example.whenwillwemeet.data.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.TimeZoneConverter;
import org.example.whenwillwemeet.common.constant.ConstantVariables;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.common.exception.ErrorCode;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.TimeSlot;
import org.example.whenwillwemeet.data.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentDAO {

    private final AppointmentRepository appointmentRepository;

    public void saveAppointment(AppointmentModel appointmentModel) {
        appointmentRepository.save(appointmentModel);
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

    public AppointmentModel getAppointmentById(String id) {
        AppointmentModel appointmentModel = appointmentRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(ErrorCode.APPOINTMENT_NOT_FOUND_EXCEPTION));
        // appointmentModel이 이미 isPresent하기 때문에 객체를 직접 넣어서 UTC로 변환
        AppointmentModel convertedAppointment = TimeZoneConverter.convertToUTC(appointmentModel);
        log.info("[AppointmentDAO]-[getAppointmentById] Successfully fetched appointment [{}]", convertedAppointment.getId());
        return convertedAppointment;
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
            return new CommonResponse(true, HttpStatus.CREATED, "Appointment created", savedAppointment);
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

}
