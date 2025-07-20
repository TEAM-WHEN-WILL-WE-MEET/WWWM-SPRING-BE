package org.example.whenwillwemeet.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.example.whenwillwemeet.data.enumerate.AuthProvider;

@Builder
public record UserSignupDto(
    @NotBlank
    String name,
    @NotBlank
    String email,
    @NotBlank
    String password,

    AuthProvider provider
) {

}
