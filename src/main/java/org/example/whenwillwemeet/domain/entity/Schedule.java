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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.whenwillwemeet.common.constant.ConstantVariables;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "schedule")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDate date;

  @ManyToOne
  @JoinColumn(name = "appointment_id", nullable = false)
  private Appointment appointment;

  @Builder.Default
  @BatchSize(size = 100)
  @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<TimeSlot> timeSlots = new ArrayList<>();

  public void initializeTimeSlots() {
    List<TimeSlot> slots = new ArrayList<>();
    LocalTime startTime = LocalTime.parse(appointment.getStartTime().toString());
    LocalTime endTime = LocalTime.parse(appointment.getEndTime().toString());

    while (startTime.isBefore(endTime)) {
      // 생성 및 연결
      TimeSlot newTimeSlot = TimeSlot.builder()
          .time(LocalDateTime.of(this.date, startTime))
          .schedule(this)
          .build();
      this.timeSlots.add(newTimeSlot);
      slots.add(newTimeSlot);
      // 10분 단위로 TimeSlot을 생성하도록 변경
      startTime = startTime.plusMinutes(ConstantVariables.TIME_SLOT_UNIT_TIME);
    }
  }

}
