package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.configuration.OperationCopyConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Hl7v2CopyOperationTests {

    @Test
    void testCopyComponetToComponetSameSegment() throws HL7Exception
    {
        String testHL7  ="MSH|^~\\&||DOE|DCC|DOE|20050829141336||ACK|1125342816253.100000055|P|2.3.1";
        String expectedHL7 = "MSH|^~\\&||DOE|DCC|DOE|20050829141336||ACK|1125342816253.100000055|P|2.3.1||||||||||DOE";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("MSH-4-1", "MSH-22-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponetToComponetDifferentSegment() throws HL7Exception {

        String testHl7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890|||||||||||||||20240516120000\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHl7);

        Hl7v2CopyOperation testClass = getCopyOperation("/MSH-7-1", "/PID-33-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponetToComponetDifferentSegmentNotExist() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890\r
GT1|||^John""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-5-2", "/GT1-3-2");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopySubComponetToSubComponetSameSegmentSameField() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe&Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-5-1-1", "/PID-5-1-2");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);

    }


    private Hl7v2CopyOperation getCopyOperation(String sourceField, String destinationField) {
        OperationCopyConfig config = new OperationCopyConfig();
        config.setSourceField(sourceField);
        config.setDestinationField(destinationField);
        return new Hl7v2CopyOperation(config);
    }

    private String getEncodedHl7FromString(String hl7String) throws HL7Exception{

        DefaultHapiContext context = new DefaultHapiContext();
        context.setValidationContext(new NoValidation());
        PipeParser parser = context.getPipeParser();
        Message expectedMessage = parser.parse(hl7String);
        return expectedMessage.encode();
    }

//
//    @Test
//    void testCopySubComponetToSubComponetSameSegmentDifferentField() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("PID-5-1-1");
//        message.setDestinationField("PID-5-3-2");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n"+ //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob&Doe||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" +//
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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
//    void testCopySubComponetToSubComponetDifferentSegment() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("PID-11-1-1");
//        message.setDestinationField("PV1-3-4-1");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n"+ //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^123 Main St|||||||||||||||||1234567890";
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
//    void testCopySubComponetToSubComponetDifferentSegmentNotExist() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("PID-5-1-1");
//        message.setDestinationField("GT1-3-1-1");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n"+ //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe";
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
//
//    @Test
//    void testCopyComponetToSubComponetSameFieldSameSegment() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("PID-5-1");
//        message.setDestinationField("PID-5-1-2");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n"+ //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe&Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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
//    void testCopyComponetToSubComponetDifferentFieldSameSegment() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("PID-5-1");
//        message.setDestinationField("PID-5-3-2");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n"+ //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob&Doe||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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
//    void testCopyComponetToSubComponetDifferentSegment() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("PID-5-1");
//        message.setDestinationField("PV1-3-4-1");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n"+ //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890";
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
//    void testCopyComponetToSubComponetDifferentSegmentNotExist() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("PID-5-1");
//        message.setDestinationField("GT1-3-1-1");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n"+ //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe";
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
//
//    @Test
//    void testCopyComponetToComponetSameRepeatSegment() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("GT1-3-1-1");
//        message.setDestinationField("GT1-4-1-1");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe|Doe";
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
//
//    @Test
//    void testCopyComponetToComponetDifferentRepeatSegment() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("GT1(0)-3-1-1");
//        message.setDestinationField("GT1(1)-4-1-1");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe\r\n" + //
//                "GT1|||Doe";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe\r\n" + //
//                "GT1|||Doe|Doe";
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
//    void testCopyComponetToComponetDifferentRepeatSegmentNotExist() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("GT1(0)-3-1-1");
//        message.setDestinationField("GT1(1)-3-1-1");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe\r\n" + //
//                "GT1|||Doe";
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
//
//    @Test
//    void testCopyComponetToComponetFullSyntax() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("/PATIENT/PV1-2");
//        message.setDestinationField("/PATIENT/PV1-4");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|I||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe";
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
//
//    @Test
//    void testCopyComponetToComponetShortSyntax() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("/.PV1-2");
//        message.setDestinationField("/.PV1-4");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1|I|Ward123^Room456^Bed789^Doe|I||||||||||||||||1234567890\r\n" + //
//                "GT1|||Doe";
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
//
//    @Test
//    void testCopyComponetToComponetFullSyntaxNotExist() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("/PID-1-1");
//        message.setDestinationField("/PATIENT/PV1-1-1");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "GT1|||Doe";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1\r\n" + //
//                "GT1|||Doe";
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
//
//    @Test
//    void testCopyComponetToComponetShortSyntaxNotExist() throws HL7Exception{
//        OperationCopyConfig message = new OperationCopyConfig();
//        message.setSourceField("/PID-1-1");
//        message.setDestinationField("/.PV1-1-1");
//        Hl7v2CopyOperation testClass = new Hl7v2CopyOperation(message);
//        String testHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "GT1|||Doe";
//        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r\n" + //
//                "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
//                "PV1|1\r\n" + //
//                "GT1|||Doe";
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

}
