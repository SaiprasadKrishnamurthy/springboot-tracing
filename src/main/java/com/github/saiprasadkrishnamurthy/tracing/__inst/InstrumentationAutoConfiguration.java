package com.github.saiprasadkrishnamurthy.tracing.__inst;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "sb.tracing.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.github.saiprasadkrishnamurthy.tracing.__inst.core")
public class InstrumentationAutoConfiguration {
}
