package org.example.whenwillwemeet.service;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.common.exception.ErrorCode;
import org.example.whenwillwemeet.data.dao.AppointmentDAO;
import org.example.whenwillwemeet.data.dao.UserDAO;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.AppointmentUser;
import org.example.whenwillwemeet.data.model.UserModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 약속(Appointment) 관련 서비스 클래스
 * - 약속 생성
 * - 약속 단건 조회 (필요시 사용자 연결)
 * - 사용자와 약속 간의 연결 처리
 * - 사용자의 약속 목록 조회
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

  private final AppointmentDAO appointmentDAO;
  private final UserDAO userDAO;

  /**
   * 특정 ID의 약속을 조회하며, 로그인한 사용자가 해당 약속에 참여하지 않은 경우 자동으로 연결
   *
   * @param appointmentId 조회할 약속의 ID
   * @param loginUserId JWT를 통해 주입된 로그인 사용자 ID
   * @return 약속 객체를 포함한 성공 응답
   */
  @Transactional
  public CommonResponse getAppointment(String appointmentId, ObjectId loginUserId) {
    // 1. 약속 정보 조회
    AppointmentModel appointmentById = appointmentDAO.getAppointmentById(appointmentId);
    // 2. 로그인한 사용자가 약속에 참여 중인지 확인
    if (appointmentById.getUsers().stream()
        .noneMatch(user -> user.getId().equals(loginUserId))) {
      // 3. 참여하지 않은 경우 자동으로 약속에 연결
      linkUserToAppointment(loginUserId, appointmentById);
    }
    return new CommonResponse(true, HttpStatus.OK, "요청이 정상적으로 처리 되었습니다.", appointmentById);
  }

  /**
   * 새로운 약속 생성 후 로그인한 사용자를 해당 약속에 자동 연결
   *
   * @param appointmentModel 생성할 약속 정보
   * @param loginUserId JWT를 통해 주입된 로그인 사용자 ID
   * @return 생성 성공 여부에 따른 응답
   */
  @Transactional
  public CommonResponse createAppointment(AppointmentModel appointmentModel, ObjectId loginUserId) {
    // 1. 약속 생성 요청
    CommonResponse response = appointmentDAO.createAppointment(appointmentModel);
    // 2. 생성 성공 시 사용자 연결 처리
    if (response.isSuccess()) {
      linkUserToAppointment(loginUserId, (AppointmentModel) response.getObject());
    }
    return response;
  }

  /**
   * 사용자와 약속 간의 양방향 연결 수행
   * - UserModel에 약속 추가
   * - AppointmentModel에 AppointmentUser 정보 추가
   *
   * @param userId 사용자 ID
   * @param appointmentModel 연결할 약속
   */
  private void linkUserToAppointment(ObjectId userId, AppointmentModel appointmentModel) {
    // 1. 사용자 조회
    UserModel userModel = userDAO.findById(userId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
    // 2. 사용자 정보에 약속 추가
    userModel.addAppointment(appointmentModel);
    userDAO.save(userModel);
    // 3. 약속에 사용자 정보 추가 (AppointmentUser로 변환 후 추가)
    appointmentModel.addAppointmentUser(AppointmentUser.of(userModel));
    appointmentDAO.saveAppointment(appointmentModel);
  }

  /**
   * 로그인한 사용자가 참여한 모든 약속 목록 조회
   *
   * @param loginUserId JWT를 통해 주입된 로그인 사용자 ID
   * @return 사용자의 약속 리스트를 포함한 응답
   */
  @Transactional(readOnly = true)
  public CommonResponse getMyAppointments(ObjectId loginUserId) {
    // 1. 사용자 조회
    UserModel me = userDAO.findById(loginUserId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
    // 2. 약속 목록 반환
    return new CommonResponse(true, HttpStatus.OK, "요청이 정상적으로 처리 되었습니다.", me.getAppointments());
  }
}
