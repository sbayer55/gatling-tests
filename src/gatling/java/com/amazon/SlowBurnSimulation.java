package com.amazon;

import com.amazon.tools.Chain;
import com.amazon.tools.Protocol;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

public class SlowBurnSimulation extends Simulation {
    private static final Integer largeBatchSize = 200;
    private static final Integer rampUsers = 10;
    private static final Duration rampUpTime = Duration.ofMinutes(1);
    private static final Duration peakDuration = Duration.ofMinutes(1);

    ScenarioBuilder rampUpScenario = CoreDsl.scenario("Slow Burn")
            .forever()
            .on(Chain.sendApacheCommonLogPostRequest("Post logs with large batch", largeBatchSize));

    {
        setUp(
                rampUpScenario.injectOpen(
                        CoreDsl.rampUsers(rampUsers).during(rampUpTime),
                        CoreDsl.nothingFor(peakDuration)
                )
        )
                .maxDuration(rampUpTime.plus(peakDuration))
                .protocols(Protocol.httpProtocol());
    }
}
