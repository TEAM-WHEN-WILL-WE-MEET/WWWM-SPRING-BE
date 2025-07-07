package org.example.whenwillwemeet.common.aop.resolver;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.ZoneId;
import org.example.whenwillwemeet.common.aop.annotation.ValidZoneId;

public class ZoneIdValidator implements ConstraintValidator<ValidZoneId, String> {
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) return false;
    try {
      ZoneId.of(value);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
