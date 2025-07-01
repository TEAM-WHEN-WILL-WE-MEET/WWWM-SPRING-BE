package org.example.whenwillwemeet.data.dao;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.data.repository.UserRepository;
import org.example.whenwillwemeet.domain.entity.User;
import org.springframework.stereotype.Component;

/**
 * 사용자 관련 데이터 액세스 객체 (DAO)
 * - MySQL 를 통해 사용자 정보를 저장, 조회, 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDAO {

  private final UserRepository userRepository;

  public void save(User user) {
    userRepository.save(user);
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public Optional<User> findById(UUID id) {
    return userRepository.findById(id);
  }

}