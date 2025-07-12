package org.example.whenwillwemeet.service;

import lombok.RequiredArgsConstructor;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.common.exception.ErrorCode;
import org.example.whenwillwemeet.data.dao.UserDAO;
import org.example.whenwillwemeet.data.dto.UserLoginDto;
import org.example.whenwillwemeet.data.dto.UserSignupDto;
import org.example.whenwillwemeet.domain.entity.User;
import org.example.whenwillwemeet.security.jwt.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증 관련 서비스 클래스
 * - 로그인 처리
 * - 회원가입 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAuthService {

  private final UserDAO userDAO;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;

  /**
   * 사용자 로그인 처리
   * @param userLoginDto 사용자 로그인 요청 정보 (이메일, 비밀번호)
   * @return JWT 토큰을 포함한 응답 객체
   */
  public CommonResponse login(UserLoginDto userLoginDto) {
    // 1. 이메일로 사용자 조회
    User byEmail = userDAO.findByEmail(userLoginDto.email())
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_BY_EMAIL_EXCEPTION));

    // 2. 비밀번호 일치 여부 검증
    if (!passwordEncoder.matches(userLoginDto.password(), byEmail.getPassword())) {
      throw new ApplicationException(ErrorCode.INVALID_PASSWORD_EXCEPTION);
    }

    // 3. 사용자 ID를 기반으로 JWT 토큰 생성
    String token = jwtUtils.createToken(byEmail.getId());

    // 4. 성공 응답 반환 (JWT 토큰 포함)
    return new CommonResponse(true, HttpStatus.OK, "로그인이 정상적으로 완료되었습니다.", token);
  }

  /**
   * 사용자 회원가입 처리
   * @param userSignupDto 사용자 회원가입 요청 정보 (이름, 이메일, 비밀번호)
   * @return 회원가입 완료 응답 객체
   */
  @Transactional
  public CommonResponse signup(UserSignupDto userSignupDto) {
    // 1. 이메일 중복 확인
    if(userDAO.findByEmail(userSignupDto.email()).isPresent()) {
      throw new ApplicationException(ErrorCode.ALREADY_EXIST_EXCEPTION);
    }

    // 2. 사용자 모델 생성 후 저장 (비밀번호는 인코딩)
    userDAO.save(User.create(userSignupDto.name(), userSignupDto.email(),
        passwordEncoder.encode(userSignupDto.password())));

    // 3. 성공 응답 반환
    return new CommonResponse(true, HttpStatus.CREATED, "회원가입이 정상적으로 완료되었습니다.");
  }
}
