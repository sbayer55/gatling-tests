package com.amazon;

import com.amazon.tools.Protocol;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpDsl;

public class SingleRequestSimulation extends Simulation {
    ChainBuilder sendSingleLogFile = CoreDsl.exec(
            HttpDsl.http("Post log")
                    .post("/log/ingest")
                    .body(CoreDsl.ElFileBody("bodies/singleLog.json"))
                    .asJson()
                    .check(HttpDsl.status().is(200), CoreDsl.responseTimeInMillis().lt(200))
            );

    ScenarioBuilder basicScenario = CoreDsl.scenario("Post static json log file")
            .exec(sendSingleLogFile);


    {

        setUp(
                basicScenario.injectOpen(CoreDsl.atOnceUsers(1))
        ).protocols(
                Protocol.httpProtocol()
        ).assertions(
                CoreDsl.global().responseTime().mean().lt(100),
                CoreDsl.global().successfulRequests().percent().is(100.0)
        );
    }
}
