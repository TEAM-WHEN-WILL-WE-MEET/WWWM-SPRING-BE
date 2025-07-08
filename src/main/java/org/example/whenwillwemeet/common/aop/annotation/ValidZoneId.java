package org.example.whenwillwemeet.common.aop.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.example.whenwillwemeet.common.aop.resolver.ZoneIdValidator;

@Documented
@Constraint(validatedBy = ZoneIdValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidZoneId {
  String message() default "Invalid time zone";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
