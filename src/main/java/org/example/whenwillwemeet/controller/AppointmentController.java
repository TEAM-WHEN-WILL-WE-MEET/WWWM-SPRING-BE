package org.example.whenwillwemeet.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.aop.annotation.LoginUserId;
import org.example.whenwillwemeet.data.dto.AppointmentCreateDto;
import org.example.whenwillwemeet.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 약속 ID를 기준으로 단일 약속 정보 조회
     * - 기존에 참여하지 않은 유저일 경우 해당 약속에 유저 정보 추가
     *
     * @param appointmentId 조회할 약속 ID (path variable)
     * @param loginUserId JWT 필터를 통해 주입된 로그인 사용자 ID
     * @return 약속 상세 정보 응답
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<CommonResponse> getAppointment(@PathVariable UUID appointmentId,
        @LoginUserId UUID loginUserId) {
        CommonResponse response = appointmentService.getAppointmentAndLinkUser(appointmentId, loginUserId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * 로그인한 사용자가 참여 중인 모든 약속 목록 조회
     * @param loginUserId JWT 필터를 통해 주입된 로그인 사용자 ID
     * @return 약속 리스트 응답
     */
    @GetMapping("/me")
    public ResponseEntity<CommonResponse> getMyAppointments(@LoginUserId UUID loginUserId) {
        CommonResponse response = appointmentService.getMyAppointments(loginUserId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * 새로운 약속 생성
     * - 약속 모델 구조에 대해 유효성 검증 수행
     * - 유효한 경우 DB에 저장
     *
     * @param loginUserId JWT 필터를 통해 주입된 로그인 사용자 ID
     * @return 생성 성공 또는 검증 실패 응답
     */
    @PostMapping("")
    public ResponseEntity<CommonResponse> createAppointment(@Valid @RequestBody AppointmentCreateDto appointmentCreateDto,
        @LoginUserId UUID loginUserId){
        CommonResponse response = appointmentService.createAppointment(appointmentCreateDto, loginUserId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
