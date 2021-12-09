To run all Simulation:

```
./gradlew \
    gatlingRun \
    -DinitialLoad=1 \
    -DpauseDuration=1 \
    -DpeakLoad=1 \
    -DrampUpDuration=1 \
    -DpeakLoadDuration=1 \
    -DusersPerSecond=200 \
    -DtargetHost=http://75.101.195.88:2021 \
    -DrpsRampUpDuration=30 \
    -DtestDuration=600 \
    -DtargetRps=400 \
    -DlogsPerRequest=50
```


To run TargetRps Simulation

```./gradlew \
    gatlingRun-com.amazon.TargetRps \
    -DusersPerSecond=200 \
    -DtargetHost=http://75.101.195.88:2021 \
    -DrpsRampUpDuration=30 \
    -DtestDuration=600 \
    -DtargetRps=400 \
    -DlogsPerRequest=50
```