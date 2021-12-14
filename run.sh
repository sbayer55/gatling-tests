export _JAVA_OPTIONS="-Xms8g -Xmx8g"

export targetHost="http://localhost:2021"

export usersPerSecond=200
export rpsRampUpDuration=30
export testDuration=600

#export targetRps=400
#export logsPerRequest=50

function post_test() {
    aws s3 cp --recursive \
    /home/ec2-user/gatling/build/reports/gatling \
    s3://log-ingestion-performance-results > /dev/null

    echo "Report name:"
    ls /home/ec2-user/gatling/build/reports/gatling/
    echo "================================="

    echo "Starting Sleep"
    sleep 300s
    echo "Sleep over"
}

function run_target_rps_test() {
    ./gradlew \
        clean \
        gatlingRun-com.amazon.TargetRps \
        -DusersPerSecond=$usersPerSecond \
        -DtargetHost=$targetHost \
        -DrpsRampUpDuration=$rpsRampUpDuration \
        -DtestDuration=$testDuration \
        -DtargetRps=$targetRps \
        -DlogsPerRequest=$logsPerRequest


    echo "================================="
    echo "usersPerSecond=$usersPerSecond"
    echo "targetHost=$targetHost"
    echo "rpsRampUpDuration=$rpsRampUpDuration"
    echo "testDuration=$testDuration"
    echo "targetRps=$targetRps"
    echo "logsPerRequest=$logsPerRequest"
    echo "================================="

    post_test
}

function run_basic_simulation_test() {
    ./gradlew \
        clean \
        gatlingRun-com.amazon.BasicSimulation \
        -DtargetHost=$targetHost \
        -DinitialLoad=$initialLoad \
        -DpauseDuration=$pauseDuration \
        -DpeakLoad=$peakLoad \
        -DrampUpDuration=$rampUpDuration \
        -DpeakLoadDuration=$peakLoadDuration \
        -DlogsPerRequest=$logsPerRequest

    echo "================================="
    echo "targetHost = ${targetHost}"
    echo "initialLoad = ${initialLoad}"
    echo "pauseDuration = ${pauseDuration}"
    echo "peakLoad = ${peakLoad}"
    echo "rampUpDuration = ${rampUpDuration}"
    echo "peakLoadDuration = ${peakLoadDuration}"
    echo "logsPerRequest = ${logsPerRequest}"
    echo "================================="

    post_test
}


export targetRps=400
export logsPerRequest=50

run_target_rps_test

export targetRps=100
export logsPerRequest=200

run_target_rps_test

export targetRps=500
export logsPerRequest=50

run_target_rps_test

export targetRps=600
export logsPerRequest=50

run_target_rps_test

export initialLoad=20
export pauseDuration=0
export peakLoad=40
export rampUpDuration=180
export peakLoadDuration=600
export logsPerRequest=200

run_basic_simulation_test


echo "Done!"