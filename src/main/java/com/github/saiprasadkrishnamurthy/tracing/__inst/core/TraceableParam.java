package com.github.saiprasadkrishnamurthy.tracing.__inst.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceableParam {
    String documentation();

    String[] tags() default {};

    String[] keyPathExpressions();
}
