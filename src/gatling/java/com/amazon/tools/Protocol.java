package com.amazon.tools;

import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public final class Protocol {
    private Protocol() {
    }

    private static final String http = "http";
    private static final String https = "https";

    public static final String localhost = "localhost";
    private static final String host = System.getProperty("host", localhost);

    private static final Integer defaultPort = 2021;
    private static final Integer port = Integer.getInteger("port", defaultPort);

    private static String asUrl(String protocol, String host, Integer port) {
        return protocol + "://" + host + ":" + port;
    }

    public static HttpProtocolBuilder httpProtocol() {
        return httpProtocol(http, host, port);
    }

    public static HttpProtocolBuilder httpProtocol(String host) {
        return httpProtocol(http, host, port);
    }

    public static HttpProtocolBuilder httpsProtocol() {
        return httpProtocol(https, host, port);
    }

    public static HttpProtocolBuilder httpsProtocol(String host) {
        return httpProtocol(https, host, port);
    }

    public static HttpProtocolBuilder httpsProtocol(Integer port) {
        return httpProtocol(https, host, port);
    }

    public static HttpProtocolBuilder httpsProtocol(String host, Integer port) {
        return httpProtocol(https, host, port);
    }

    public static HttpProtocolBuilder httpProtocol(String protocol, String host) {
        return httpProtocol(protocol, host, port);
    }

    public static HttpProtocolBuilder httpProtocol(String protocol, String host, Integer port) {
        return HttpDsl.http
                .baseUrl(asUrl(protocol, host, port))
                .acceptHeader("application/json")
                .header("Content-Type", "application/json");
    }
}
