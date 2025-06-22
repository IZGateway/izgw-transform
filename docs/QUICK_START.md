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

This is not meant to be an exhaustive tutorial on the different API calls that one can make to the Transformation Service. However here are a couple examples of calls that will work assuming you have followed the previous instructions on this page to get the transformation service running locally.

In these examples, you will see a _cert_ and _key_ specified from the _target_ directory. Similar to when we ran the Transformation Service using the keystores generated during the build, we here use the client certificate which is also generated during the build.

In a _real_ scenario, you would be using valid keystores and client certificate.

### Pipelines API

In this example, we call the Pipelines API endpoint using the HTTP method GET. This will pull back a list of Pipelines configured in the running system.

```shell
curl -X GET -k --cert ./target/self_signed_unit_tests.crt \
--key ./target/self_signed_unit_tests.key \
--location 'https://localhost:444/api/v1/pipelines'
```

This should result in output similar to:

```json
{"data":[{"pipelineName":"Xform Local Testing Only - Hub Service","id":"6e6df3c3-78e7-478f-8c38-f6e937127b1c","organizationId":"7c74f309-810c-4a05-8a8d-4938d099383d","description":"","inboundEndpoint":"izgts:IISHubService","outboundEndpoint":"izghub:IISHubService","active":true,"pipes":[{"id":"cc6fcd21-f395-4155-a82e-8436351659f4","solutionId":"2f81dcd6-329e-4e6b-a9f0-69aa6d5dacfd","solutionVersion":"1.0","preconditions":[]}]}],"has_more":"false"}
```

### IISHubService Loopback

Next we'll send in a test message to the IISHubService, in _loopback mode_. This will process the message through the located Pipeline and return the transformed message. As it is a loopback, no attempt is made to send the message to the downstream system as would normally happen.

There is an [example_message.xml](./quickstart/example_message.xml) file which we are having _curl_ read in to send.

Execute the following command:

```shell
curl -X POST -k \
--cert ./target/self_signed_unit_tests.crt \
--key ./target/self_signed_unit_tests.key \
--location 'https://localhost:444/IISHubService' \
--header 'x-loopback: true' \
--header 'Content-Type: application/xml' \
--data @./docs/quickstart/example_message.xml
```

This should result in the following output:

```xml
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:iis="urn:cdc:iisb:2014">
    <soap:Header xmlns:wsa="http://www.w3.org/2005/08/addressing">
        <wsa:Action>urn:cdc:iisb:hub:2014:IISHubPortType:SubmitSingleMessageResponse</wsa:Action>
        <wsa:MessageID>f3d4004a-c6e4-447c-8c6b-155f7d5f216b</wsa:MessageID>
        <wsa:To>http://www.w3.org/2005/08/addressing/anonymous</wsa:To>
        <hub:HubResponseHeader xmlns:hub="urn:cdc:iisb:hub:2014">
            <hub:DestinationId>dev</hub:DestinationId>
        </hub:HubResponseHeader>
        <xform:XformResponseHeader xmlns:xform="urn:cdc:iisb:hub:xform">
            <xform:TransformedRequest>MSH|^~\&amp;|IZGATEWAY|FLHOSP1|FLSHOTS|FLSHOTS|20230809110858-0500||VXU^V04^VXU_V04|AUSTIN1|X|2.5.1|||ER|AL|                PID|1||DC1ADMIN^IZG-TEST^MR||BABYIZG^BABYIZG^AmeliaIZG^^L|XFORM_LOCAL_TESTING^EllaIZG^TeresaIZG^^M|20170723|F||2106-3^White^CDCREC~2054-5^Black                or African American^CDCREC|123 Main Street^^TAMPA^FL^376041234^USA||(423)                364-3003^PRN^CP^1^215^5551213||eng^English^ISO639|||||||2135-2^Hispanic or Latino^CDCREC|||                PD1|||||||||||02^Reminder/Recall - any method^HL70215|||||A|20150901|20150901|                NK1|1|WinchesterIZG^EllaIZG^TeresaIZG^^^L|MTH^Mother^HL70063|5255 Loughboro Rd                NW^^Tampa^FL^33601^USA^P|^PRN^PH^1^555^5551212NETizgatewaytesting@gmail.com|||||||||||||||||||||||||||^^^^43040-1234                ORC|RE||197028^SP|||||||1112223334^GrecoIZG^AlanaIZG^^^^L^^NPI^^^^^^RN||2223334445^Jones^Casey^^^^L^^NPI^^^^^^MD|||||DMC53427^IZ                Gateway Clinic^HL70362|||||^^^^08540                RXA|0|1|20200815|20200815|03^measles, mumps, rubella virus vaccine^CVX^00006-4681-01^MMR                II^NDC|0.5|mLUCUM||02^Historical information - from other                provider^NIP001|1112223334^GrecoIZG^AlanaIZG^^^^L^^NPI^^^^^^RN|^DMC53427^^^4 First Street^PO                Box^Tampa^FL^33601||||0934GG|20211231|MSD^Merck Sharp T Dohme Corp^MVX|||CP|A|                RXR|C38299^Subcutaneous^NCIT^SC^Subcutaneous^HL70162|RA^Right Arm^HL70163|                OBX|1|CE|64994-7^vaccine fund pgm elig cat^LN|2|V02^VFC eligible-Medicaid/Medicaid Managed                Care^HL70064||||||F|||20201031||||||||                OBX|2|CE|30963-3^Vaccine funding source^LN|2|VXC50^Public^CDCPHINVS||||||F|||20201031|                ORC|RE||197028^SP|||||||1112223334^GrecoIZG^AlanaIZG^^^^L^^NPI^^^^^^RN||2223334445^Jonesizg^Caseyizg^^^^L^^NPI^^^^^^MD|||||DMC53427^IZ                Gateway Clinic^HL70362|||||^^^^129011234                RXA|0|1|20211020||21^Varicella^CVX|0.5|mL^milliliters^UCUM||00^Administered^NIP001|1112223334^GrecoIZG^AlanaIZG^^^^L^^NPI^^^^^^RN|^DMC53427^^^4                First Street^PO Box^Tampa^FL^33601||||U6329BP|20211231|MSD^Merck and Co^MVX|||CP|A|                RXR|C38299NCIT|RA^HL70163|                OBX|1|CE|64994-7^vaccine fund pgm elig cat^LN|1|V01^Not VFC elig^HL70064||||||F|||20210723|                OBX|2|CE|30963-3^Vaccine funding source^LN|1|VXC50^Public^HL7</xform:TransformedRequest>
        </xform:XformResponseHeader>
    </soap:Header>
    <soap:Body>
        <iis:SubmitSingleMessageResponse>
            <iis:Hl7Message>MSH|^~\&amp;|IZGATEWAY|FLHOSP1|FLSHOTS|FLSHOTS|20230809110858-0500||VXU^V04^VXU_V04|AUSTIN1|X|2.5.1|||ER|AL|                PID|1||DC1ADMIN^IZG-TEST^MR||BABYIZG^BABYIZG^AmeliaIZG^^L|XFORM_LOCAL_TESTING^EllaIZG^TeresaIZG^^M|20170723|F||2106-3^White^CDCREC~2054-5^Black                or African American^CDCREC|123 Main Street^^TAMPA^FL^376041234^USA||(423)                364-3003^PRN^CP^1^215^5551213||eng^English^ISO639|||||||2135-2^Hispanic or Latino^CDCREC|||                PD1|||||||||||02^Reminder/Recall - any method^HL70215|||||A|20150901|20150901|                NK1|1|WinchesterIZG^EllaIZG^TeresaIZG^^^L|MTH^Mother^HL70063|5255 Loughboro Rd                NW^^Tampa^FL^33601^USA^P|^PRN^PH^1^555^5551212NETizgatewaytesting@gmail.com|||||||||||||||||||||||||||^^^^43040-1234                ORC|RE||197028^SP|||||||1112223334^GrecoIZG^AlanaIZG^^^^L^^NPI^^^^^^RN||2223334445^Jones^Casey^^^^L^^NPI^^^^^^MD|||||DMC53427^IZ                Gateway Clinic^HL70362|||||^^^^08540                RXA|0|1|20200815|20200815|03^measles, mumps, rubella virus vaccine^CVX^00006-4681-01^MMR                II^NDC|0.5|mLUCUM||02^Historical information - from other                provider^NIP001|1112223334^GrecoIZG^AlanaIZG^^^^L^^NPI^^^^^^RN|^DMC53427^^^4 First Street^PO                Box^Tampa^FL^33601||||0934GG|20211231|MSD^Merck Sharp T Dohme Corp^MVX|||CP|A|                RXR|C38299^Subcutaneous^NCIT^SC^Subcutaneous^HL70162|RA^Right Arm^HL70163|                OBX|1|CE|64994-7^vaccine fund pgm elig cat^LN|2|V02^VFC eligible-Medicaid/Medicaid Managed                Care^HL70064||||||F|||20201031||||||||                OBX|2|CE|30963-3^Vaccine funding source^LN|2|VXC50^Public^CDCPHINVS||||||F|||20201031|                ORC|RE||197028^SP|||||||1112223334^GrecoIZG^AlanaIZG^^^^L^^NPI^^^^^^RN||2223334445^Jonesizg^Caseyizg^^^^L^^NPI^^^^^^MD|||||DMC53427^IZ                Gateway Clinic^HL70362|||||^^^^129011234                RXA|0|1|20211020||21^Varicella^CVX|0.5|mL^milliliters^UCUM||00^Administered^NIP001|1112223334^GrecoIZG^AlanaIZG^^^^L^^NPI^^^^^^RN|^DMC53427^^^4                First Street^PO Box^Tampa^FL^33601||||U6329BP|20211231|MSD^Merck and Co^MVX|||CP|A|                RXR|C38299NCIT|RA^HL70163|                OBX|1|CE|64994-7^vaccine fund pgm elig cat^LN|1|V01^Not VFC elig^HL70064||||||F|||20210723|                OBX|2|CE|30963-3^Vaccine funding source^LN|1|VXC50^Public^HL70064||||||F|||20220406</iis:Hl7Message>
        </iis:SubmitSingleMessageResponse>
    </soap:Body>
</soap:Envelope>
```

---

&larr;[Back to README](../README.md)
