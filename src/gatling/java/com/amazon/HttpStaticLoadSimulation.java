package com.amazon;

import com.amazon.tools.Chain;
import com.amazon.tools.Protocol;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

public class HttpStaticLoadSimulation extends Simulation {
    private static final Duration testDuration =  Duration.ofMinutes(5);

    ScenarioBuilder httpStaticLoad = CoreDsl.scenario("Http Static Load")
            .during(testDuration)
            .on(Chain.sendApacheCommonLogPostRequest("Average log post request", 20));

    {
        setUp(httpStaticLoad.injectOpen(
                CoreDsl.rampUsers(10).during(Duration.ofSeconds(10)),
                CoreDsl.atOnceUsers(10)
        ))
                .protocols(Protocol.httpProtocol())
                .maxDuration(testDuration)
                .assertions(
                        CoreDsl.global().failedRequests().percent().lt(1.0),
                        CoreDsl.global().responseTime().mean().lt(200),
                        CoreDsl.global().responseTime().max().lt(1000)
                );
    }
}
