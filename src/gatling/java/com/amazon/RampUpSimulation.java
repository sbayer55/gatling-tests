package com.amazon;

import com.amazon.tools.Chain;
import com.amazon.tools.Protocol;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;
import java.util.stream.Collectors;

public class RampUpSimulation extends Simulation {
    private static final Integer largeBatchSize = 200;
    private static final Integer rampUsers = 40;
    private static final Duration rampUpTime = Duration.ofSeconds(30);
    private static final Duration peakLoadTime = Duration.ofMinutes(10);

    ScenarioBuilder rampUpScenario = CoreDsl.scenario("Ramp Up Scenario")
            .forever()
            .on(Chain.sendApacheCommonLogPostRequest("Post logs with large batch", largeBatchSize));

    {
        setUp(Protocol.allProtocols.stream()
                .map(protocol ->
                        rampUpScenario.injectOpen(
                                CoreDsl.rampUsers(rampUsers).during(rampUpTime)
                        ).protocols(protocol))
                .collect(Collectors.toList())
        ).assertions(
                CoreDsl.global().failedRequests().percent().lt(1.0),
                CoreDsl.global().responseTime().mean().lt(600),
                CoreDsl.global().responseTime().max().lt(10000),
                CoreDsl.global().requestsPerSec().gt(100.0)
        );
    }
}
