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

public class BasicSimulation extends Simulation {
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
                            .body(CoreDsl.StringBody(Templates.template)));

    ChainBuilder sendTemplateLogRepeatedly = CoreDsl.repeat(10).on(sendTemplateLog);

    ChainBuilder sendSingleLogFile = CoreDsl.exec(
            HttpDsl.http("Post log")
                    .post("/log/ingest")
                    .body(CoreDsl.ElFileBody("bodies/singleLog.json"))
                    .asJson()
                    .check(HttpDsl.status().is(200)));

    ChainBuilder sendLargeLogFile = CoreDsl.exec(
            HttpDsl.http("Post log")
                    .post("/log/ingest")
                    .body(CoreDsl.ElFileBody("bodies/largeLog.json"))
                    .asJson()
                    .check(HttpDsl.status().is(200)));

    ScenarioBuilder basicScenario = CoreDsl.scenario("Data Prepper Load Test")
            .exec(sendTemplateLog);

    ScenarioBuilder loadScenario = CoreDsl.scenario("Fixed Duration Load Simulation")
            .forever()
            .on(sendTemplateLog);

    {
        int initialLoad = Integer.getInteger("initialLoad");
        int pauseDuration = Integer.getInteger("pauseDuration", 5);
        int peakLoad = Integer.getInteger("peakLoad");
        int rampUpDuration = Integer.getInteger("rampUpDuration");
        int peakLoadDuration = Integer.getInteger("peakLoadDuration");

        setUp(
                loadScenario.injectOpen(
                        CoreDsl.atOnceUsers(initialLoad),
                        CoreDsl.nothingFor(Duration.ofSeconds(pauseDuration)),
                        CoreDsl.atOnceUsers(initialLoad),
                        CoreDsl.rampUsers(peakLoad).during(Duration.ofSeconds(rampUpDuration))
                ).protocols(httpProtocol)
        ).maxDuration(Duration.ofSeconds(pauseDuration + rampUpDuration + peakLoadDuration))
                .assertions(
                        CoreDsl.global().responseTime().mean().lt(1000), // 100ms
                        CoreDsl.global().successfulRequests().percent().gt(95.0)
                );
    }
}
