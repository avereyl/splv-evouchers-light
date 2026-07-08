package org.splv.evouchers.core.io.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = MultilineValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MultilineConstraint {

	String message() default "Invalid multiline string";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    int maxLines() default Integer.MAX_VALUE;
    int minLengthPerLine() default 0;
    int maxLengthPerLine() default Integer.MAX_VALUE;
    
}
