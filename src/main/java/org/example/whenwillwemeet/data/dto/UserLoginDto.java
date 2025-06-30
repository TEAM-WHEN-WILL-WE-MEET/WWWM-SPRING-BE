package org.example.whenwillwemeet.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginDto(
    @NotBlank
    String name,
    @Email
    String email,
    @NotBlank
    String password
) {

}
