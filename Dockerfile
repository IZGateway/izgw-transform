FROM ghcr.io/izgateway/alpine-node-openssl-fips:latest

RUN apk update && \
    apk upgrade --no-cache && \
    apk add --no-cache openjdk17-jre

# Define arguments (set in izgw-transform pom.xml)
ARG JAR_FILENAME
ARG XFORM_VERSION

# Ports
EXPOSE 444
EXPOSE 9082
# Remote debugging
EXPOSE 8000

COPY docker/data/filebeat.yml /usr/share/izg-transform/

# Install logrotate
RUN rm /etc/logrotate.conf
COPY docker/data/logrotate.conf /etc/logrotate.conf
RUN (crontab -l 2>/dev/null; echo "*/15 * * * * /etc/periodic/daily/logrotate") | crontab -

WORKDIR /

# Install filebeat
# Rename default dnsmasq file to make sure dnsmasq does not read its entries
RUN rm -f /filebeat/filebeat.yml && \
    cp /usr/share/izg-transform/filebeat.yml /filebeat/ && \
    mv /etc/dnsmasq.conf /etc/dnsmasq.conf.bkup && \
    echo 'cache-size=10000' > /etc/dnsmasq.conf

# This is fialing - need to debug RUN rm -f /metricbeat/metricbeat.yml && cp /usr/share/izgateway/metricbeat.yml /metricbeat/

# Set working directory
WORKDIR /usr/share/izg-transform
COPY docker/data/lib/*.jar lib/

# Add izgw-transform jar file
ADD target/$JAR_FILENAME app.jar

# add script to run
COPY docker/fatjar-run.sh run1.sh

# Remove carriage returns from batch file (for build on WinDoze).
RUN tr -d '\r' <run1.sh >run.sh && \
    rm run1.sh

# Add izgw-transform jar file
ADD target/$JAR_FILENAME app.jar

# Make scripts executable
RUN ["chmod", "u+r+x", "run.sh"]

ENV XFORM_VERSION=$XFORM_VERSION

ENTRYPOINT ["sh","-c","bash run.sh app.jar"]
