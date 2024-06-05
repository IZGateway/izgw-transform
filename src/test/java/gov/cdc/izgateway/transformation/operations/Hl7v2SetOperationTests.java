package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.configuration.OperationSetConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Hl7v2SetOperationTests {

//    @Test
//    void testSetComponetExistingField() throws HL7Exception{
//        OperationSetConfig message = new OperationSetConfig();
//        message.setDestinationField("MSH-4-1");
//        message.setSetValue("PCC");
//        Hl7v2SetOperation testClass = new Hl7v2SetOperation(message);
//        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
//                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r\n" + //
//                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|PCC|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
//                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r\n" + //
//                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        DefaultHapiContext context = new DefaultHapiContext();
//        context.setValidationContext(new NoValidation());
//        PipeParser parser = context.getPipeParser();
//        Message testMessage = parser.parse(testHL7);
//        Message expectedMessage = parser.parse(expectedHL7);
//        testClass.executeOperation(testMessage);
//        String expected = expectedMessage.encode();
//        String test = testMessage.encode();
//        assertEquals(expected, test);
//    }
//
//    @Test
//    void testSetComponetNonExistingField() throws HL7Exception{
//        OperationSetConfig message = new OperationSetConfig();
//        message.setDestinationField("MSH-22-1");
//        message.setSetValue("Sample");
//        Hl7v2SetOperation testClass = new Hl7v2SetOperation(message);
//        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
//                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r\n" + //
//                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1||||||||||Sample\r\n" + //
//                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r\n" + //
//                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        DefaultHapiContext context = new DefaultHapiContext();
//        context.setValidationContext(new NoValidation());
//        PipeParser parser = context.getPipeParser();
//        Message testMessage = parser.parse(testHL7);
//        Message expectedMessage = parser.parse(expectedHL7);
//        testClass.executeOperation(testMessage);
//        String expected = expectedMessage.encode();
//        String test = testMessage.encode();
//        assertEquals(expected, test);
//    }
//
//    @Test
//    void testSetComponetNonExistingSegment() throws HL7Exception{
//        OperationSetConfig message = new OperationSetConfig();
//        message.setDestinationField("PID-1");
//        message.setSetValue("1");
//        Hl7v2SetOperation testClass = new Hl7v2SetOperation(message);
//        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
//                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
//                        "PID|1\r\n" + //
//                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        DefaultHapiContext context = new DefaultHapiContext();
//        context.setValidationContext(new NoValidation());
//        PipeParser parser = context.getPipeParser();
//        Message testMessage = parser.parse(testHL7);
//        Message expectedMessage = parser.parse(expectedHL7);
//        testClass.executeOperation(testMessage);
//        String expected = expectedMessage.encode();
//        String test = testMessage.encode();
//        assertEquals(expected, test);
//    }
//
//    @Test
//    void testSetSubComponetExistingField() throws HL7Exception{
//        OperationSetConfig message = new OperationSetConfig();
//        message.setDestinationField("PV1-3-1-1");
//        message.setSetValue("1");
//        Hl7v2SetOperation testClass = new Hl7v2SetOperation(message);
//        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
//                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r\n" + //
//                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
//                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r\n" + //
//                        "PV1|1|I|1^Room456^Bed789|||||||||||||||||1234567890";
//        DefaultHapiContext context = new DefaultHapiContext();
//        context.setValidationContext(new NoValidation());
//        PipeParser parser = context.getPipeParser();
//        Message testMessage = parser.parse(testHL7);
//        Message expectedMessage = parser.parse(expectedHL7);
//        testClass.executeOperation(testMessage);
//        String expected = expectedMessage.encode();
//        String test = testMessage.encode();
//        assertEquals(expected, test);
//    }
//
//    @Test
//    void testSetSubComponetNonExistingField() throws HL7Exception{
//        OperationSetConfig message = new OperationSetConfig();
//        message.setDestinationField("PV1-3-1-2");
//        message.setSetValue("1");
//        Hl7v2SetOperation testClass = new Hl7v2SetOperation(message);
//        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
//                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r\n" + //
//                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
//                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r\n" + //
//                        "PV1|1|I|Ward123&1^Room456^Bed789|||||||||||||||||1234567890";
//        DefaultHapiContext context = new DefaultHapiContext();
//        context.setValidationContext(new NoValidation());
//        PipeParser parser = context.getPipeParser();
//        Message testMessage = parser.parse(testHL7);
//        Message expectedMessage = parser.parse(expectedHL7);
//        testClass.executeOperation(testMessage);
//        String expected = expectedMessage.encode();
//        String test = testMessage.encode();
//        assertEquals(expected, test);
//    }
//
}
