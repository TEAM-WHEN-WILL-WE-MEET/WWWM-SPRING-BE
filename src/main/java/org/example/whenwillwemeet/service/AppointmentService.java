package org.example.whenwillwemeet.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.common.exception.ErrorCode;
import org.example.whenwillwemeet.converter.AppointmentConverter;
import org.example.whenwillwemeet.converter.ScheduleConverter;
import org.example.whenwillwemeet.data.dao.AppointmentDAO;
import org.example.whenwillwemeet.data.dao.UserDAO;
import org.example.whenwillwemeet.data.dto.AppointmentCreateDto;
import org.example.whenwillwemeet.data.dto.MyAppointmentGetDto;
import org.example.whenwillwemeet.domain.entity.Appointment;
import org.example.whenwillwemeet.domain.entity.Schedule;
import org.example.whenwillwemeet.domain.entity.User;
import org.example.whenwillwemeet.domain.entity.UserAppointment;
import org.example.whenwillwemeet.repository.AppointmentRepository;
import org.example.whenwillwemeet.repository.ScheduleRepository;
import org.example.whenwillwemeet.repository.UserAppointmentRepository;
import org.example.whenwillwemeet.repository.UserRepository;
import org.example.whenwillwemeet.repository.UserTimeSlotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 약속(Appointment) 관련 서비스 클래스
 * - 약속 생성 - 약속 단건 조회 (필요시 사용자 연결)
 * - 사용자와 약속 간의 연결 처리
 * - 사용자의 약속 목록 조회
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

  private final AppointmentDAO appointmentDAO;
  private final UserDAO userDAO;
  private final UserRepository userRepository;
  private final UserAppointmentRepository userAppointmentRepository;
  private final AppointmentRepository appointmentRepository;
  private final UserTimeSlotRepository userTimeSlotRepository;
  private final ScheduleRepository scheduleRepository;

  /**
   * 특정 ID의 약속을 조회하며, 로그인한 사용자가 해당 약속에 참여하지 않은 경우 자동으로 연결
   *
   * @param appointmentId 조회할 약속의 ID
   * @param loginUserId   JWT를 통해 주입된 로그인 사용자 ID
   * @return 약속 객체를 포함한 성공 응답
   */
  @Transactional
  public CommonResponse getAppointmentAndLinkUser(UUID appointmentId, UUID loginUserId) {
    // 1. 유저 정보 조회
    User loginUser = userRepository.findById(loginUserId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));

    // 2. 약속 정보 조회 (Schedule Fetch Join)
    Appointment appointment = appointmentRepository.findWithSchedulesById(appointmentId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.APPOINTMENT_NOT_FOUND_EXCEPTION));

    // 3. Appointment 를 UTC 로 변환
    //TimeZoneConverter.convertToUTC(appointment);
    
    // 4. 로그인한 사용자가 약속에 참여 중인지 확인
    if (!userAppointmentRepository.existsByUserAndAppointment(loginUser, appointment)) {
      // 3. 참여하지 않은 경우 자동으로 약속에 연결
      userAppointmentRepository.save(appointment.addUsers(loginUser));
    }
    
    // 5. DTO 로 변환하여 반환
    return new CommonResponse(true, HttpStatus.OK, "요청이 정상적으로 처리 되었습니다.",
        AppointmentConverter.toResponseDto(appointment));
  }

  /**
   * 새로운 약속 생성 후 로그인한 사용자를 해당 약속에 자동 연결
   *
   * @param appointmentCreateDto 생성할 약속 정보
   * @param loginUserId      JWT를 통해 주입된 로그인 사용자 ID
   * @return 생성 성공 여부에 따른 응답
   */
  @Transactional
  public CommonResponse createAppointment(AppointmentCreateDto appointmentCreateDto,
      UUID loginUserId) {
    // 1. 유저 조회
    User loginUser = userDAO.findById(loginUserId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
    // 2. 약속 생성
    Appointment newAppointment = AppointmentConverter.toEntity(appointmentCreateDto);
    newAppointment.initializeTimes();

    // 3. 스케쥴 생성
    appointmentCreateDto.getSchedules()
        .forEach(schedule -> {
          Schedule newSchedule = ScheduleConverter.toEntity(schedule, newAppointment);
          // 약속에 스케쥴 추가
          newAppointment.addSchedule(newSchedule);
          // 스케쥴에 타임 슬롯 초기화
          newSchedule.initializeTimeSlots();
        });
    // 3. 생성한 유저를 약속에 참여시킴
    newAppointment.addUsers(loginUser);

    appointmentDAO.saveAppointment(newAppointment);

    return new CommonResponse(true, HttpStatus.CREATED, "Appointment created", AppointmentConverter.toResponseDto(newAppointment));
  }


  /**
   * 로그인한 사용자가 참여한 모든 약속 목록 조회
   *
   * @param loginUserId JWT를 통해 주입된 로그인 사용자 ID
   * @return 사용자의 약속 리스트를 포함한 응답
   */
  @Transactional(readOnly = true)
  public CommonResponse getMyAppointments(UUID loginUserId) {
    // 1. 사용자 조회
    User me = userDAO.findById(loginUserId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
    List<UserAppointment> appointmentsByUser = userAppointmentRepository.findByUser(me);
    // 2. 약속 목록 반환
    List<MyAppointmentGetDto> list = appointmentsByUser.stream()
        .map(UserAppointment::getAppointment)
        .map(AppointmentConverter::toMyAppointmentGetDto)
        .toList();
    return new CommonResponse(true, HttpStatus.OK, "요청이 정상적으로 처리 되었습니다.", list);
  }

  @Transactional
  public CommonResponse exitAppointment(UUID appointmentId, UUID loginUserId) {
    // 1. 사용자 조회
    User me = userDAO.findById(loginUserId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
    
    // 2. 약속 조회
    Appointment appointment = appointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.APPOINTMENT_NOT_FOUND_EXCEPTION));
    
    // 3. 약속-사용자 연관관계 제거
    userAppointmentRepository.deleteByAppointmentAndUser(appointment, me);

    // 4. TimeSlot - 사용자 연관관계 제거
    List<Schedule> schedules = scheduleRepository.findByAppointment(appointment);
    userTimeSlotRepository.deleteByUserAndTimeSlot_ScheduleIn(me, schedules);
    
    // 5. 만약 약속에 아무도 남지 않았을 경우 약속 삭제
    if (userAppointmentRepository.countByAppointment(appointment) == 0) {
      appointmentRepository.delete(appointment);
    }

    return new CommonResponse(true, HttpStatus.OK, "User [" + loginUserId + "] exit from appointment [" + appointmentId + "]");
  }
}
