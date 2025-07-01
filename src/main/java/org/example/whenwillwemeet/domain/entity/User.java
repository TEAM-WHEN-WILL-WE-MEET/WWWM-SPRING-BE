package org.example.whenwillwemeet.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "CHAR(36)")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String password;

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

  public void patchName(String name) {
    if (name.isBlank()) {
      return;
    }
    this.name = name;
  }

  public void patchEmail(String email) {
    if (email.isBlank()) {
      return;
    }
    this.email = email;
  }

  public void patchPassword(String encodedPassword) {
    if (encodedPassword.isBlank()) {
      return;
    }
    this.password = encodedPassword;
  }
}
