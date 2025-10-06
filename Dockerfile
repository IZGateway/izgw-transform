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
COPY docker/data/metricbeat.yml /usr/share/izg-transform/

# Install logrotate
RUN rm /etc/logrotate.conf
COPY docker/data/logrotate.conf /etc/logrotate.conf
RUN (crontab -l 2>/dev/null; echo "*/15 * * * * /etc/periodic/daily/logrotate") | crontab -

WORKDIR /
# Install tini
RUN apk add --no-cache tini
# Install filebeat
# Install metricbeat
# Rename default dnsmasq file to make sure dnsmasq does not read its entries
RUN rm -f /filebeat/filebeat.yml && \
    cp /usr/share/izg-transform/filebeat.yml /filebeat/ && \
    rm -f /metricbeat/metricbeat.yml && \
    cp /usr/share/izg-transform/metricbeat.yml /metricbeat/ && \
    mv /etc/dnsmasq.conf /etc/dnsmasq.conf.bkup && \
    echo 'cache-size=10000' > /etc/dnsmasq.conf

# Set working directory
WORKDIR /usr/share/izg-transform
RUN mkdir module

# Copy BC-FIPS Jars
# This ensures we only use NIST certified publicly available packages
COPY docker/data/lib/bcfips/*.jar /usr/share/izg-transform/lib/bcfips/

# Copy aspectjweaver and spring-instrument jars
COPY docker/data/lib/*.jar /usr/share/izg-transform/lib/

# Copy docs/quickstart to the image
COPY docs/quickstart /usr/share/izg-transform/quickstart

# Add izgw-transform jar file
ADD target/$JAR_FILENAME app.jar

# add script to run
COPY docker/fatjar-run.sh run1.sh

# Remove carriage returns from runs script (for build on WinDoze).
RUN tr -d '\r' <run1.sh >run.sh && \
    rm run1.sh

# Make scripts executable
RUN ["chmod", "u+r+x", "run.sh"]

ENV XFORM_VERSION=$XFORM_VERSION

ENTRYPOINT ["/sbin/tini", "--", "sh", "-c", "crond && exec bash run.sh app.jar"]
