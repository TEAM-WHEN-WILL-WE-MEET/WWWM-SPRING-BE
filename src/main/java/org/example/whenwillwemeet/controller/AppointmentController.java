package org.example.whenwillwemeet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.aop.annotation.LoginUserId;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.validation.AppointmentValidation;
import org.example.whenwillwemeet.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 약속(Appointment) 관련 API 컨트롤러
 * - 약속 단건 조회
 * - 내 약속 목록 조회
 * - 약속 생성
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentValidation appointmentValidation;

    /**
     * 약속 ID를 기준으로 단일 약속 정보 조회
     * - 기존에 참여하지 않은 유저일 경우 해당 약속에 유저 정보 추가
     *
     * @param appointmentId 조회할 약속 ID (query param)
     * @param loginUserId JWT 필터를 통해 주입된 로그인 사용자 ID
     * @return 약속 상세 정보 응답
     */
    @GetMapping("")
    public ResponseEntity<CommonResponse> getAppointment(@RequestParam("id") String appointmentId,
        @LoginUserId ObjectId loginUserId) {
        CommonResponse response;
        // appointmentId 파라미터가 비어 있는 경우
        if (appointmentId.isEmpty()) {
            log.warn("[AppointmentController]-[getAppointment] AppointmentId needed");
            response = new CommonResponse(false, HttpStatus.BAD_REQUEST, "AppointmentId needed");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        response = appointmentService.getAppointment(appointmentId, loginUserId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * 로그인한 사용자가 참여 중인 모든 약속 목록 조회
     * @param loginUserId JWT 필터를 통해 주입된 로그인 사용자 ID
     * @return 약속 리스트 응답
     */
    @GetMapping("/me")
    public ResponseEntity<CommonResponse> getMyAppointments(@LoginUserId ObjectId loginUserId) {
        CommonResponse response = appointmentService.getMyAppointments(loginUserId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * 새로운 약속 생성
     * - 약속 모델 구조에 대해 유효성 검증 수행
     * - 유효한 경우 DB에 저장
     *
     * @param appointmentModel 요청으로 전달된 약속 정보
     * @param loginUserId JWT 필터를 통해 주입된 로그인 사용자 ID
     * @return 생성 성공 또는 검증 실패 응답
     */
    @PostMapping("")
    public ResponseEntity<CommonResponse> createAppointment(@Valid @RequestBody AppointmentModel appointmentModel,
        @LoginUserId ObjectId loginUserId){
        List<String> validationErrors = appointmentValidation.validateAppointmentModel(appointmentModel);

        if (!validationErrors.isEmpty()) {
            log.warn("[AppointmentController]-[createAppointment] Validation failed: {}", validationErrors);
            CommonResponse response = new CommonResponse(false, HttpStatus.BAD_REQUEST, String.join(", ", validationErrors));
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        CommonResponse response = appointmentService.createAppointment(appointmentModel, loginUserId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
