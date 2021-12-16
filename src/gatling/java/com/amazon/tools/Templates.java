package com.amazon.tools;

import io.gatling.javaapi.core.Session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class Templates {
    public static final String APACHE_COMMON_LOG_DATETIME_PATTERN = "d/LLL/uuuu:HH:mm:ss";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(APACHE_COMMON_LOG_DATETIME_PATTERN);

    public static String now() {
        return formatter.format(LocalDateTime.now()) + " -0700";
    }
    
    public static final Function<Session, String> apacheCommonLogTemplate(int batchSize) {
        return session -> {
            String log = "{\"log\": \"127.0.0.1 - frank [" + now() + "] \\\"GET /apache_pb.gif HTTP/1.0\\\" 200 2326\"}";
            List<String> logs = Collections.nCopies(batchSize, log);
            String logArray = String.join(",", logs);
            return "[" + logArray + "]";
        };
    }

    private Templates() {
    }
}
