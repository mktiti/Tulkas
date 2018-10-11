#!/bin/bash

JAR=target/engine-runtime-client-jar-with-dependencies.jar

HOST_IN="$1"
PORT_IN="$2"
LOG_PATH_IN="$3"

HOST=${HOST_IN:-localhost}
PORT=${PORT_IN:-12345}
LOG_PATH=${LOG_PATH_IN:-log.txt}

IS_MATCH=""
if [ $4 = "match" ]; then
   IS_MATCH="true"
else
  IS_MATCH="false"
fi

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
export KREATOR_PROPS_SOURCE=env-var
export KREATOR_PROPS_PREFIX=TULKAS_
export TULKAS_SOCKET_HOST=${HOST}
export TULKAS_SOCKET_PORT=${PORT}
export TULKAS_IS_MATCH=${IS_MATCH}
export TULKAS_THREAD_PREFIX=Engine
export TULKAS_LOG_PATH=${LOG_PATH}
export TULKAS_READABLE_FILES=/dev/random:/dev/urandom

echo "Starting client"
java -Dlogback.configurationFile=logback.xml -jar ${JAR}