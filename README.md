To run all Simulation:

```
./gradlew clean gatlingRun
```


To run TargetRps Simulation

```
./gradlew clean gatlingRun-com.amazon.TargetRpsSimulation
```

Simulations found in `./src/gatling/java/com/amazon`

# Recommended Gatling Documentation
[Gatling Quickstart](https://gatling.io/docs/gatling/tutorials/quickstart/)
[Gatling Advanced Simulations](https://gatling.io/docs/gatling/tutorials/advanced/)
[Passing Command Line Parameters](https://gatling.io/docs/gatling/guides/passing_parameters/)
[Gatling Gradle Plugin](https://gatling.io/docs/gatling/reference/current/extensions/gradle_plugin/)

# Command Line Parameters

Creating command line parameters:

```java
// Integer
private static final Integer targetRps = Integer.getInteger("targetRps");
// Integer with default value
private static final Integer maxUsers = Integer.getInteger("maxUsers", 1000);
// String with default value
private static final String targetHost = System.getProperty("targetHost", "http://localhost");
```

Setting parameters on run:

```java
./gradlew clean gatlingRun -DtargetRps=100 -DmaxUsers=10 -DtargetHost=127.0.0.1
```

Setting Java Options

```shell
export _JAVA_OPTIONS="-Xms8g -Xmx8g"
./gradlew clean gatlingRun
```

# Test Components

## Protocol

Defines a protocol (http, https, jdbc) that can be used when setting up a scenario. _Note_ it is possible to set up a scenario multiple times each with different protocols. The following is an example of creating a http protocol.

```java
HttpProtocolBuilder httpProtocol = HttpDsl.http
        .baseUrl("http://localhost")
        .acceptHeader("application/json")
        .header("Content-Type", "application/json");
```

## Chain

A Chain defines a set of exec steps such as sending a http rest request or executing a set of requests to log in. Each exec step in a chain can have individual assertions. The following is an example of a chain that sends a get request expecting a 202 response within 200ms, then sends a post request expecting a 200 response.

```java
ChainBuilder getThenPost = CoreDsl.exec(
        HttpDsl.http("Send Get")
                .get("/url/suffix")
                .check(
                        HttpDsl.status().is(202),
                        CoreDsl.responseTimeInMillis().lt(200)
                )
        )
        .exec(
                HttpDsl.http("Post log")
                    .post("/log/ingest")
                    .body(CoreDsl.ElFileBody("bodies/singleLog.json"))
                    .asJson()
                    .check(HttpDsl.status().is(200))
        );
```

## Template

Templates are static functions that allow you to generate dynamic content for Chain exec steps. The following example is of a template that generates a String containing a JSON document with a dynamic value.

```java
public static final String customValue(Session session) {
        String value = System.getProperty("value", "default");
        return "{\"CustomValue\": \"" + value + "\"}";
}
```

Example of a chain using the customValue template.

```java
ChainBuilder postTemplate = CoreDsl.exec(HttpDsl.http("Post with template")
        .post("/log/ingest")
        .body(customValue));
);
```

## Scenario

A scenario is a named set of Chains to execute. The following is an example scenario to login, open the landing page, then navigate to the users profile. 

```java
ChainBuilder login = null; // TODO
ChainBuilder getLandingPage = null; // TODO
ChainBuilder navigateToUserProfile = null; // TODO

ScenarioBuilder users = CoreDsl.scenario("Login and navigate to user profile")
        .exec(login, getLandingPage, navigateToUserProfile);
```

## Simulation

A simulation is a self-contained performance test that run a set of scenarios with user, request, and duration constrains with assertion criteria to determine a pass / fail for the scenario. The following is a simulation that will simulate 10 concurrent users attempting to execute a login scenario. For the test to pass all requests must receive a success response code within 200 ms.

```java
setUp(
        loginScenario.injectOpen(
                CoreDsl.atOnceUsers(10)
        ).protocols(httpProtocol)
).assertions(
        CoreDsl.global().responseTime()..mean().lte(200),
        CoreDsl.global().successfulRequests().percent().is(100.0)
);
```

Here is a scenario where the user count will increase at a linear rate over 10 minutes from 0 to 100 then sustain peak load for 5 minutes.

```java
setUp(
        simpleScenario.injectOpen(
                CoreDsl.rampUsers(100).during(Duration.ofMinutes(10)),
                CoreDsl.nothingFor(Duration.ofMinutes(5))
        ).protocols(httpProtocol)
);
```

This scenario will throttle the number of requests sent from a maximum of 0 to 100 over 5 minutes then to a maximum of 100 request / second for an additional 5 minutes. 

In this scenario think of the number of users as a maximum number of concurrent requests. If the target RPS is hit before each user sends a request the remaining users will be queued to send a request after a response is received. It is possible the target RPS will not be achieved if the service under load is not able to maintain the target RPS. For example if the target service has a constant response time of 1 second and the maximum number of users is 10, the maximum achievable RPS is 10. 

The ideal maximum number of users is the (`target RPS` * `the target service minimum response time`). This configuration will guarantee that target RPS is achievable if the target service is capable.

```java
setUp(
        loginScenario.injectOpen(
                CoreDsl.atOnceUsers(10),
                CoreDsl.nothingFor(Duration.ofMinutes(10))
        )
).throttle(
        CoreDsl.reachRps(100).in(Duration.ofMinutes(5)),
        CoreDsl.holdFor(Duration.ofMinutes(5))
).protocols(httpProtocol);
```
