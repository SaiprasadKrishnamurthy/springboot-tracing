package com.github.saiprasadkrishnamurthy.tracing.__inst.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "sendtrace")
public class SendTraceProperties {
    private List<String> conditions = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

}