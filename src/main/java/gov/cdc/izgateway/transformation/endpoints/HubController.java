//package gov.cdc.izgateway.transformation.endpoints;
//
//import ca.uhn.hl7v2.HL7Exception;
//import ca.uhn.hl7v2.model.Message;
////import gov.cdc.izgateway.transformation.context.TransformationContext;
////import gov.cdc.izgateway.transformation.services.Hl7ParserService;
////import gov.cdc.izgateway.transformation.services.Hl7TransformerService;
//import gov.cdc.izgateway.security.AccessControlRegistry;
//import gov.cdc.izgateway.security.Roles;
//import gov.cdc.izgateway.service.DestinationService;
//import gov.cdc.izgateway.service.MessageHeaderService;
//import gov.cdc.izgateway.soap.message.SoapMessage;
//import jakarta.annotation.security.RolesAllowed;
//import lombok.extern.java.Log;
//import lombok.extern.log4j.Log4j;
//import org.apache.camel.ProducerTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpHeaders;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Arrays;
//
//@Log
//@RestController
//@RolesAllowed({Roles.SOAP, Roles.ADMIN})
//public class HubController {
//    @Autowired
//    private ProducerTemplate producerTemplate;
//
//    @Autowired
//    public HubController(
//            AccessControlRegistry registry
//    ) {
//        registry.register(this);
//    }
//
//    @GetMapping("/hellox")
//    public String transform() {
//        return "Hello from HubController!";
//    }
//
//    /**
//     * Will return the transformed message
//     *
//     * @param incomingMessage
//     * @return
//     */
//    @PutMapping("/transformx")
//    public String transform(@RequestBody String incomingMessage, @RequestHeader HttpHeaders headers) {
//        log.info("incomingMessage: \n" + incomingMessage);
//        try {
//            // TransformationContext context = getTransformationContext(incomingMessage, headers);
//
//            producerTemplate.sendBody("direct:izghub", "context");
//
//            // System.out.println("Transformed message: \n\n" + context.getHl7Message().encode());
//            return "completed the transform";
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.warning(e.getMessage());
//            return e.getMessage();
//        }
//    }
//
//}