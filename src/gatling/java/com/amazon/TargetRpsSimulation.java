package com.amazon;

import com.amazon.tools.Chain;
import com.amazon.tools.Protocol;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;


public class TargetRpsSimulation extends Simulation {
    private static final Integer smallBatchSize = 50;
    private static final Integer largeBatchSize = 200;
    private static final Duration delayTime = Duration.ofSeconds(30);
    private static final Duration rampUpTime = Duration.ofSeconds(30);
    private static final Duration peakLoadTime = Duration.ofMinutes(10);
    private static final Integer maxUsersPerSecond = 200;


    ScenarioBuilder smallBatchScenario = CoreDsl.scenario("Small Batch Scenario")
            .forever()
            .on(Chain.sendApacheCommonLogPostRequest("Post logs with small batch", smallBatchSize));

    ScenarioBuilder largeBatchScenario = CoreDsl.scenario("Large Batch Scenario")
            .forever()
            .on(Chain.sendApacheCommonLogPostRequest("Post logs with large batch", largeBatchSize));

    private static PopulationBuilder runScenarioWithTargetRps(ScenarioBuilder scenario, Integer targetRps) {
        return scenario.injectOpen(
                CoreDsl.nothingFor(delayTime),
                CoreDsl.atOnceUsers(maxUsersPerSecond),
                CoreDsl.nothingFor(peakLoadTime)
        ).throttle(
                CoreDsl.reachRps(targetRps).in(rampUpTime),
                CoreDsl.holdFor(peakLoadTime)
        ).protocols(Protocol.httpProtocol());
    }

    {
        setUp(
                runScenarioWithTargetRps(smallBatchScenario, 400),
                runScenarioWithTargetRps(smallBatchScenario, 500),
                runScenarioWithTargetRps(smallBatchScenario, 600),
                runScenarioWithTargetRps(largeBatchScenario, 100)
        );
    }
}
