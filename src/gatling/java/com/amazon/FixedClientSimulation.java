package com.amazon;

import com.amazon.tools.Chain;
import com.amazon.tools.Protocol;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

public class FixedClientSimulation extends Simulation {
    private static final Integer largeBatchSize = 200;
    private static final Integer users = 10;
    private static final Duration duration = Duration.ofMinutes(5);

    ScenarioBuilder fixedScenario = CoreDsl.scenario("Slow Burn")
            .during(duration)
            .on(Chain.sendApacheCommonLogPostRequest("Post logs with large batch", largeBatchSize));

    {
        setUp(fixedScenario.injectOpen(CoreDsl.atOnceUsers(users)))
                .protocols(Protocol.httpProtocol())
                .assertions(CoreDsl.global().requestsPerSec().gt(140.0));
    }
}
