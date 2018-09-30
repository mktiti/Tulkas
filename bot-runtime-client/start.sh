#!/bin/bash

JAR=target/bot-runtime-client-jar-with-dependencies.jar
PACKAGE=hu.mktiti.cirkus.runtime.base.ClientRuntimeKt

HOST_IN="$1"
PORT_IN="$2"
LOG_PATH_IN="$3"

HOST=${HOST_IN:-localhost}
PORT=${PORT_IN:-12345}
LOG_PATH=${LOG_PATH_IN:-log.txt}

echo "Host: $HOST, Port: $PORT"
echo "Working directory: $(pwd)"

# set working directory to script location
cd "$(dirname "$0")"

if [ ! -f ${JAR} ]; then
    echo "Jar file [$JAR] not found, attempting rebuild"
    mvn clean build
fi

if [ ! -f ${JAR} ]; then
    echo "Jar file ($JAR) still not found, possible build error, exiting"
fi

# set environment variables
export KREATOR_PROPS_PREFIX=CIRKUS_
export CIRKUS_SOCKET_HOST=${HOST}
export CIRKUS_SOCKET_PORT=${PORT}
export CIRKUS_THREAD_PREFIX=Bot
export CIRKUS_LOG_PATH=${LOG_PATH}

echo "Starting client"
java -Dlogback.configurationFile=logback.xml -cp ${JAR} ${PACKAGE}