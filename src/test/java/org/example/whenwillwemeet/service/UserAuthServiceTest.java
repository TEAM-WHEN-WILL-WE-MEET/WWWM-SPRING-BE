package org.example.whenwillwemeet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.data.dao.UserDAO;
import org.example.whenwillwemeet.data.dto.UserLoginDto;
import org.example.whenwillwemeet.data.dto.UserSignupDto;
import org.example.whenwillwemeet.domain.entity.User;
import org.example.whenwillwemeet.security.jwt.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

  @Mock
  UserDAO userDAO;
  @Mock
  PasswordEncoder passwordEncoder;
  @Mock
  JwtUtils jwtUtils;

  @InjectMocks
  UserAuthService userAuthService;

  @Nested
  @DisplayName("Login")
  class Login {

   @Test
   @DisplayName("이메일과 비밀번호가 올바르면 JWT 포함 성공 응답을 반환한다.")
   void success() {
     //given
     UUID userId = UUID.randomUUID();
     String name = "kim";
     String email = "a@b.com";
     String rawPassword = "1234";
     String encodedPassword = "{bcrypt}encoded";
     String jwt = "jwt.token.value";

     User user = mock(User.class);
     given(user.getId()).willReturn(userId);
     given(user.getPassword()).willReturn(encodedPassword);

     given(userDAO.findByEmail(email)).willReturn(Optional.of(user));
     given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);
     given(jwtUtils.createToken(userId)).willReturn(jwt);

     UserLoginDto dto = new UserLoginDto(name, email, rawPassword);

     //when
     CommonResponse response = userAuthService.login(dto);

     //then
     assertThat(response).isNotNull();
     assertThat(response.isSuccess()).isTrue();
     assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
     assertThat(response.getObject()).isInstanceOf(String.class);
     assertThat((String) response.getObject()).isEqualTo(jwt);

   }

    @Test
    @DisplayName("이메일로 사용자를 찾지 못하면 ApplicationException을 던진다")
    void notFoundByEmail() {
      // given
      String name = "kim";
      String email = "a@b.com";
      String rawPassword = "1234";

      given(userDAO.findByEmail(email)).willReturn(Optional.empty());

      UserLoginDto dto = new UserLoginDto(name, email, rawPassword);

      // when & then
      assertThatThrownBy(() -> userAuthService.login(dto))
          .isInstanceOf(ApplicationException.class);

      then(userDAO).should().findByEmail(email);
      then(passwordEncoder).shouldHaveNoInteractions();
      then(jwtUtils).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 ApplicationException을 던진다")
    void invalidPassword() {
      // given
      String name = "kim";
      String email = "a@b.com";
      String rawPassword = "1234";
      String encodedPassword = "{bcrypt}encoded";

      User user = mock(User.class);
      given(user.getPassword()).willReturn(encodedPassword);
      given(userDAO.findByEmail(email)).willReturn(Optional.of(user));
      given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

      UserLoginDto dto = new UserLoginDto(name, email, rawPassword);

      // when & then
      assertThatThrownBy(() -> userAuthService.login(dto))
          .isInstanceOf(ApplicationException.class);

      then(jwtUtils).shouldHaveNoInteractions();
    }

  }

  @Nested
  @DisplayName("signup")
  class Signup {

    @Test
    @DisplayName("중복 이메일이 아니면 인코딩된 비밀번호로 저장하고 201을 반환한다")
    void success() {
      // given
      String email = "a@b.com";
      String name = "홍길동";
      String rawPw = "pass";
      String encodedPw = "{bcrypt}encoded";

      given(userDAO.findByEmail(email)).willReturn(Optional.empty());
      given(passwordEncoder.encode(rawPw)).willReturn(encodedPw);

      UserSignupDto dto = new UserSignupDto(name, email, rawPw);

      // when
      CommonResponse res = userAuthService.signup(dto);

      // then
      assertThat(res).isNotNull();
      assertThat(res.isSuccess()).isTrue();
      assertThat(res.getStatus()).isEqualTo(HttpStatus.CREATED);

      // save가 실제로 어떤 엔티티를 받았는지 검증하기 위해 캡쳐
      ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
      verify(userDAO).save(captor.capture());

      User saved = captor.getValue();
      assertThat(saved).isNotNull();
      assertThat(saved.getEmail()).isEqualTo(email);
      assertThat(saved.getName()).isEqualTo(name);
      assertThat(saved.getPassword()).isEqualTo(encodedPw);

      then(passwordEncoder).should().encode(rawPw);
      then(userDAO).should(times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 ApplicationException을 던진다")
    void duplicateEmail() {
      // given
      String email = "dup@x.com";
      User exists = mock(User.class);
      given(userDAO.findByEmail(email)).willReturn(Optional.of(exists));

      // when & then
      assertThatThrownBy(() -> userAuthService.signup(new UserSignupDto("n", email, "p")))
          .isInstanceOf(ApplicationException.class);

      then(passwordEncoder).shouldHaveNoInteractions();
      then(userDAO).should(never()).save(any());
    }
  }
}