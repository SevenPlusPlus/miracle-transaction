package com.miracle.common.transaction.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.miracle.common.transaction.annotation.api.Propagation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LogTransactional {
	
	String module() default "";
	
	Propagation propagation() default Propagation.PROPAGATION_REQUIRED;
}
