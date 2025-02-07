package org.example.whenwillwemeet.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.TimeZoneConverter;
import org.example.whenwillwemeet.data.dao.AppointmentDAO;
import org.example.whenwillwemeet.data.dao.ScheduleDAO;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.TimeSlot;
import org.example.whenwillwemeet.data.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ScheduleService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ScheduleDAO scheduleDAO;

    @Autowired
    private AppointmentDAO appointmentDAO;

    public CommonResponse getSchedule(String appointmentId) {
        Optional<AppointmentModel> optionalAppointment = appointmentDAO.getAppointmentModelById(appointmentId);

        return optionalAppointment.map(appointmentModel -> {
            log.info("[ScheduleService]-[getSchedule] Schedule found [{}]", appointmentId);
            return new CommonResponse(true, HttpStatus.OK, "Schedule fetched", appointmentModel.getSchedules());
        }).orElse(
                new CommonResponse(false, HttpStatus.NOT_FOUND, "Appointment not found", null)
        );
    }

    // 주어진 Schedule 정보를 기반으로 현재 Appointment 모델과 비교하여 사용자를 TimeSlot에 추가하거나 제거
    // 즉, Frontend에서 발생한 이벤트를 전달해주면 자동으로 현재 DB의 데이터와 비교하여 Toggle
    @Transactional
    public CommonResponse updateSchedule(Schedule inputSchedule) {
        try {
            String appointmentId = inputSchedule.getAppointmentId();

            // 현재 Appointment 정보를 데이터베이스에서 fetch
            Optional<AppointmentModel> appointmentOpt = appointmentRepository.findById(appointmentId);

            if (!appointmentOpt.isPresent())
                throw new RuntimeException("Appointment not found with id: " + appointmentId);

            // InputSchedule의 TimeSlot의 User들이 상이하면 Throw Exception
            String userName = inputSchedule.getTimes().getFirst().getUsers().getFirst();
            for (TimeSlot inputTimeSlot : inputSchedule.getTimes())
                if(!Objects.equals(userName, inputTimeSlot.getUsers().getFirst()))
                    throw new RuntimeException("Different UserName Exists");

            // User가 존재하지 않으면 Throw Exception
            if(!scheduleDAO.isUserExistsInAppointment(appointmentId, userName))
                throw new RuntimeException("User [" + userName + "] not found in " + appointmentId);

            // appointment를 UTC로 변환
            AppointmentModel appointment = TimeZoneConverter.convertToUTC(appointmentOpt.get());

            // 입력 Schedule의 ID와 일치하는 Schedule을 찾기
            Optional<Schedule> existingScheduleOpt = appointment.getSchedules().stream()
                    .filter(s -> s.getId().equals(inputSchedule.getId()))
                    .findFirst();

            if (!existingScheduleOpt.isPresent())
                throw new RuntimeException("Schedule not found in Appointment");

            Schedule existingSchedule = existingScheduleOpt.get();

            // 입력 Schedule의 각 TimeSlot에 대해 Toggle 작업을 수행
            for (TimeSlot inputTimeSlot : inputSchedule.getTimes()) {
                // TimeSlot 존재 여부를 검사할 때도 UTC를 기준으로 검사
                Optional<TimeSlot> existingTimeSlotOpt = findTimeSlotByTime(
                        existingSchedule,
                        inputTimeSlot.getTime()
                );

                if (existingTimeSlotOpt.isPresent()) {
                    TimeSlot existingTimeSlot = existingTimeSlotOpt.get();
                    if (existingTimeSlot.getUsers().contains(userName)) {
                        // 사용자가 이미 존재하면 제거
                        scheduleDAO.removeUserFromTimeSlot(appointmentId, existingSchedule.getId(), inputTimeSlot.getTime(), userName, "UTC");
                    } else {
                        // 사용자가 존재하지 않으면 추가
                        scheduleDAO.addUserToTimeSlot(appointmentId, existingSchedule.getId(), inputTimeSlot.getTime(), userName, "UTC");
                    }
                } else {
                    log.error("[ScheduleService]-[updateSchedule] Appointment [{}] Schedule [{}], User [{}] TimeSlot not found : [{}]",
                            appointmentId, inputSchedule.getDate(), userName, inputTimeSlot.getTime());
                    throw new RuntimeException("TimeSlot not found in existing Schedule");
                }
            }
            log.info("[ScheduleService]-[updateSchedule] Appointment [{}] Schedule [{}], User [{}]",appointmentId, inputSchedule.getDate(), userName);
            return new CommonResponse(true, HttpStatus.OK, "Schedule [" + inputSchedule.getDate() + "], User [" + userName + "] updated");
        } catch(Exception e){
            log.error("[ScheduleService]-[updateSchedule] Schedule [{}] update failed with : {}", inputSchedule.getDate(), e.toString());
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Schedule [" + inputSchedule.getDate() + "] update failed with : [" + e + "]");
        }
    }

    private Optional<TimeSlot> findTimeSlotByTime(Schedule schedule, LocalDateTime time) {
        return schedule.getTimes().stream()
                .filter(ts -> ts.getTime().equals(time))
                .findFirst();
    }

    public CommonResponse getUserSchedule(String appointmentId, String userName) {
        return scheduleDAO.getUserSchedule(appointmentId, userName);
    }
}