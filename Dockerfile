FROM ghcr.io/izgateway/alpine-node-openssl-fips:latest

# Install Tini and Java
RUN apk update && \
    apk upgrade --no-cache && \
    apk add --no-cache tini && \
    apk add --no-cache openjdk21-jre --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community

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

# add scripts
COPY docker/fatjar-run.sh run1.sh
COPY docker/docker-entrypoint.sh entrypoint1.sh
COPY docker/generate-token.js generate-token.js

# Remove carriage returns from scripts (for build on Windows).
RUN tr -d '\r' <run1.sh >run.sh && rm run1.sh && \
    tr -d '\r' <entrypoint1.sh >entrypoint.sh && rm entrypoint1.sh

# Make scripts executable
RUN chmod u+rx run.sh entrypoint.sh

# Generate self-signed BCFKS keystore for local testing (CN follows IZ Gateway local-test convention).
# Engineers running the production image supply their own keystore via XFORM_CRYPTO_STORE_* env vars.
RUN mkdir -p /ssl/local && \
    keytool -genkeypair \
        -alias server \
        -keyalg RSA -keysize 2048 -validity 3650 \
        -keystore /ssl/local/server.bcfks \
        -storepass changeit \
        -keypass changeit \
        -storetype BCFKS \
        -providername BCFIPS \
        -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider \
        -providerpath /usr/share/izg-transform/lib/bcfips/bc-fips-2.1.2.jar \
        -dname "CN=sql.xform.testing.local, O=izgateway" \
        -noprompt && \
    cp /ssl/local/server.bcfks /ssl/local/trust.bcfks

ENV XFORM_VERSION=$XFORM_VERSION

ENTRYPOINT ["/sbin/tini", "--", "/usr/share/izg-transform/entrypoint.sh"]
