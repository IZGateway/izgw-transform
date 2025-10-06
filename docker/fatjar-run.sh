#!/bin/bash

cd /usr/share/izg-transform || exit

# Get Service Name from AWS for log reporting
SERVICE_NAME=unknown
if [[ $ECS_CONTAINER_METADATA_URI_V4 ]]; then
    SERVICE_NAME=$(curl -s $ECS_CONTAINER_METADATA_URI_V4 | jq -r '.Labels["com.amazonaws.ecs.container-name"]')
fi
export SERVICE_NAME

# Start filebeat and metricbeat if API key is provided
if [[ $ELASTIC_API_KEY ]]; then
    if command -v filebeat >/dev/null 2>&1; then
        filebeat -e &
        echo "Started Filebeat"
    else
        echo "ERROR: filebeat not found"
    fi
    
    if command -v metricbeat >/dev/null 2>&1; then
        metricbeat -e &
        echo "Started MetricBeat"
    else
        echo "ERROR: metricbeat not found"
    fi
else
    echo "Elastic logging not enabled"
fi

# Monitor beats and exit container if they stop unexpectedly
monitor_beats() {
    while true; do
        sleep 30
        if [[ $ELASTIC_API_KEY ]]; then
            if ! pgrep -f "filebeat" > /dev/null; then
                echo "Filebeat process has died unexpectedly. Stopping Transform serviceand container."
                pkill -f "java.*app.jar" 2>/dev/null
                exit 1
            fi
            if ! pgrep -f "metricbeat" > /dev/null; then
                echo "Metricbeat process has died unexpectedly. Stopping Transform service and container."
                pkill -f "java.*app.jar" 2>/dev/null
                exit 1
            fi
        fi
    done
}

jarfilename=$1

#Save original nameserver in temp file and empty contents
echo nameserver 127.0.0.1 > /tmp/newresolv.conf
cat /etc/resolv.conf >> /tmp/newresolv.conf
truncate -s 0 /etc/resolv.conf
cat /tmp/newresolv.conf > /etc/resolv.conf

#Start dnsmasq as root
dnsmasq  --use-stale-cache=0 --log-queries=extra --user=root --log-facility=/var/log/dnsmasq.log

# Enable remote debugging if DEBUG is set in the environment
JAVA_TOOL_OPTS=
if [[ $DEBUG ]]
then
    # To enable remote debugging, set JAVA_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=*:8000,server=y,suspend=n
    # NOTE: The * below is important.  It enables your debugger to attach through whatever IP Address Docker is using
    # to recieve connections on port 8000.  If not used, then only connections from 127.0.0.1 are accepted, and while
    # you may think your IP Address of the host is 127.0.0.1, it's not inside the container.  Change suspend=n to suspend=y to
    # debug application startup, but revert to suspend=n before final checkin.
    JAVA_TOOL_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000
fi

# Start monitoring in background if logging is enabled
if [[ $ELASTIC_API_KEY ]]; then
    monitor_beats &
    echo "Started beat monitoring"
fi

# Start Java application
java $JAVA_OPTS $JAVA_TOOL_OPTS -javaagent:lib/aspectjweaver-1.9.22.jar -javaagent:lib/spring-instrument-5.3.8.jar \
   -XX:+CreateCoredumpOnCrash -cp "./lib/bcfips/*" \
   --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
   --add-opens=java.base/java.net=ALL-UNNAMED \
   --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED \
   -Dorg.bouncycastle.fips.approved_only=true \
   -Dorg.bouncycastle.jsse.client.dh.unrestrictedGroups=true \
   -Djavax.net.ssl.trustStorePassword=changeit \
   -Xms4g \
   -Xmx8g \
   -Djava.library.path=lib \
   -jar $jarfilename &

JAVA_PID=$!
echo "Started Java application with PID: $JAVA_PID"

# Wait for the Java process to finish
wait $JAVA_PID

