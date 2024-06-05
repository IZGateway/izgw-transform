package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static gov.cdc.izgateway.transformation.TestUtils.getCopyOperation;
import static gov.cdc.izgateway.transformation.TestUtils.getEncodedHl7FromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Hl7v2CopyOperationTests {

    @Test
    void testCopyComponentToComponentSameSegment() throws HL7Exception
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
    void testCopyComponentToComponentDifferentSegment() throws HL7Exception {

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
    void testCopyComponentToComponentDifferentSegmentNotExist() throws HL7Exception{

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
    void testCopySubComponentToSubComponentSameSegmentSameField() throws HL7Exception{

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

    @Test
    void testCopySubComponentToSubComponentSameSegmentDifferentField() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob&Doe||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-5-1-1", "/PID-5-3-2");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);

    }

    @Test
    void testCopySubComponentToSubComponentDifferentSegment() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^123 Main St|||||||||||||||||1234567890""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-11-1-1", "/PV1-3-4-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);

    }

    @Test
    void testCopySubComponentToSubComponentDifferentSegmentNotExist() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890\r
GT1|||Doe""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-5-1-1", "/GT1-3-1-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);

    }

    @Test
    void testCopyComponentToSubComponentSameFieldSameSegment() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe&Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-5-1", "/PID-5-1-2");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponentToSubComponentDifferentFieldSameSegment() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob&Doe||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-5-1", "/PID-5-3-2");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);

    }

    @Test
    void testCopyComponentToSubComponentDifferentSegment() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-5-1", "/PV1-3-4-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponentToSubComponentDifferentSegmentNotExist() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890\r
GT1|||Doe""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-5-1", "/GT1-3-1-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponentToComponentSameRepeatSegment() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r
GT1|||Doe""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r
GT1|||Doe|Doe""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/GT1-3-1-1", "/GT1-4-1-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponentToComponentDifferentRepeatSegment() throws HL7Exception{
        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r
GT1|||Doe\r
GT1|||Doe""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r
GT1|||Doe\r
GT1|||Doe|Doe""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/GT1(0)-3-1-1", "/GT1(1)-4-1-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponentToComponentDifferentRepeatSegmentNotExist() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r
GT1|||Doe""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r
GT1|||Doe\r
GT1|||Doe""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/GT1(0)-3-1-1", "/GT1(1)-3-1-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponentToComponentFullSyntax() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r
GT1|||Doe""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|I||||||||||||||||1234567890\r
GT1|||Doe""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PATIENT/PV1-2", "/PATIENT/PV1-4");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponentToComponentShortSyntax() throws HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890\r
GT1|||Doe""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|I||||||||||||||||1234567890\r
GT1|||Doe""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/.PV1-2", "/.PV1-4");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }


    @Test
    void testCopyComponentToComponentFullSyntaxNotExist() throws HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
GT1|||Doe""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1\r
GT1|||Doe""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-1-1", "/PATIENT/PV1-1-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

    @Test
    void testCopyComponentToComponentShortSyntaxNotExist() throws HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
GT1|||Doe""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1\r
GT1|||Doe""";

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, testHL7);

        Hl7v2CopyOperation testClass = getCopyOperation("/PID-1-1", "/.PV1-1-1");

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

}
