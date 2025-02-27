package org.example.whenwillwemeet.data.model;

import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Document(collection = "users")
public class UserModel {

  @Id
  private ObjectId id;
  private String name;
  private String email;
  private String password;
  @CreatedDate
  private LocalDateTime createdAt;
  @DBRef
  private List<AppointmentModel> appointments;

  public static UserModel create(String name, String email, String password) {
    return UserModel.builder()
        .id(ObjectId.get())
        .name(name)
        .email(email)
        .password(password)
        .build();
  }

}
