&larr;[Back to README](../README.md)

---

# Running Transformation Service In Docker

## Prerequisites

* Transformation Service Docker image (See [Quick Start](./QUICK_START.md) for image build instructions)
* Store Files
    * Server Key Store
    * Client Trust Store
* Configuration Files

### Store Files

Transformation Service requires two keystore files in order to properly operate. You can read more about these here [Transformation Service SSL/Keystore File Reference](./docs/KEYSTORE_FILES.md). For running locally, you can also use files created during the build process.  See the [Quick Start](./docs/QUICK_START.md) guide for an example.

In the example later in this document, we will assume that you have: 

- A Server Keystore file named awsdev_keystore.bcfks
- A Client Keystore file named izgw_client_trust.bcfks

For this example, let's say we put these in a directory /tmp/izgw-xform/ssl. This local directory would be mapped into the running container to /ssl.

### Application Configuration Files

The Transformation Service can store its Application Configuration in different ways as described here: [Application Configuration Storage](./docs/APPLICATION_CONFIGURATION_STORAGE.md)

For this example, we will use files. The different Application Configurations are detailed here: [Transformation Service Configuration Reference](./docs/CONFIGURATION_REFERENCE.md#application-configuration)

You can download example files from this repository which you'll need located in a place that can be accessed by the running Docker container. For this example, let's say we put these in a /izgw-xform/configuration directory. This local directory will be mapped into the running
container to /configuration.

Assuming that you have the files named as the defaults as described in the Application Configuration section of the [Configuration Reference](./CONFIGURATION_REFERENCE.md) you can just set XFORM_CONFIGURATIONS_DIRECTORY to /configuration

## Running via Docker

### Simple Configuration Overview 

To run the image, we need to set a few environment variables used by the running container. Those are:

- XFORM_CONFIGURATIONS_DIRECTORY
  - The directory, in the running container, where the Application Configuration files will be located. For our example we have these in a local directory /tmp/izgw-xform/configuration which will be mounted as /configuration in the Docker container. So we set this to /configuration
-XFORM_SERVER_PORT
  - The listening port for the Transformat Service. We will set this to 443.
- XFORM_DESTINATION_HUB_URI
  - The downstream IZ Gateway Hub service endpoint. For this example, we will use https://dev.izgateway.org/IISHubService
- XFORM_IIS_DESTINATION
  - The downstream IZ Gateway Hub IIS endpoint. For this example, we will use https://dev.izgateway.org/dev/IISService
- SSL_SHARE
  - The directory, in the running container, where the necessary server and client keystore files are located. For our example we have these in a local directory /tmp/izgw-xform/ssl which will be mounted as /ssl in the Docker container. So we set this to /ssl
  - Not that if you have the server and keystore files named the defaults, then you only need to set this configuration option. If your files are named differently you will to specify each file via the separate parameters (such as XFORM_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE) as described in the [Configuration Reference](./CONFIGURATION_REFERENCE.md)
- COMMON_PASS
  - This is the password that is necessary to open the Server Key Store and Client Trust Store files located via SSL_SHARE
- LOGGING_LEVEL
  - Running locally we can set this to TRACE to get more details

### Run Docker Image Directly

```shell
docker run \
--env=XFORM_SERVER_PORT=443 \
--env=XFORM_CONFIGURATIONS_DIRECTORY=/configuration \
--env=COMMON_PASS=<PASSWORD> \
--env=XFORM_DESTINATION_HUB_URI=https://dev.izgateway.org/IISHubService \
--env=XFORM_IIS_DESTINATION=https://dev.izgateway.org/dev/IISService \
--env=SSL_SHARE=/ssl \
--env=LOGGING_LEVEL=TRACE \
--volume=/tmp/izgw-xform/configuration:/configuration \
--volume=/tmp/izgw-xform/ssl:/ssl \
-p 443:443 \
-d \
izgw-transform:latest
```

This can be simplified by creating a dotenv formatted file. There is an example file at the root of the repository called ```.env.simple-docker``` which has a _Simple Docker Example_ section:

```shell
cp .env.simple-docker .env
```

Open the .env file in an editor and you'll see this section like: 

```
# BEGIN : Simple Docker Example
XFORM_SERVER_PORT=443
XFORM_CONFIGURATIONS_DIRECTORY=/configuration
XFORM_CONFIGURATIONS_DIRECTORY_LOCAL=/tmp/izgw-xform/configuration
COMMON_PASS=<PASSWORD>
XFORM_DESTINATION_HUB_URI=https://dev.izgateway.org/IISHubService
XFORM_IIS_DESTINATION=https://dev.izgateway.org/dev/IISService
SSL_SHARE_LOCAL=/tmp/izgw-xform/ssl
SSL_SHARE=/ssl
LOGGING_LEVEL=TRACE
# BEGIN : Simple Docker Example
```

You should only need to replace ```<PASSWORD>``` with the proper value for your keystores. 

The docker command then becomes:

```shell
docker run \
--env-file=.env \
--volume=/tmp/izgw-xform/configuration:/configuration \
--volume=/tmp/izgw-xform/ssl:/ssl \
-p 443:443 \
-d \
izgw-transform:latest
```

### Run Docker Image via Compose

There is a [docker-compose.yml](../docker-compose.yml) file that uses a .env file (like the previous example) to run the image.

Assuming you have a .env in place, you can run this from the root of the repository:

```shell
docker compose up
```

---

&larr;[Back to README](../README.md)
