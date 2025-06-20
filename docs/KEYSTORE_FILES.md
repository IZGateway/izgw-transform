# Transformation Service SSL / Keystore File Reference

The Transformation Service makes use of two key stores: the Server Key Store and the
Client Trust Store

Both the Server Key Store and Client Trust Store must be in Bouncy Castle File KeyStore (BCFKS) format to conform
to FIPS encryption requirements for media. JKS format is non-compliant for secret material.

Please _NOTE_ the following that pertain to both files:

* Both files need to have the same password set for opening them, which will be used to set the COMMON_PASS environment variable when running the image.
* You'll need to place Server Key Store and Client Trust Store files in a location that can be accessed by the running code. For example, let's say we put these in a /izgw-xform/ssl directory. This local directory would need to be mapped into the running container, that directory would be set via properties so that Transformation Service would know where to read them.

For the examples in this document to create files you will need:

- To be on a system with the ability to run openssl
- To be on a system with the ability to run keytool (this is available if you have Java installed)
- A bc-fips jar to produce the Bouncy Castle FIPS compliant file
  - The latest appropriate jar to use is in the docker/data folder of this repository
- Where you see <PASSWORD> would be replaced with the value you'd configured as COMMON_PASS in a running system

## Server Key Store

The Server Key Store is used to identify the Transformation Service to calling systems validate the certificates those calling systems present.

Systems calling the APHL hosted Transformation Service must have a certificate that has been issued by DigiCert that has been provided by the IZ Gateway Security team. This enables the Transformation Service to identify trusted calling systems because those certificates will all be "signed" by a single Certificate Authority. This also means that the hosted Transformation Service does not need to change its Server Key Store frequently (only when the Certificate Authority's certificate has been renewed). Systems calling the Transformation Service should verify that the server certificate is valid, and further will be asked to present their client certificate in order for the Transformation Service to ensure the connection is coming from a trusted system.

### Generating A Server Key Store

Below are example commands to run to generate a Server Key store. For our example, we will name this server_keystore.bcfks.

To follow these instructions, you should already have in your possession:
- A key file in pem format (sample-private-key.pem)
- A certificate file in pem format (sample-cert.pem)
- Root CA for the certificate issuer (DigiCertCA.crt)
- Intermediate Root for the certificate issuer (IntermediateRoot.crt)

Steps:

-Create a .p12 file from your cert and key pem files 
    ```openssl pkcs12 -export -in sample-cert.pem -inkey sample-private-key.pem -out sample-keystore.p12 -name "samplealias"```
- Create a .bcfks file using the following command 
  - ```keytool -importkeystore -srckeystore sample-keystore.p12 -srcstorepass '<PASSWORD>' -storepass <PASSWORD> -destkeystore server_keystore.bcfks -deststoretype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath ./bc-fips-2.0.0.jar```
- Add the root ca cert to the bcfks file 
  - ```keytool -importcert -file DigiCertCA.crt -keystore server_keystore.bcfks -storetype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath ./bc-fips-2.0.0.jar -storepass '<PASSWORD>' -alias DigiCertCA```
- Add the intermediate cert to the bcfks file 
  - ```keytool -importcert -file IntermediateRoot.crt -keystore server_keystore.bcfks -storetype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath ./bc-fips-2.0.0.jar -storepass '<PASSWORD>' -alias IntermediateRoot```
- To list the contents of the bcfks file
  - ```keytool -list -keystore ./server_keystore.bcfks -storepass '<PASSWORD>' -deststoretype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath ./bc-fips-2.0.0.jar```

## Client Trust Store

When the Transformation Service makes a connection to any outbound system (IZ Gateway, and IDP such as Okta), it will need to verify that it has made a connection to a trusted system using the expected certificate. This is established by verifying that the server certificate of the destination is a trusted certificate by appearing in the Transformation Service Client Trust Store.

## Generate Client Trust Store

You may already have a file that works for this if you are connecting to a dev or test IZ Gateway. However, as an example, below are steps to generate this file to connect to the Development IZ Gateway hosted at dev.izgateway.org.  For this example, the output file will be named client_keystore.bcfks.

Steps:

- Pull the public cert for the server using openssl 
  - ```openssl s_client -showcerts -connect dev.izgateway.org:443 </dev/null 2>/dev/null | openssl x509 -outform PEM > dev.izgateway.org.crt```
- Create the Client Trust Store using keytool 
  - ```keytool -import -alias dev.izgateway.org -keystore client_keystore.bcfks -file dev.izgateway.org.crt -noprompt -storepass '<PASSWORD>' -deststoretype BCFKS -providername BCFIPS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath ./bc-fips-2.0.0.jar```
