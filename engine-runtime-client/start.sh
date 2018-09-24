#!/bin/bash

JAR=target/engine-runtime-client-jar-with-dependencies.jar
PACKAGE=hu.mktiti.cirkus.runtime.base.ClientRuntimeKt

HOST_IN="$1"
PORT_IN="$2"

HOST=${HOST_IN:-localhost}
PORT=${PORT_IN:-12345}

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

echo "Starting client"
KREATOR_PROPS_SOCKET_HOST=${HOST} KREATOR_PROPS_SOCKET_PORT=${PORT} KREATOR_PROPS_THREAD_PREFIX=Engine java -cp ${JAR} ${PACKAGE}