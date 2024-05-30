# IZ Gateway Transformation Service

The Transformation Service, as a new optional use case within the IZ Gateway, offers stakeholders a solution for addressing jurisdiction and/or provider-specific HL7 message transformation needs. This service follows a systematic process to ensure comprehensive data exchange that complies with data exchange partners requirements through the IZ Gateway. Using the IZ Gateway Transformation Service, stakeholders can efficiently identify and implement transformations tailored to their specific requirements. In addition, as an open-source service, users have the option to self-host, develop and implement their own transformations to streamline immunization data exchange pairings limiting the need for one-off configuration updates for specific data exchange partners. The Transformation service enhances interoperability through efficient exchange of complete and accurate immunization data, ultimately contributing to more effective public health initiatives.

## Paul's Notes
- May 29 
  - Had to add to application.yml:
  - spring:
    autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
- May 27 Name conflcit with applicationController - try to figure out why this is.  Most likely due to camel

## Development Team Notes to Address

- Some environment variables were added to this project that may not be needed after the core library is refactored.  For example, an environment variable for the DB URL is needed now, but will not be needed after the refactor.
- TBD

### Benefits

- TBD

## Subsection 2 TBD

### Capabilities

- TBD
- TBD

### Benefits

- TBD
