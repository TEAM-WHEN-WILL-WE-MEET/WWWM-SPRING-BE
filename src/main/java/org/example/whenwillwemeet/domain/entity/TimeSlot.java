package org.example.whenwillwemeet.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.whenwillwemeet.common.constant.ConstantVariables;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "time_slot")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDateTime time;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id", nullable = false)
  private Schedule schedule;

  @Builder.Default
  @BatchSize(size = 50)
  @OneToMany(mappedBy = "timeSlot", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<UserTimeSlot> users = new ArrayList<>();

  public void updateTime(LocalDateTime time) {
    this.time = time;
  }

  public void applyRelationship(Schedule schedule) {
    this.schedule = schedule;
    schedule.getTimeSlots().add(this);
  }

  public static void createTimeSlotsAndApplyRelationship(LocalDateTime startTime, LocalDateTime endTime,
      Schedule schedule) {
    List<TimeSlot> slots = new ArrayList<>();

    while (startTime.isBefore(endTime)) {
      TimeSlot newTimeSlot = TimeSlot.builder()
          .time(startTime)
          .build();
      newTimeSlot.applyRelationship(schedule);
      slots.add(newTimeSlot);
      // 10분 단위로 TimeSlot을 생성하도록 변경
      startTime = startTime.plusMinutes(ConstantVariables.TIME_SLOT_UNIT_TIME);
    }
  }

  public UserTimeSlot addUser(User user) {
    UserTimeSlot build = UserTimeSlot.builder()
        .user(user)
        .timeSlot(this)
        .build();
    users.add(build);
    return build;
  }
}
