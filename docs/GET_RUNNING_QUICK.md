&larr;[Back to README](../README.md)

---

# Get Running Quick

This document aims to get Transformation Service up and running in minutes. Leaving you with a system that you can interact with locally to get an idea of its capabilities and purpose. There will be links to deeper documentation about the system for continued use and development.

## Prerequisites

- Java 17
- Maven 3.6+
- Git
- GitHub Account
- Docker

### Maven Configuration

The Transformation Service currently has two dependencies on other IZ Gateway libraries: IZGW Core and v2tofhir. These are obtained via GitHub packages. At this time it is necessary to configure Maven in order to access GitHub packages. Please see [Maven GitHub Packages Access](./MAVEN_GITHUB_PACKAGES.md) for details on this configuration.

## Up And Running Quickly—Local

### 1. Clone repository

```shell
git clone git@github.com:IZGateway/izgw-transform.git
cd izgw-transform
```

### 2. Build JAR file

```shell
mvn clean package
```

### 3. Run Transformation Service

```shell
java -javaagent:docker/data/lib/aspectjweaver-1.9.22.jar \
     -javaagent:docker/data/lib/spring-instrument-5.3.8.jar \
     -Dspring.config.import=file:./docs/quickstart.properties \
     -DSSL_SHARE=./target \
     -DXFORM_CONFIGURATIONS_DIRECTORY=./docs/quickstart/configuration \
     -DCOMMON_PASS=XFORM_TESTING_COMMON_PASS \
     -jar target/xform-0.8.0.jar
```

The Transformation Service is up and ready when you see log entries on the console:

```json
{"@timestamp":"2025-06-17T21:36:51.262972-04:00","@version":"1","message":"Xform application loaded","logger_name":"gov.cdc.izgateway.xform.Application","thread_name":"Xform Service","level":"INFO","level_value":20000}
{"@timestamp":"2025-06-17T21:36:51.263036-04:00","@version":"1","message":"Build: xform-0.8.0-202506172057","logger_name":"gov.cdc.izgateway.xform.Application","thread_name":"Xform Service","level":"INFO","level_value":20000}
```

## Up and Running Quickly—Docker

### 1. Clone repository

```shell
git clone git@github.com:IZGateway/izgw-transform.git
cd izgw-transform
```

### 2. Build Project & Docker Image

```shell
mvn clean install
```

This will build the code and then build and tag a Docker image locally as izgw-transform:latest.

### 3. Run Transformation Service Docker Container

```shell
docker run --name=local-xform \
-e SSL_SHARE=/target \
-e XFORM_CONFIGURATIONS_DIRECTORY=/docs/quickstart/configuration \
-e COMMON_PASS=XFORM_TESTING_COMMON_PASS \
--volume=./target:/target \
--volume=./docs:/docs \
-p 444:444 \
izgw-transform:latest
```

Similar to running locally, you should see these log entries on the console:

```json
{"@timestamp":"2025-06-17T21:36:51.262972-04:00","@version":"1","message":"Xform application loaded","logger_name":"gov.cdc.izgateway.xform.Application","thread_name":"Xform Service","level":"INFO","level_value":20000}
{"@timestamp":"2025-06-17T21:36:51.263036-04:00","@version":"1","message":"Build: xform-0.8.0-202506172057","logger_name":"gov.cdc.izgateway.xform.Application","thread_name":"Xform Service","level":"INFO","level_value":20000}
```

## Environment Variable Explanation

The above commands to execute Transformation Service use these bare minimum configuration options:

- SSL_SHARE &rarr; The directory containing keystore files necessary for server SSL and for mTLS
    - The build process generates self-signed files for use in unit testing, which we are using here for local testing
    - See [Transformation Service SSL/Keystore File Reference](./KEYSTORE_FILES.md) for more details on the necessary ssl/keystore files
- COMMON_PASS &rarr; Password used to access the keystore files in the specified SSL_SHARE directory
- XFORM_CONFIGURATIONS_DIRECTORY &rarr; The directory containing application configuration for the system once running
    - This contains a bare minimum set of configuration files to run locally

There are _many_ other configuration properties needed for proper Transformation Service execution. The above three work for local configuration because the default settings are appropriate in this case. For a full explanation of all configuration options please see the [Transformation Service Configuration Reference](./CONFIGURATION_REFERENCE.md)

## Example Calls

TODO

---

&larr;[Back to README](../README.md)
