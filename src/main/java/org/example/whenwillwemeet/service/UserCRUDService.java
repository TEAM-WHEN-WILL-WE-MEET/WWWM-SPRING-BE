package org.example.whenwillwemeet.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.common.exception.ErrorCode;
import org.example.whenwillwemeet.data.dao.AppointmentDAO;
import org.example.whenwillwemeet.data.dao.UserDAO;
import org.example.whenwillwemeet.data.dto.UserInfoDto;
import org.example.whenwillwemeet.data.dto.UserPatchDto;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.UserModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 정보 조회 및 수정 관련 서비스
 * - 사용자 본인 정보 조회
 * - 사용자 정보 수정 (이름, 이메일, 비밀번호)
 * - 약속(Appointment)에 포함된 사용자 정보도 함께 업데이트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCRUDService {

    private final AppointmentDAO appointmentDAO;
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인한 사용자의 정보를 조회
     * @param loginUserId JWT 필터를 통해 주입된 사용자 ID
     * @return 사용자 정보 DTO 포함 응답
     */
    @Transactional(readOnly = true)
    public CommonResponse getMyInfo(ObjectId loginUserId) {
        // 사용자 조회
        UserModel userModel = userDAO.findById(loginUserId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
        // 사용자 정보를 DTO 로 감싸 반환
        return new CommonResponse(true, HttpStatus.OK, "요청이 성공적으로 처리되었습니다.", UserInfoDto.of(userModel));
    }

    /**
     * 로그인한 사용자의 정보를 업데이트 (이름, 이메일, 비밀번호)
     * 또한 해당 사용자가 포함된 모든 약속(Appointment)의 사용자 정보도 함께 반영
     * null 로 들어온 데이터는 변경하지 않음
     * @param userPatchDto 수정 요청 정보
     * @param loginUserId 로그인 사용자 ID
     * @return 수정 완료 응답
     */
    @Transactional
    public CommonResponse updateUser(UserPatchDto userPatchDto, ObjectId loginUserId) {
        // 사용자 조회
        UserModel targetUser = userDAO.findById(loginUserId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
        StringBuilder sb = new StringBuilder();
        /**
         * TODO: name, email, password 비즈니스 로직에 맞게 유효성 검사 필요
         */

        // 사용자 정보 수정. 수정 된 필드 이름을 sb에 추가
        if (!userPatchDto.name().isBlank()) {
            sb.append("name ");
            targetUser.patchName(userPatchDto.name());
        }
        if (!userPatchDto.email().isBlank()) {
            sb.append("email ");
            targetUser.patchEmail(userPatchDto.email());
        }
        if (!userPatchDto.password().isBlank()) {
            sb.append("password ");
            targetUser.patchPassword(passwordEncoder.encode(userPatchDto.password()));
        }
        // 변경사항 저장
        userDAO.save(targetUser);

        // 해당 사용자가 참여한 모든 약속의 사용자 정보 (AppointmentUser) 도 함께 업데이트
        List<AppointmentModel> appointments = Optional.ofNullable(targetUser.getAppointments())
            .orElse(Collections.emptyList());

        for (AppointmentModel appointmentModel : appointments) {
            appointmentModel.getUsers().stream()
                .filter(user -> user.getId().equals(targetUser.getId())) // 약속에서 사용자 찾기
                .forEach(me -> me.patchData(targetUser)); // 사용자 정보 패치
            appointmentDAO.saveAppointment(appointmentModel); // 변경사항 저장
        }

        return new CommonResponse(true, HttpStatus.OK, "User information about ("+ sb.toString() +") updated successfully [" + targetUser.getName() + "]");
    }

}