# Building & Developing Transformation Service

This guide aims to help you get started quickly with building the Transformation Service and provide an overview of the code for local development.

## Prerequisites

- Java 17
- Maven 3.6+
- Git
- GitHub Account

### Maven Configuration

The Transformation Service currently has two dependencies on other IZ Gateway libraries: IZGW Core and v2tofhir. These are obtained via GitHub packages. At this time it is necessary to configure Maven in order to access GitHub packages. Please see [Maven GitHub Packages Access](./MAVEN_GITHUB_PACKAGES.md) for details on this configuration.

## Building Transformation Service

### 1. Clone 

