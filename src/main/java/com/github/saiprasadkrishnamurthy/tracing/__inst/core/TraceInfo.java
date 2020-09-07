package com.github.saiprasadkrishnamurthy.tracing.__inst.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceInfo {
    String documentation();

    String[] tags() default {};
}
