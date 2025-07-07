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
import org.example.whenwillwemeet.repository.AppointmentRepository;
import org.example.whenwillwemeet.repository.ScheduleRepository;
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


  /*
  // 주어진 Schedule 정보를 기반으로 현재 Appointment 모델과 비교하여 사용자를 TimeSlot에 추가하거나 제거
  // 즉, Frontend에서 발생한 이벤트를 전달해주면 자동으로 현재 DB의 데이터와 비교하여 Toggle
  @Transactional
  @Deprecated
  public CommonResponse updateSchedule(ScheduleUpdateDto dto, UUID loginUserId) {
    // 1. User, Schedule 조회
    User loginUser = userDAO.findById(loginUserId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
    Schedule targetSchedule = scheduleRepository.findById(dto.getScheduleId())
        .orElseThrow(() -> new ApplicationException(ErrorCode.SCHEDULE_NOT_FOUND_EXCEPTION));

    List<LocalDateTime> requestedTimes = dto.getTimes();

    // 2. 해당 스케줄의 TimeSlot 중 요청된 시간에 해당하는 것만 조회
    List<TimeSlot> relevantTimeSlots = timeSlotRepository.findByScheduleAndTimeIn(targetSchedule,
        requestedTimes);

    // 3. 현재 유저가 이미 참여한 UserTimeSlot 조회
    List<UserTimeSlot> userJoinedSlots = userTimeSlotRepository.findByUserAndTimeSlotIn(loginUser,
        relevantTimeSlots);

    // 4. 유저가 이미 참여한 시간 추출
    Set<LocalDateTime> joinedTimes = userJoinedSlots.stream()
        .map(fus -> {
          return fus.getTimeSlot().getTime();
        })
        .collect(Collectors.toSet());

    // 5. 아직 참여하지 않은 시간 필터링
    List<TimeSlot> newJoinTargets = relevantTimeSlots.stream()
        .filter(slot -> !joinedTimes.contains(slot.getTime()))
        .collect(Collectors.toList());

    // 6. 새로 참여할 UserTimeSlot 생성
    List<UserTimeSlot> userSlotsToInsert = newJoinTargets.stream()
        .map(slot -> {
          UserTimeSlot uts = UserTimeSlot.builder()
              .user(loginUser)
              .timeSlot(slot)
              .build();
          uts.applyRelationships(loginUser, slot); // 양방향 연관
          return uts;
        })
        .toList();

    userTimeSlotRepository.saveAll(userSlotsToInsert);
    userTimeSlotRepository.deleteAll(userJoinedSlots);

    return new CommonResponse(true, HttpStatus.OK,
        "Schedule [" + dto.getScheduleId() + "], User [" + loginUser.getId()
            + "] updated");
  }

   */

}