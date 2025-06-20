# Running In Docker

## Prerequisites

* GitHub Image Access &rarr; Access to IZG Github Image Repository via Personal Access Token to pull the Xform
  Service image
* Store Files
    * Server Key Store
    * Client Trust Store
* Configuration Files

### Store Files

Transformation Service requires two keystore files in order to properly operate. You can read more about these here [Transformation Service SSL/Keystore File Reference](./docs/KEYSTORE_FILES.md). For running locally, you can also use files created during the build process.  See the [Quick Start](./docs/QUICK_START.md) guide for an example.

In our example on this page we will assume that you have: 

- A Server Keystore file named server_keystore.bcfks
- A Client Keystore file named client_keystore.bcfks

For this example, let's say we put these in a directory /izgw-xform/ssl. This local directory would be mapped into the running container to /ssl.

### Application Configuration Files

The Transformation Service can store its Application Configuration as described here: [Application Configuration Storage](./docs/APPLICATION_CONFIGURATION_STORAGE.md)

For this example, we will use files. The different Application Configurations are detailed here: [Transformation Service Configuration Reference](./docs/CONFIGURATION_REFERENCE.md#application-configuration)

You can download example files from this repository which you'll need located in a place that can be accessed by the running Docker container. For this example, let's say we put these in a /izgw-xform/configuration directory. This local directory will be mapped into the running
container to /configuration.

Assuming that you have the files named as the defaults as described in the Application Configuration section of the [Configuration Reference](./CONFIGURATION_REFERENCE.md) you can just set XFORM_CONFIGURATIONS_DIRECTORY to /configuration

## Running Transformation Service Locally via Docker

TODO

To run the image, we need to set a few environment variables used by the running container. Those are:

* XFORM_CONFIGURATIONS_ORGANIZATIONS
    * This specifies the name and location on the running container of the configuration for Organizations. For our
       example, we will look for organizations.json in the /configuration folder. So we would set this to
      /configuration/organizations.json
* XFORM_CONFIGURATIONS_PIPELINES
    * This specifies the name and location on the running container of the configuration for Pipelines. For our example, 
      we will look for pipelines.json in the /configuration folder. So we would set this to
      /configuration/pipelines.json
* XFORM_CONFIGURATIONS_SOLUTIONS
    * This specifies the name and location on the running container of the configuration for Solutions. For our example, 
      we will look for solutions.json in the /configuration folder. So we would set this to
      /configuration/solutions.json
* XFORM_CONFIGURATIONS_MAPPINGS
    * This specifies the name and location on the running container of the configuration for Mappings. For our example, 
      we will look for mappings.json in the /configuration folder. So we would set this to
      /configuration/mappings.json
* XFORM_CONFIGURATIONS_ACCESS-CONTROL
    * This specifies the name and location on the running container of the configuration for access control. For our example, 
      we will look for access-control.json in the /configuration folder. So we would set this to
      /configuration/access-control.json
* XFORM_CONFIGURATIONS_OPERATION-PRECONDITION-FIELDS
    * This specifies the name and the location on the running container of the configuration for fields available in configuring operations and preconditions.  For our example, we will look for precondition-fields.json in the /configuration folder. So we would set this to /configuration/precondition-fields.json
* XFORM_CONFIGURATIONS_USERS
  * This specifies the name and location on the running container of the configuration for users.  These are known users of the Transformation Service system and tie them to an Organization.  For our example, we will look for users.json file in the /configuration folder. So we would set this to /configuration/users.json
* XFORM_CONFIGURATIONS_GROUP-ROLE-MAPPING
  * This specifies the name and location on the running container of the configuration for group to role mappings.  These are Groups necessary for executing different Transformation Service tasks.  This file determines how to map roles that we may receive in a JWT token to Transformation Service groups.  For example, we will look for group-role-mapping.json in the /configuration folder. So we would set this to /configuration/group-role-mapping.json
* COMMON_PASS
    * This is the password that is necessary to open the Server Key Store and Client Trust Store files
* XFORM_SERVER_PORT
    * This is the port that we want the Transformation Service to listen on. For example, we will use port 444
* XFORM_DESTINATION_HUB_URI
    * This is the downstream IZ Gateway Hub that we want the Transformation Service to submit messages to. For our
       example, we will use https://dev.izgateway.org/IISHubService
* XFORM_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE
    * This specifies the path and file on the running container for the Server Key Store.  For our example, we will look for these files in the /ssl directory and the name of the file is server_keystore.bcfks
* XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE
  * Same as TS_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE
* XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE
  * Same as TS_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE
* XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE
    * This specifies the path and file on the running container for the Client Key Store.  For our example, we will look for these files in the /ssl directory, and the name of the file is client_keystore.bcfks
* XFORM_JWT_SECRET
  * This is only necessary if you are going to execute Transformation Service using JWT authentication.  This would be a shared secret that would be necessary for generating and authentication JWT tokens between a caller and this instance of the Transformation Service.

To run the image, we can issue a single docker command as follows:

```bash
docker run \
--env=COMMON_PASS=<PASSWORD> \
--env=XFORM_DESTINATION_HUB_URI=https://dev.izgateway.org/IISHubService \
--env=XFORM_SERVER_PORT=444 \
--env=XFORM_CONFIGURATIONS_ORGANIZATIONS=/configuration/organizations.json \
--env=XFORM_CONFIGURATIONS_PIPELINES=/configuration/pipelines.json \
--env=XFORM_CONFIGURATIONS_SOLUTIONS=/configuration/solutions.json \
--env=XFORM_CONFIGURATIONS_MAPPINGS=/configuration/mappings.json \
--env=XFORM_CONFIGURATIONS_ACCESS-CONTROL=/configuration/access-control.json \
--env=XFORM_CONFIGURATIONS_OPERATION-PRECONDITION-FIELDS=/configuration/operation-precondition-fields.json \
--env=XFORM_CONFIGURATIONS_USERS=/configuration/users.json \
--env=XFORM_CONFIGURATIONS_GROUP-ROLE-MAPPING=/configuration/group-role-mapping.json \
--env=XFORM_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE=/ssl/server_keystore.bcfks \
--env=XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE=/ssl/server_keystore.bcfks \
--env=XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE=/ssl/server_keystore.bcfks \
--env=XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE=/ssl/client_keystore.bcfks \
--volume=/path/to/configuration:/configuration \
--volume=/path/to/ssl:/ssl \
-p 444:444 \
-d \
ghcr.io/izgateway/izgw-transform:latest
```

The first time you run the image you will see messages where Docker is pulling down the image and then running. So
output will be similar to:

```text
Unable to find image 'ghcr.io/izgateway/izgw-transform:latest' locally
latest: Pulling from izgateway/izgw-transform
d25f557d7f31: Already exists
9e36186fec5a: Already exists
14d453f422e7: Already exists
.
.
.
876a4ef3d3db: Pull complete
c214f187cb9f: Pull complete
33c3919a2226: Pull complete
Digest: sha256:57b124b5928ab7d0db1ca6c9f1101951f7ab3ceaa9d34d62114f3557b821444c
Status: Downloaded newer image for ghcr.io/izgateway/izgw-transform:latest
ded59e415a2b6ffe5523cd7aede89cbf61281bb3fe13e343be80f158fb76ceec
```

At this point you can run docker's ps command to see that the container is running:

```bash
docker ps -f name=local-xform
CONTAINER ID   IMAGE                                     COMMAND                  CREATED         STATUS         PORTS                                      NAMES
ded59e415a2b   ghcr.io/izgateway/izgw-transform:latest   "sh -c 'bash run.sh …"   2 minutes ago   Up 2 minutes   8000/tcp, 0.0.0.0:444->444/tcp, 9082/tcp   local-xform
```
