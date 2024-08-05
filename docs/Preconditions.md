# Preconditions

## Overview

This document aims to describe the different Preconditions available in the the Transformation Service and how to configure them in JSON.  

A UI will ultimately be made available for configuring Preconditions.  This document, however, will detail how to configure the Preconditions in the underlying JSON configuration which can be used with the API.

## Available Preconditions

### Equals / Not Equals

This Precondition allows you to configure a path to a piece of data and then a value that it needs to match or _not_ match.

There are four fields requred in the JSON:

* method &rarr; equals or not_equals
* id &rarr; Unique UUID for this configured Precondition 
* dataPath &rarr; Where in the data to find the value we want to compare
  * For HL7 messages the data path needs to be a valid HAPI Terser location 
* comparisonValue &rarr; The information we want to compare to value found at dataPath

Examples:

```json
{
    "method": "not_equals",
    "id": "9201fa07-e7c9-407e-b715-a527a5caac18",
    "dataPath": "/MSH-3-1",
    "comparisonValue": "SAPP"
}
```

```json
{
    "method": "equals",
    "id": "15e13d97-c8a1-41dc-8a4f-ce499120bea5",
    "dataPath": "/MSH-9-1",
    "comparisonValue": "VXU"
}
```

### Exists / Not Exists

This Precondition allows you to configure a path and let you know if that path exists or not.  

This is useful, for example, in HL7v2 to know if a specific field exists in the processed message.  You could also specify the first field in a segment to understand if the segment exists in the message.

There are three fields requred in the JSON:

* method &rarr; exists or not_exists
* id &rarr; Unique UUID for this configured Precondition
* dataPath &rarr; Where in the data to look
    * For HL7 messages the data path needs to be a valid HAPI Terser location

Examples:

```json
{
  "method": "exists",
  "id": "c3be2391-f3e4-4933-96eb-a8f936ba7b8c",
  "dataPath": "/ORDER/OBSERVATION(0)/OBX-3-2"
}
```

```json
{
  "method": "not_exists",
  "id": "37950d06-0b1f-43b7-ab3a-6ef3582a2d69",
  "dataPath": "/PID-3-4-2"
}
```

### Regex Match

This Precondition allows you to configure a path and then a Regular Expression.  If the Regular Expression results in a match then the Precondition passes.

There are four fields requred in the JSON:

* method &rarr; regex_match
* id &rarr; Unique UUID for this configured Precondition
* dataPath &rarr; Where in the data to find the value we want to execute the Regular Expression on
  * For HL7 messages the data path needs to be a valid HAPI Terser location
* regex &rarr; The Regular Expression
  * **Note** the regex must be properly escaped for storing in JSON format.

Examples: 

```json
{
    "method": "regex_match",
    "id": "6f4429e0-a628-4e13-94cf-94f26c2890b6",
    "dataPath": "/PID-13-1",
    "regex": "\\((\\d{3})\\) (\\d{3})-(\\d{4})"
}
```

```json
{
    "method": "regex_match",
    "id": "1df2201a-cd94-41a0-a2ee-9d202c51d84b",
    "dataPath": "/PID-13-4",
    "regex": "(\\w+)@(\\w+)\\.(com|org|net)?"
}
```
