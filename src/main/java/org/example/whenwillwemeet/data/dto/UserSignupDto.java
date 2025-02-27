package org.example.whenwillwemeet.data.dto;

import jakarta.validation.constraints.NotBlank;

public record UserSignupDto(
    @NotBlank
    String name,
    @NotBlank
    String email,
    @NotBlank
    String password
) {

}
