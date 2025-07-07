package org.example.whenwillwemeet.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.constant.ConstantVariables;

@Entity
@Table(name = "appointment")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "time_zone")
  private String timeZone;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "expire_at", nullable = false)
  private LocalDateTime expireAt;

  @Builder.Default
  @OneToMany(mappedBy = "appointment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Schedule> schedules = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "appointment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<UserAppointment> users = new ArrayList<>();

  // APPOINTMENT_EXPIRATION_TIME static 상수를 통해 ExpireAt 설정
  // TTL 기능을 활성화하기 위해 별도로 MongoDB Collectiond에 TTL Index 추가
  public void initializeTimes() {
    ZoneId appointmentZoneId = ZoneId.of(this.timeZone);
    ZonedDateTime nowInAppointmentZone = ZonedDateTime.now(appointmentZoneId);
    log.info("[AppointmentModel]-[initializeTimes] Now In Appointment TimeZone {}", nowInAppointmentZone);

    this.createdAt = nowInAppointmentZone.withZoneSameInstant(ZoneId.of(this.timeZone)).toLocalDateTime();
    this.expireAt = nowInAppointmentZone.plusDays(ConstantVariables.APPOINTMENT_EXPIRATION_TIME).toLocalDateTime();
  }

  public UserAppointment addUsers(User user) {
    UserAppointment build = UserAppointment.builder()
        .user(user)
        .appointment(this)
        .build();
    users.add(build);
    return build;
  }

  public void addSchedule(Schedule schedule) {
    schedules.add(schedule);
  }
}
