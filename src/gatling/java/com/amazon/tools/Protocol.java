package com.amazon.tools;

import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Protocol {
    private Protocol() {
    }

    private static final String http = "http";
    private static final String https = "https";

    public static final String localhost = "localhost";
    private static final String host = System.getProperty("host", localhost);

    private static final String defaultPort = "2021";
    private static final String port = System.getProperty("port", defaultPort);

    public static final List<HttpProtocolBuilder> allProtocols = Stream.of(http, https)
            .map(protocol -> httpProtocol(protocol, host))
            .collect(Collectors.toList());

    private static String asUrl(String protocol, String host, String port) {
        return protocol + "://" + host + ":" + port;
    }

    public static HttpProtocolBuilder httpProtocol() {
        return httpProtocol(host);
    }

    public static HttpProtocolBuilder httpProtocol(String host) {
        return httpProtocol(http, host);
    }

    public static HttpProtocolBuilder httpsProtocol() {
        return httpsProtocol(host);
    }

    public static HttpProtocolBuilder httpsProtocol(String host) {
        return httpProtocol(https, host);
    }

    public static HttpProtocolBuilder httpProtocol(String protocol, String host) {
        return httpProtocol(protocol, host, port);
    }

    public static HttpProtocolBuilder httpProtocol(String protocol, String host, String port) {
        return HttpDsl.http
                .baseUrl(asUrl(protocol, host, port))
                .acceptHeader("application/json")
                .header("Content-Type", "application/json");
    }
}
