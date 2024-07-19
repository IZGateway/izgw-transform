#!/bin/bash

cd /usr/share/izg-transform || exit

jarfilename=$1

# Enable remote debugging if DEBUG is set in the environment
JAVA_TOOL_OPTS=
if [[ $DEBUG ]]
then
    # To enable remote debugging, set JAVA_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=*:8000,server=y,suspend=n
    # NOTE: The * below is important.  It enables your debugger to attach through whatever IP Address Docker is using
    # to recieve connections on port 8000.  If not used, then only connections from 127.0.0.1 are accepted, and while
    # you may think your IP Address of the host is 127.0.0.1, it's not inside the container.
    JAVA_TOOL_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address="*:8000"
fi

# If an API Key has been given, then send data to elastic search.
if [[ $ELASTIC_API_KEY ]]
then
    filebeat -e &
    echo Started Filebeat
else
    echo Filebeat logging not enabled
fi


#Save original nameserver in temp file and empty contents
echo nameserver 127.0.0.1 > /tmp/newresolv.conf
cat /etc/resolv.conf >> /tmp/newresolv.conf
truncate -s 0 /etc/resolv.conf
cat /tmp/newresolv.conf > /etc/resolv.conf

#Start dnsmasq as root
dnsmasq  --use-stale-cache=0 --log-queries=extra --user=root --log-facility=/var/log/dnsmasq.log

java $JAVA_OPTS $JAVA_TOOL_OPTS -javaagent:lib/aspectjweaver-1.9.22.jar \
   -Xms4g \
   -Xmx8g \
   -Djava.library.path=lib \
   -jar $jarfilename

