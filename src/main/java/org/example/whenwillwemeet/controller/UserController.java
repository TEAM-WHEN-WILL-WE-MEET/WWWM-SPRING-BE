package org.example.whenwillwemeet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.aop.annotation.LoginUserId;
import org.example.whenwillwemeet.data.dto.UserLoginDto;
import org.example.whenwillwemeet.data.dto.UserPatchDto;
import org.example.whenwillwemeet.data.dto.UserSignupDto;
import org.example.whenwillwemeet.service.UserAuthService;
import org.example.whenwillwemeet.service.UserCRUDService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API 컨트롤러
 * - 회원가입
 * - 로그인
 * - 사용자 정보 수정
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users")
public class UserController {
    // 인증 관련 서비스 (로그인, 회원가입 등)
    private final UserAuthService userAuthService;
    // 사용자 CRUD 관련 서비스 (정보 조회, 수정 등)
    private final UserCRUDService userCRUDService;
    
    /**
     * 사용자 로그인 요청 처리
     * @param userLoginDto 사용자 로그인 정보 (이메일, 비밀번호)
     * @return JWT 토큰 포함 응답
     * 기획 내용에 사용자 name 도 파라미터로 받도록 되어 있어서 임시로 넣어 놓음
     */
    @PostMapping("/auth/login")
    public ResponseEntity<CommonResponse> login(@Valid @RequestBody UserLoginDto userLoginDto) {
        CommonResponse response = userAuthService.login(userLoginDto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * 사용자 회원가입 요청 처리
     * @param userSignupDto 사용자 회원가입 정보 (이름, 이메일, 비밀번호)
     * @return 가입 성공 응답
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<CommonResponse> signup(@Valid @RequestBody UserSignupDto userSignupDto) {
        CommonResponse response = userAuthService.signup(userSignupDto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * 로그인한 사용자의 정보 읽기 요청 처리
     * @param loginUserId JWT 로부터 주입된 로그인한 사용자 ID
     * @return 사용자 정보 (이름, 이메일, 비밀번호)
     */
    @GetMapping("/me")
    public ResponseEntity<CommonResponse> getMyInfo(@LoginUserId ObjectId loginUserId) {
      CommonResponse response = userCRUDService.getMyInfo(loginUserId);
      return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * 로그인한 사용자의 정보 수정 요청 처리
     * @param userPatchDto 수정할 사용자 정보 (이름, 이메일, 비밀번호)
     * @param loginUserId JWT 로부터 주입된 로그인한 사용자 ID
     * @return 수정 결과 응답
     */
    @PatchMapping("")
    public ResponseEntity<CommonResponse> updateUser(@Valid @RequestBody UserPatchDto userPatchDto,
        @LoginUserId ObjectId loginUserId) {
        CommonResponse response = userCRUDService.updateUser(userPatchDto, loginUserId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}