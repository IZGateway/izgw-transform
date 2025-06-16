&larr;[Back to README](../README.md)

---

# Building & Running Transformation Service

This document aims to help you get started quickly with building and runningthe Transformation Service locally.

## Prerequisites

- Java 17
- Maven 3.6+
- Git
- GitHub Account
- Docker
  - Required to build Docker images via ```mvn package```

### Maven Configuration

The Transformation Service currently has two dependencies on other IZ Gateway libraries: IZGW Core and v2tofhir. These are obtained via GitHub packages. At this time it is necessary to configure Maven in order to access GitHub packages. Please see [Maven GitHub Packages Access](./MAVEN_GITHUB_PACKAGES.md) for details on this configuration.

## Building Transformation Service

```shell
git clone git@github.com:IZGateway/izgw-transform.git
cd izgw-transform
mvn clean package
```

This will create the Transformation Service jar file in the _target_ directory. The file will be named ```xform-X.Y.Z.jar``` where X.Y.Z will be replaced with the value set by project &rarr; artifactId &rarr; version in the [pom.xml](../pom.xml) file.

## Running Transformation Service

TODO

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
