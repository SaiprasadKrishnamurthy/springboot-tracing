package com.github.saiprasadkrishnamurthy.tracing.__inst.core;

import com.api.jsonata4java.expressions.Expressions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class SpanSender extends Sender {

    private boolean spanSent = false;
    private final Environment environment;
    private final SendTraceProperties sendTraceProperties;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Consumer<List> spansConsumer;

    public SpanSender(Environment environment, SendTraceProperties sendTraceProperties, Consumer<List> spansConsumer) {
        this.environment = environment;
        this.sendTraceProperties = sendTraceProperties;
        this.spansConsumer = spansConsumer;
    }

    boolean isSpanSent() {
        return this.spanSent;
    }

    @Override
    public Encoding encoding() {
        return Encoding.JSON;
    }

    @Override
    public int messageMaxBytes() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int messageSizeInBytes(List<byte[]> encodedSpans) {
        return encoding().listSizeInBytes(encodedSpans);
    }

    @Override
    public Call<Void> sendSpans(List<byte[]> encodedSpans) {
        this.spanSent = true;
        if (environment.getProperty("sb.tracing.enabled", Boolean.class, false)) {
            String spans = encodedSpans.stream().map(String::new).collect(Collectors.joining(","));
            boolean shouldExport = sendTraceProperties.getConditions().stream().anyMatch(c -> {
                try {
                    Expressions exp = Expressions.parse(c);
                    JsonNode jnode = exp.evaluate(OBJECT_MAPPER.readTree(spans));
                    return jnode.asBoolean();
                } catch (Exception ex) {
                    log.error("Jsonata error:", ex);
                    return false;
                }
            });
            if (shouldExport) {
                spansConsumer.accept(encodedSpans.stream().map(String::new).collect(Collectors.toList()));
            }
        }
        return Call.create(null);
    }
}