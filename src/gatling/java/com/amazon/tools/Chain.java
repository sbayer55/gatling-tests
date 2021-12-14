package com.amazon.tools;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.http.HttpDsl;

public final class Chain {
    private Chain() {
    }

    public static ChainBuilder sendApacheCommonLogPostRequest(int batchSize) {
        return sendApacheCommonLogPostRequest("Post log", batchSize);
    }

    public static ChainBuilder sendApacheCommonLogPostRequest(String name, int batchSize) {
        return CoreDsl.exec(
                HttpDsl.http(name)
                        .post("/log/ingest")
                        .body(CoreDsl.StringBody(Templates.apacheCommonLogTemplate(batchSize))));
    }
}
