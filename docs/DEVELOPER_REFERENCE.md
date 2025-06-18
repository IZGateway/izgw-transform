&larr;[Back to README](../README.md)

---

# Building & Running Transformation Service

This document aims to help you get started quickly with building and running the Transformation Service locally.

## Prerequisites

Prerequisites are detailed in the [Get Running Quick](./GET_RUNNING_QUICK.md) guide.

## Building Transformation Service

```shell
git clone git@github.com:IZGateway/izgw-transform.git
cd izgw-transform
mvn clean package
```

This will create the Transformation Service jar file in the _target_ directory. The file will be named ```xform-X.Y.Z.jar``` where X.Y.Z will be replaced with the value set by project &rarr; artifactId &rarr; version in the [pom.xml](../pom.xml) file.

## Running Transformation Service

TODO - create example .env file and docker-compose.yml. The example .env should have all configuration options commented out with pointer to the configuration reference.

## Maven Commands Reference

**Package**

```mvn package```

This will perform:

- Compilation
- Style checking
- JAR creation

**Install**

```mvn install```

This will perform:

- Compilation
- Style checking
- JAR creation
- OWASP dependency checking
- Build and tag Docker images
- Install JAR in local Maven repository

**Style Checking**

```shell
mvn checkstyle:check
```

**OWASP Dependency Checking**

```shell
mvn org.owasp:dependency-check-maven:check
```

**Compile**

```shell
mvn clean compile
```

---

&larr;[Back to README](../README.md)
