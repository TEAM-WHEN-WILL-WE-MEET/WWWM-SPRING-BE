package org.example.whenwillwemeet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.common.exception.ErrorCode;
import org.example.whenwillwemeet.converter.ScheduleConverter;
import org.example.whenwillwemeet.data.dao.UserDAO;
import org.example.whenwillwemeet.data.dto.AppointmentGetDto.ScheduleGetDto;
import org.example.whenwillwemeet.data.dto.ScheduleUpdateDto;
import org.example.whenwillwemeet.data.dto.TimeslotToggleDto;
import org.example.whenwillwemeet.domain.entity.TimeSlot;
import org.example.whenwillwemeet.repository.AppointmentRepository;
import org.example.whenwillwemeet.repository.ScheduleRepository;
import org.example.whenwillwemeet.repository.TimeSlotRepository;
import org.example.whenwillwemeet.repository.UserRepository;
import org.example.whenwillwemeet.domain.entity.Appointment;
import org.example.whenwillwemeet.domain.entity.Schedule;
import org.example.whenwillwemeet.domain.entity.User;
import org.example.whenwillwemeet.domain.entity.UserTimeSlot;
import org.example.whenwillwemeet.repository.UserTimeSlotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final UserDAO userDAO;
  private final AppointmentRepository appointmentRepository;
  private final ScheduleRepository scheduleRepository;
  private final UserTimeSlotRepository userTimeSlotRepository;
  private final UserRepository userRepository;
  private final TimeSlotRepository timeSlotRepository;

  @Transactional(readOnly = true)
  public CommonResponse getSchedule(UUID appointmentId) {
    // 1. 약속 조회
    Appointment appointment = appointmentRepository.findWithSchedulesById(appointmentId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.APPOINTMENT_NOT_FOUND_EXCEPTION));
    
    // 2. 스케쥴 DTO로 변환
    List<ScheduleGetDto> result = appointment.getSchedules().stream()
        .map(ScheduleConverter::toScheduleGetDto)
        .toList();
    return new CommonResponse(true, HttpStatus.OK, "Schedule fetched", result);
  }

  @Transactional(readOnly = true)
  public CommonResponse getUserSchedule(UUID appointmentId, UUID userId) {
    // 1. User 조회
    User user = userRepository.getReferenceById(userId);
    // 2. 스케쥴 조회 (유저가 참여한 스케쥴만)
    List<Schedule> schedules = scheduleRepository.findWithTimeSlotByAppointmentIdAndUser(appointmentId, user);
    List<ScheduleGetDto> result = schedules.stream()
        .map(ScheduleConverter::toScheduleGetDto)
        .toList();

    return new CommonResponse(true, HttpStatus.OK, "User [" + userId + "] schedule fetched", result);
  }

  @Transactional
  public CommonResponse enableTimeslot(UUID appointmentId, Long scheduleId, TimeslotToggleDto dto, UUID loginUserId) {
    // 1. User 조회
    User loginUser = userDAO.findById(loginUserId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));

    // 2. Schedule 프록시 조회
    Schedule schedule = scheduleRepository.getReferenceById(scheduleId);

    List<TimeSlot> targetTimeSlots = timeSlotRepository.findByScheduleAndTimeIn(schedule,
        dto.getTimes());

    List<UserTimeSlot> savedUsers = targetTimeSlots.stream()
        .map(timeSlot -> timeSlot.addUser(loginUser))
        .toList();

    userTimeSlotRepository.saveAll(savedUsers);

    return new CommonResponse(true, HttpStatus.OK,
        "Schedule [" + scheduleId + "], User [" + loginUser.getId()
            + "] updated (toggle enabled)");
  }

  @Transactional
  public CommonResponse disabledTimeslot(UUID appointmentId, Long scheduleId, TimeslotToggleDto dto, UUID loginUserId) {
    // 1. User 조회
    User loginUser = userDAO.findById(loginUserId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));

    // 2. Schedule 프록시 조회
    Schedule schedule = scheduleRepository.getReferenceById(scheduleId);

    List<TimeSlot> targetTimeSlots = timeSlotRepository.findByScheduleAndTimeIn(schedule,
        dto.getTimes());

    List<UserTimeSlot> byUserAndTimeSlotIn = userTimeSlotRepository.findByUserAndTimeSlotIn(
        loginUser, targetTimeSlots);

    userTimeSlotRepository.deleteAll(byUserAndTimeSlotIn);

    return new CommonResponse(true, HttpStatus.OK,
        "Schedule [" + scheduleId + "], User [" + loginUser.getId()
            + "] updated (toggle disabled)");
  }

  @Deprecated
  @Transactional
  public CommonResponse updateUserTimeSlots(UUID appointmentId, ScheduleUpdateDto dto, UUID loginUserId) {
    // 1. User 조회
    User loginUser = userDAO.findById(loginUserId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));

    // 2. Schedule 조회
    // -> 조회 시 TimeSlot을 조인하여 가져옴. 그 중 dto에서 받은 시간 범위에 해당하는 TimeSlot만 가져옴
    Schedule targetSchedule = scheduleRepository.findWithTimeSlotByIdAndTimesIn(
        dto.getScheduleId(),
        dto.getTimes()
    ).orElseThrow(() -> new ApplicationException(ErrorCode.SCHEDULE_NOT_FOUND_EXCEPTION));

    // 3. 조회된 UserTimeSlot 중 user가 참여한 UserTimeSlot 삭제
    List<UserTimeSlot> deleteUserTimeSlot = targetSchedule.getTimeSlots().stream()
        .flatMap(slot -> slot.getUsers().stream())
        .filter(user -> user.getUser().getId().equals(loginUser.getId()))
        .toList();

    // 4. UserTimeSlot이 조회되지 않았으면 유저가 참여하지 않은 시간대이므로 추가
    List<UserTimeSlot> insertTimeSlots = targetSchedule.getTimeSlots().stream()
        .filter(slot -> slot.getUsers().stream().noneMatch(uts -> uts.getUser().getId().equals(loginUser.getId())))
        .map(slot-> slot.addUser(loginUser))
        .toList();

    userTimeSlotRepository.deleteAll(deleteUserTimeSlot);
    userTimeSlotRepository.saveAll(insertTimeSlots);

    return new CommonResponse(true, HttpStatus.OK,
        "Schedule [" + dto.getScheduleId() + "], User [" + loginUser.getId()
            + "] updated");
  }
}