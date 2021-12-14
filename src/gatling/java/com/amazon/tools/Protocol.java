package com.amazon.tools;

import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public final class Protocol {
    private Protocol() {
    }

    public static final String localhost = "http://localhost:2021";
    private static final String targetHost = System.getProperty("targetHost", localhost);


    public static HttpProtocolBuilder httpProtocol() {
        return httpProtocol(targetHost);
    }

    public static HttpProtocolBuilder httpProtocol(String targetHost) {
        return HttpDsl.http
                .baseUrl(targetHost)
                .acceptHeader("application/json")
                .header("Content-Type", "application/json");
    }
}
