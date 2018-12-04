package com.miracle.common.transaction.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.miracle.common.transaction.annotation.api.Propagation;
import com.miracle.common.transaction.annotation.api.TccMode;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TccTransactional {
	Propagation propagation() default Propagation.PROPAGATION_REQUIRED;
	
	String confirmMethod() default "";
	
	String cancelMethod() default "";
	
	TccMode mode() default TccMode.TCC;
	
	boolean asyncConfirm() default false;

	boolean asyncCancel() default false;
}
