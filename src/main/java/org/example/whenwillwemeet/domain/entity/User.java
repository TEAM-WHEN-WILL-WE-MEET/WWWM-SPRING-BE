package org.example.whenwillwemeet.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "user")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String password;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public static User create(@NotBlank String name, @NotBlank String email, String encodedPassword) {
    return User.builder()
        .id(UUID.randomUUID())
        .name(name)
        .email(email)
        .password(encodedPassword)
        .createdAt(LocalDateTime.now())
        .build();
  }

  public boolean patchName(String name) {
    if (!isValidName(name)) {
      return false;
    }
    this.name = name;
    return true;
  }

  public boolean patchEmail(String email) {
    if (!isValidEmail(email)) {
      return false;
    }
    this.email = email;
    return true;
  }

  public boolean patchPassword(String encodedPassword) {
    if (!isValidPassword(encodedPassword)) {
      return false;
    }
    this.password = encodedPassword;
    return true;
  }

  public boolean isValidName(String name) {
    if (Objects.isNull(name)) {
      return false;
    }
    if (name.isBlank()) {
      return false;
    }
    return true;
  }

  public boolean isValidEmail(String email) {
    if (Objects.isNull(email)) {
      return false;
    }
    if (email.isBlank()) {
      return false;
    }
    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
      return false;
    }
    return true;
  }

  public boolean isValidPassword(String encodedPassword) {
    if (Objects.isNull(encodedPassword)) {
      return false;
    }
    if (encodedPassword.isBlank()) {
      return false;
    }
    return true;
  }

}
