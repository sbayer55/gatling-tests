package com.amazon;

import com.amazon.tools.Chain;
import com.amazon.tools.Protocol;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

public class RampUpSimulation extends Simulation {
    private static final Integer largeBatchSize = 200;
    private static final Integer rampUsers = 40;
    private static final Duration rampUpTime = Duration.ofSeconds(30);
    private static final Duration peakLoadTime = Duration.ofMinutes(10);

    private static PopulationBuilder rampUpPopulation(ProtocolBuilder protocol) {
        return CoreDsl.scenario("Ramp Up using " + protocol.protocol() + " protocol Scenario")
                .exec(Chain.sendApacheCommonLogPostRequest("Post logs with large batch", largeBatchSize))
                .during(peakLoadTime)
                .on(Chain.sendApacheCommonLogPostRequest("Post logs with large batch", largeBatchSize))
                .injectOpen(
                        CoreDsl.rampUsers(rampUsers).during(rampUpTime),
                        CoreDsl.nothingFor(peakLoadTime)
                ).protocols(protocol);
    }


    ScenarioBuilder rampUpScenario = CoreDsl.scenario("Ramp Up Scenario")
            .during(peakLoadTime)
            .on(Chain.sendApacheCommonLogPostRequest("Post logs with large batch", largeBatchSize));

    {
        setUp(
                rampUpScenario.injectOpen(
                        CoreDsl.rampUsers(rampUsers).during(rampUpTime)
                )
        ).protocols(
                Protocol.httpProtocol()
        ).assertions(
                CoreDsl.global().failedRequests().percent().lt(1.0),
                CoreDsl.global().responseTime().mean().lt(600),
                CoreDsl.global().responseTime().max().lt(10000),
                CoreDsl.global().requestsPerSec().gt(100.0)
        );
    }
}
