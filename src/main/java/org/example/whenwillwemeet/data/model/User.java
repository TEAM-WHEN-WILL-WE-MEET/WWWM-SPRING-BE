package org.example.whenwillwemeet.data.model;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String name;
    private String password;
    private String email;
    private String phoneNumber;
    private String appointmentId;
}