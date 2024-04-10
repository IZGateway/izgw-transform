FROM ghcr.io/izgateway/alpine-node-openssl-fips:latest

RUN apk update
RUN apk add --no-cache openjdk17-jre

# Define arguments (set in izgw-transform pom.xml)
ARG JAR_FILENAME

# Ports - TODO update to proper ports as application takes shape
EXPOSE 8080
# Remote debugging
EXPOSE 8000

#Rename default dnsmasq file to make sure dnsmasq does not read its entries
RUN mv /etc/dnsmasq.conf /etc/dnsmasq.conf.bkup
RUN echo 'cache-size=10000' > /etc/dnsmasq.conf

# Set working directory
WORKDIR /usr/share/izg-transform

# Add izgw-transform jar file
ADD target/$JAR_FILENAME app.jar

# add script to run
ADD docker/fatjar-run.sh run1.sh

# Remove carriage returns from batch file (for build on WinDoze).
RUN tr -d '\r' <run1.sh >run.sh
RUN rm run1.sh

# Add izgw-transform jar file
ADD target/$JAR_FILENAME app.jar

# Ensure we only use NIST certified publicly available BC-FIPS packages
ADD docker/data/fips/bc-fips-1.0.2.4.jar bc-fips-1.0.2.4.jar
ADD docker/data/fips/bcpkix-fips-1.0.7.jar bcpkix-fips-1.0.7.jar
ADD docker/data/fips/bctls-fips-1.0.16.jar bctls-fips-1.0.16.jar

# Make scripts executable
RUN ["chmod", "u+r+x", "run.sh"]


ENTRYPOINT ["sh","-c","bash run.sh app.jar"]
