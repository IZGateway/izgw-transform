# Operations

## Overview

This document aims to describe the different Operations available in the Transformation Service and how to configure them in JSON.

Operations are steps which make up a Solution.  There will likely be a UI in the future for building Solutions.  For the MVP building Solutions will need to happen via the Solutions configuration file or via the API.

## Available Operations 

### Copy

This Operation allows you to copy data from a specified _From_ field to a specified _To_ field.

There are three fields required in the JSON:

* method &rarr; copy
* sourceField &rarr; Where in the data to find the value we want to copy
  * For HL7 messages the data path needs to be a valid HAPI Terser location
* destinationField &rarr; Where in the data to place the copied value 
  * For HL7 messages the data path needs to be a valid HAPI Terser location

Example: 

This copies the date in the HL7v2 MSH.7.1 field to the EVN.2.1 field.

```json
{
  "method": "copy",
  "sourceField": "/MSH-7-1",
  "destinationField": "/EVN-2-1"
}
```

### Set

This Operation allows you to place a static value into a specified field in the data.

There are three fields required in the JSON:

* method &rarr; set
* destinationField &rarr; Where in the data to place the value specified by setValue
    * For HL7 messages the data path needs to be a valid HAPI Terser location
* setValue &rarr; The value that we want to place in the location specified by destinationField

Example: 

This will force a value of P into the HL7v2 Processing ID in MSH-11.

```json
{
  "method": "set",
  "destinationField": "/MSH-11-1",
  "setValue": "P"
}
```

### Regex Replace

This Operation allows you to apply a Regular Expression Replace on a specified field in the data.

There are three fields required in the JSON:

* method &rarr; regex_replace
* field &rarr; Where in the data to run through the Regular Expression Replace
    * For HL7 messages the data path needs to be a valid HAPI Terser location
* regex &rarr; The Regular Expression
  * **Note** the regex must be properly escaped for storing in JSON format.
* replacement &rarr; The Replacement

Example: 

This will add a dash into a 9-digit zip code.

```json
{
  "method": "regex_replace",
  "field": "/PID-11-5",
  "regex": "\\b(\\d{5})(\\d{4})?\\b",
  "replacement": "$1-$2"
}
```

### Save State

This Operation will save the value from a specified field in the data to the State so that it can be used in other steps in the process.


Example: 

This will save the value of HL7v2 Message Control ID into the state.

```json
{
  "method": "save_state",
  "field": "/MSH-10-1",
  "key": "messageControlId"
}

```
