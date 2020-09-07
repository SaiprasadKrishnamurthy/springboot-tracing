package com.github.saiprasadkrishnamurthy.tracing.__inst.core;

import lombok.Data;

@Data
public class NoSpan {
    private final String fullyQualifiedName;
    private final String methodName;
}
