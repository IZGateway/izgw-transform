# IZ Gateway Transformation Service 0.14.0
IGDD-2294 Update to utilize izgw-bom 

# IZ Gateway Transformation Service 0.13.0
Update tomcat to 10.1.48

# IZ Gateway Transformation Service 0.12.0
IGDD-2226 Update GitHub Action to properly tag image pushed to APHL during release
IGDD-2216 Update IZG Hub and Xform Service Docker containers to shutdown if there is a failure of any services
IGDD-2052 Updated secret setting code to base64 decode the secret

# IZ Gateway Transformation Service 0.11.0
IGDD-2181 Update Github Actions to push Docker Images to new APHL environment
IGDD-2184 Update versions of netty, spring-core, and bc-fips

# IZ Gateway Transformation Service 0.10.0
- IGDD-2171 Bump spring version for tomcat cve. Update for next release version
- IGDD-2171 Update GSON and Camel to later versions
- IGDD-2157 Update GitHub Action to deploy in multiple regions
- IGDD-2046 Adding initialization capability for creating self-signed certs and re-initializing DynamoDB

# IZ Gateway Transformation Service 0.9.1
* IGDD-2155 Rebuild using latest base image to resolve CVE-2025-50106 and CVE-2025-30749

# IZ Gateway Transformation Service 0.9.0
* IGDD-2045 LogController now extends LogControllerBase from izgw-core, reduce duplicate code
* IGDD-2063 Add Dependency Checker to GitHub Action
* IGDD-2085 Update bcfips to 2.1.0
* IGDD-2043 Update so that calls to /api/v1/operations and /api/v1/preconditions endpoints properly log details on the user making the call
* IGDD-2016 Make Organizations in the Transformation Service “Organization Aware” like other objects, meaning that a user will only have access to read or update Organizations assigned to their user
* IGDD-2024 Operation execution in Solution now respects order

# IZ Gateway Transformation Service 0.8.1
* IGDD-2092 Bump Spring Framework version to 3.4.7

# IZ Gateway Transformation Service 0.8.0
* IGDD-1991 - API calls to CREATE objects generates and saves UUIDv4 for ID instead of accepted ID submitted by @austinmoody in #144
* Hotfix 0.7.1 rebase by @austinmoody in #148
* IGDD-1407 testing the transformation by @keithboone in #149
* IGDD-1889 update FHIR to V2 based on pilot testing by @keithboone in #150
* IGDD-1407 testing the transformation by @keithboone in #152
* IGDD-1891 - Add DynamoDB storage and migration by @austinmoody in #155
* IGDD-2031 - bcfips update by @austinmoody in #156

# IZ Gateway Transformation Service 0.7.1
* IGDD-2010 - Adding new endpoint /healthy and allowing new role of 'any' to allow public access for infrastructure health checks (https://github.com/IZGateway/izgw-transform/pull/142)
* IGDD-2009 - Adding new class to access server trust store for infrastructure certificate validatation (https://github.com/IZGateway/izgw-core/pull/31)

# IZ Gateway Transformation Service 0.7.0
* IGDD-1958 - Remove unused and unnecessary precondition-fields.json configuration file by (#126)
* IGDD-1955 - Add Group/Role Mapping API endpoings (#127)
* IGDD-1954 / IGDD-1953 - Access Control and User API (#130)
* IGDD-1930 add $convert operation for testing v2to fhir (#131)
* IGDD-1971 Verify roles when creating or updating group mappings (#133)
* IGDD-1889 Update fhir to v2 query based on pilot testing feedback (#134)
* IGDD-1994 access control for objects that are associated with organizations (#135)
* IGDD-1933 - Postman updates for Okta JWT integration (#136)
* IGDD-1863 - hapi update in v2tofhir, part of v2tofhir library included in Xform Service (IZGateway/v2tofhir#6)

# IZ Gateway Transformation Service 0.6.1
* Update pom.xml to remove specifying Spring versions for different dependencies now that 3.4.4 starter covers scenarios.
* Update pom.xml to remove individual dependencies that are already being pulled in by parent.
* Update pom.xml to bump up openapi version to be compatible with Spring 6.4.4 change.
* Update Postman tests to add check for Swagger.
* Bump up to hotfix version 0.6.1. Add configuration to make updating Camel versions easier.
* Bump up camel version.

# IZ Gateway Transformation Service 0.6.0
* Connectathon fixes by @keithboone in #114
* Adding user and access control for Keith. by @pcahillai in #116
* IGDD-1945 Bumping spring boot version to 3.4.3 to address the CVE. by @pcahillai in #118
* FHIR changes based on demo environment needs by @pcahillai in #120

# IZ Gateway Transformation Service 0.5.0
* Fixing test script typo for Release 0.4.0 by @pcahillai in #108
* IGDD-1906 - Update TS_TC_08 Postman tests so that AIRA is in the names. by @austinmoody in #111
* IGDD-1655 solution api verification by @austinmoody in #112
* IGDD-1654 pipeline api verification by @austinmoody in #113
* Merging Release 0.5.0 branch into main by @pcahillai in #117

# IZ Gateway Transformation Service 0.4.0
* IGDD-1775 documentation for selfhosted by @pcahillai in #95
* IGDD-1718 - Update GitHub action for new Xform Service ECS Cluster by @austinmoody in #96
* IGDD-1711 add authorization improvements including JWT support. by @pcahillai in #97
* Add user for Xform Console to default configuration. by @austinmoody in #98
* IGDD-1725 Adding logging for API calls. by @pcahillai in #100
* IGDD-1864 Adding read (for single record via ID), create, update, delete for ma… by @pcahillai in #101
* IGDD-1778 FHIR tests for postman by @pcahillai in #102
* Connectathon fixes by @keithboone in #104
* IGDD-1417 - Changes for updated JWT validation in izgw-core 2.1.8 by @austinmoody in #105
* IGDD-1868 Xform Service - Added improvements to run locally with docker by @pcahillai in #103
* Update pom.xml to remediate CVE-2024-12798, CVE-2024-38820, CVE-2024-12801 by @austinmoody in #106
* Update maven.yml to not push latest tag to APHL by @austinmoody in #107
* Merging Release 0.4.0 branch to main by @pcahillai in #109

# IZ Gateway Transformation Service 0.4.0
* IGDD-1697 pipeline improvements by @pcahillai in #62
* Changing version logic. by @pcahillai in #63
* Removing notes from the readme that are not needed any longer. by @pcahillai in #64
* IGDD-1531 - API additions for preconditions by @austinmoody in #67
* IGDD-1535 enable fhir query by @keithboone in #68
* IGDD-1706 - Xform CVE remediation by @austinmoody in #73
* Update push.yml to use proper GitHub credentials in Maven settings.xml by @austinmoody in #74
* Changing the workflow file to align with team standards by @pcahillai in #76
* IGDD-1716_Update_Docker_Missing_Elastic_Env_Var by @austinmoody in #77
* IGDD-1651 - update docker documentation for keystore by @austinmoody in #78
* IGDD-1717- Rename base configuration entities by @austinmoody in #79
* IGDD-1661 Add_ID_To_Operations by @austinmoody in #80
* IGDD-1763 - Update available fields to work for Operations by @austinmoody in #81
* IGDD-1764 - Add Precondition Advice by @austinmoody in #88
* IGDD-1788 - Setup push of images to APHL on Release by @austinmoody in #89
* IGDD-1770 support for cdc wsdl by @pcahillai in #90
* IGDD-1621 - Standardize naming to Xform by @austinmoody in #91
* Creating a base class for HubController and IISController to reduce duplicate code. by @pcahillai in #92
* Editing the name of test patients so they do not result in Unknown t… by @pcahillai in #93
* Merging Release 0.3.0 branch into main by @pcahillai in #94


