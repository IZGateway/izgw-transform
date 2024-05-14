package gov.cdc.izgateway.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.configuration.OperationCopyConfig;
import gov.cdc.izgateway.transformation.operations.Hl7v2CopyOperation;
@SpringBootTest
class TransformationApplicationTests {

  @Test
  void contextLoads() {}

  @Test
  void testCopyWithinSegment() throws HL7Exception{
    OperationCopyConfig what = new OperationCopyConfig();
    what.setSourceField("MSH-4-1");
    what.setDestinationField("MSH-4-2");
    Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(what);
    String testHL7  ="MSH|^~\\&||DOE|DCC|DOE|20050829141336||ACK|1125342816253.100000055|P|2.3.1";
    String expectedHL7 = "MSH|^~\\&||DOE^DOE|DCC|DOE|20050829141336||ACK|1125342816253.100000055|P|2.3.1|";
    DefaultHapiContext context = new DefaultHapiContext();
    context.setValidationContext(new NoValidation());
    PipeParser parser = context.getPipeParser();
    Message testMessage = parser.parse(testHL7);
    Message expectedMessage = parser.parse(expectedHL7);
    testClass.executeOperation(testMessage);
    //errors happen if you compare the Message objects, turning to strings was the best solution I could think of
    String expected = expectedMessage.encode();
    String test = testMessage.encode();
    assertEquals(expected, test);
  }

  @Test
  void testCopyDifferentSegment() throws HL7Exception{
    OperationCopyConfig what = new OperationCopyConfig();
    what.setSourceField("MSA-2");
    what.setDestinationField("MSH-10");
    Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(what);
    String testHL7  ="MSH|^~\\&||DOE^DOE|DCC|DOE|20050829141336||ACK|1125342816253.100000055|P|2.3.1\r" + 
            "MSA|AE|00000001|Patient id was not found, must be of type 'MR'|||^^HL70357\r" + 
            "ERR|PID^^3^^^HL70357";
    String expectedHL7 = "MSH|^~\\&||DOE^DOE|DCC|DOE|20050829141336||ACK|00000001|P|2.3.1\r" + 
            "MSA|AE|00000001|Patient id was not found, must be of type 'MR'|||^^HL70357\r" + 
            "ERR|PID^^3^^^HL70357";
    DefaultHapiContext context = new DefaultHapiContext();
    context.setValidationContext(new NoValidation());
    PipeParser parser = context.getPipeParser();
    Message testMessage = parser.parse(testHL7);
    Message expectedMessage = parser.parse(expectedHL7);
    
    testClass.executeOperation(testMessage);
    String expected = expectedMessage.encode();
    String test = testMessage.encode();
    assertEquals(expected, test);
  }
}
