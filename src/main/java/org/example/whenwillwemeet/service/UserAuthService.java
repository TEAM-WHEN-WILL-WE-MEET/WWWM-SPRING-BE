package org.example.whenwillwemeet.service;

import lombok.RequiredArgsConstructor;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.common.exception.ErrorCode;
import org.example.whenwillwemeet.data.dao.UserDAO;
import org.example.whenwillwemeet.data.dto.UserLoginDto;
import org.example.whenwillwemeet.data.dto.UserSignupDto;
import org.example.whenwillwemeet.data.model.UserModel;
import org.example.whenwillwemeet.security.jwt.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAuthService {

  private final UserDAO userDAO;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;

  public CommonResponse login(UserLoginDto userLoginDto) {
    //business logic
    //존재하는 회원인지 검증
    UserModel byEmail = userDAO.findByEmail(userLoginDto.email())
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_BY_EMAIL_EXCEPTION));

    //비밀번호 검증
    if (!passwordEncoder.matches(userLoginDto.password(), byEmail.getPassword())) {
      throw new ApplicationException(ErrorCode.INVALID_PASSWORD_EXCEPTION);
    }

    //User Id 로 토큰 생성
    String token = jwtUtils.createToken(byEmail.getId());

    return new CommonResponse(true, HttpStatus.OK, "로그인이 정상적으로 완료되었습니다.", token);
  }

  @Transactional
  public CommonResponse signup(UserSignupDto userSignupDto) {
    //business logic
    //이미 존재하는 이메일인지 확인
    if(userDAO.findByEmail(userSignupDto.email()).isPresent()) {
      throw new ApplicationException(ErrorCode.ALREADY_EXIST_EXCEPTION);
    }
    //새로운 Model 을 만들어서 저장
    userDAO.save(UserModel.create(userSignupDto.name(), userSignupDto.email(),
        passwordEncoder.encode(userSignupDto.password())));

    return new CommonResponse(true, HttpStatus.CREATED, "회원가입이 정상적으로 완료되었습니다.");
  }
}
