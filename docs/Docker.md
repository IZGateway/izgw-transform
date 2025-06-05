# Running In Docker

## Prerequisites

* GitHub Image Access &rarr; Access to IZG Github Image Repository via Personal Access Token to pull the Xform
  Service image
* Store Files
    * Server Key Store
    * Client Trust Store
* A configuration file

### Store Files

The Transformation Service makes use of two separate sets of certificate and key stores: the Server Key Store and the
Client Trust Store

Both the Server Key Store and Client Trust Store are formatted in Bouncy Castle File KeyStore (BCFKS) format to conform
to FIPS encryption requirements for media. JKS format is non-compliant for secret material.

Please _NOTE_ the following that pertain to both files:

* Both files need to have the same password set for opening them, which will be used to set the COMMON_PASS environment
  variable when running the image.
* You'll need to place Server Key Store and Client Trust Store files in a location that can be accessed by the running
  Docker container. For our example let's say we put these in a /izgw-xform/ssl directory. This local directory will be
  mapped into the running container to /ssl, which will be used to in setting environment variables for the running container.

#### Server Key Store

The Server Key Store is used to identify the Transformation Service to systems calling the Transformation Service and
validate the certificates those calling systems present.

Systems calling the hosted Transformation Service must have a certificate that has been issued by DigiCert that has been
provided by the IZ Gateway Security team. This enables the Transformation Service to identify trusted calling systems
because those certificates will all be "signed" by a single Certificate Authority. This also means that the hosted
Transformation Service does not need to change its Server Key Store frequently (only when the Certificate Authority's
certificate has been renewed). Systems calling the Transformation Service should verify that the server certificate is
valid, and further will be asked to present their client certificate in order for the Transformation Service to ensure
the connection is coming from a trusted system.

For the purposes of our example later in this document we will name this file server_keystore.bcfks.

#### Client Trust Store

When the Transformation Service makes a connection to the IZ Gateway, it will need to verify that it has made a
connection to a trusted system using the expected certificate. This is established by verifying that the server
certificate of the destination IZ Gateway is a trusted certificate by appearing in the Transformation Service Client
Trust Store.

For the purposes of our example later in this document we will name this file client_keystore.bcfks.

### Configuration Files

The Transformation Service relies on eight configuration files at this time: Organization, Pipelines, Solutions, Mapping, Access Control, Operation/Precondition Fields, Users, and Group to Role Mappings. These
ultimately determine the changes that will happen to data as it travels through the Transformation Service but also _who_ can access API's.

You may download example configurations from the repository as described here:

* Organizations &rarr; [organizations.json](/testing/configuration/organizations.json)
* Pipelines &rarr; [pipelines.json](/testing/configuration/pipelines.json)
* Solutions &rarr; [solutions.json](/testing/configuration/solutions.json)
* Mappings &rarr; [mappings.json](/testing/configuration/mappings.json)
* Access Control &rarr; [access-control.json](/testing/configuration/access-control.json)
* Operation/Precondition Fields &rarr; [operation-precondition-fields.json](/testing/configuration/operation-precondition-fields.json)
* Users &rarr; [users.json](/testing/configuration/users.json)
* Group Role Mappings &rarr; [group-role-mapping.json](/testing/configuration/group-role-mapping.json)

You'll need to have these files located in a place that can be accessed by the running Docker container. For our example
let's say we put these in a /izgw-xform/configuration directory. This local directory will be mapped into the running
container to /configuration. That directory name (/configuration) and the name of the three files will be used to set
environment variables when running the image.

The necessary environment variables for the configuration files:

* XFORM_CONFIGURATIONS_ORGANIZATIONS
* XFORM_CONFIGURATIONS_PIPELINES
* XFORM_CONFIGURATIONS_SOLUTIONS
* XFORM_CONFIGURATIONS_MAPPINGS
* XFORM_CONFIGURATIONS_ACCESS-CONTROL
* XFORM_CONFIGURATIONS_OPERATION-PRECONDITION-FIELDS
* XFORM_CONFIGURATIONS_USERS
* XFORM_CONFIGURATIONS_GROUP-ROLE-MAPPING

## Running Transformation Service Locally via Docker

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

## Generate Server Key Store

One of the files mentioned previously is the keystore that is needed to have the Transformation Service run.  For our example we will name this server_keystore.bcfks.

Steps to create the keystore bcfks file:

* You should already have in your possession:
    * A key file in pem format and a cert file in pem format. For this example, we will use sample-private-key.pem and
      sample-cert.pem to represent these files.
* Create a .p12 file from your cert and key pem files:
    * ```openssl pkcs12 -export -in sample-cert.pem -inkey sample-private-key.pem -out sample-keystore.p12 -name "samplealias"```
* Create a .bcfks file using the following command:
    * ```keytool -importkeystore -srckeystore sample-keystore.p12 -srcstorepass 'password' -storepass 'password' -destkeystore server_keystore.bcfks -deststoretype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath ./bc-fips-2.0.0.jar```
* Add the root ca cert to the bcfks file:
    * ```keytool -importcert -file DigiCertCA.crt -keystore server_keystore.bcfks -storetype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath ./bc-fips-2.0.0.jar -storepass 'password' -alias DigiCertCA```
* Add the intermediate cert to the bcfks file
    * ```keytool -importcert -file TrustedRoot.crt -keystore server_keystore.bcfks -storetype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath ./bc-fips-2.0.0.jar -storepass 'password' -alias TrustedRoot```
* To list the contents of the bcfks file:
    * ```keytool -list -keystore ./server_keystore.bcfks -storepass 'password' -deststoretype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath ./bc-fips-2.0.0.jar```

## Generate Client Trust Store

You may already have a file that works for this if you are connecting to a dev or test IZ Gateway. However, as an
example, below are steps to generate this file to connect to the Development IZ Gateway hosted at dev.izgateway.org.  For out example, our output file will be named client_keystore.bcfks.

You will need:

* To be on a system with the ability to run openssl
* To be on a system with the ability to run keytool (this is available if you have Java installed)
* A bc-fips jar in order to produce the Bouncy Castle FIPS compliant file
    * The latest appropriate jar to use is in the docker/data folder

Steps to create the file

* Pull the public cert for the server using openssl:
    * ```openssl s_client -showcerts -connect dev.izgateway.org:443 </dev/null 2>/dev/null | openssl x509 -outform PEM > dev.izgateway.org.crt```
* Create the Client Trust Store using keytool
    * Notes about the keytool command below
        * Use the password that you intend to use as the COMMON_PASS environment variable where you see <PASSWORD>
        * Replace <PATH TO> to the location for the bc-fips jar file
    * ```keytool -import -alias dev.izgateway.org -keystore client_keystore.bcfks -file dev.izgateway.org.crt -noprompt -storepass '<PASSWORD>' -deststoretype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath /<PATH TO>/bc-fips-2.0.0.jar```
