package com.github.saiprasadkrishnamurthy.tracing.__inst.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.zipkin2.ZipkinAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;

@ConditionalOnProperty(name = "send.trace.to.mongodb", havingValue = "true", matchIfMissing = false)
@Configuration
public class MongoSenderConfig {

    @Autowired
    private Environment environment;

    @Autowired
    private SendTraceProperties sendTraceProperties;

    @Bean(ZipkinAutoConfiguration.REPORTER_BEAN_NAME)
    Reporter<Span> myReporter() {
        return AsyncReporter.create(mySender());
    }

    @Bean(ZipkinAutoConfiguration.SENDER_BEAN_NAME)
    SpanSender mySender() {
        // TODO dump to Mongo
        return new SpanSender(environment, sendTraceProperties, System.out::println);
    }
}