package org.example.whenwillwemeet.data.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentUser {

  @Id
  private ObjectId id;
  private String name;
  private String email;
}
