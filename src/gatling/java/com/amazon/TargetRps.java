package com.amazon;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Function;

public class TargetRps extends Simulation {
    static final class Templates {
        public static final Function<Session, String> template = session -> {
            int logsPerRequest = Integer.getInteger("logsPerRequest", 1);
            return "[" + String.join(",", Collections.nCopies(logsPerRequest, "{\"log\": \"127.0.0.1 - frank [7/Dec/2021:10:00:00 -0700] \\\"GET /apache_pb.gif HTTP/1.0\\\" 200 2326\"}")) + "]";
        };
    }

    HttpProtocolBuilder httpProtocol = HttpDsl.http
            .baseUrl(System.getProperty("targetHost", "http://localhost:2021"))
            .acceptHeader("application/json")
            .header("Content-Type", "application/json");

    ChainBuilder sendTemplateLog = CoreDsl.exec(
            HttpDsl.http("Post log")
                    .post("/log/ingest")
                    .body(CoreDsl.StringBody(BasicSimulation.Templates.template)));

    ScenarioBuilder loadScenario = CoreDsl.scenario("Fixed Duration Load Simulation")
            .forever()
            .on(sendTemplateLog);

    {
        int usersPerSecond = Integer.getInteger("usersPerSecond");
        int testDuration = Integer.getInteger("testDuration");
        int rpsRampUpDuration = Integer.getInteger("rpsRampUpDuration");
        int holdDuration = testDuration - rpsRampUpDuration;
        int targetRps = Integer.getInteger("targetRps");
        setUp(
                loadScenario.injectOpen(
                        CoreDsl.atOnceUsers(usersPerSecond),
                        CoreDsl.nothingFor(Duration.ofSeconds(testDuration))
                ).throttle(
                        CoreDsl.reachRps(targetRps)
                                .in(Duration.ofSeconds(rpsRampUpDuration)),
                        CoreDsl.holdFor(Duration.ofSeconds(holdDuration))
                ).protocols(httpProtocol)
        );
    }
}

//
