package com.github.saiprasadkrishnamurthy.tracing.__inst.core;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceUtils {
    private TraceUtils() {
    }

    public static void addTagToTrace(final String tagName, final String value) {
        MDC.put("tag$$" + tagName, value);
    }

    public static void addEventsToTrace(final String value) {
        MDC.put("annotation$$" + UUID.randomUUID().toString(), value);
    }
}
