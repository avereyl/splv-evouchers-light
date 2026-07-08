package org.splv.evouchers.core.io.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = DonorNameValidator.class)
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DonorNameConstraint {

	String message() default "Invalid donor name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
}
