package com.amazon;

import com.amazon.tools.Chain;
import com.amazon.tools.Protocol;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

public class RampUpSimulation extends Simulation {
    private static final Integer largeBatchSize = 200;
    private static final Integer rampUsers = 40;
    private static final Duration rampUpTime = Duration.ofSeconds(30);
    private static final Duration peakLoadTime = Duration.ofMinutes(10);

    ScenarioBuilder rampUpScenario = CoreDsl.scenario("Ramp Up Scenario")
            .forever()
            .on(Chain.sendApacheCommonLogPostRequest("Post logs with large batch", largeBatchSize));

    {
        setUp(rampUpScenario.injectOpen(
                CoreDsl.rampUsers(rampUsers).during(rampUpTime),
                CoreDsl.nothingFor(peakLoadTime)
        )).protocols(Protocol.httpProtocol());
    }
}
