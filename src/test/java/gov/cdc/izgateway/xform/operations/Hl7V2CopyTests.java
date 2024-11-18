package gov.cdc.izgateway.xform.operations;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static gov.cdc.izgateway.xform.TestUtils.getCopyOperation;
import static gov.cdc.izgateway.xform.TestUtils.getEncodedHl7FromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Hl7V2CopyTests {

    @Test
    void testCopyComponentToComponentSameSegment() throws OperationException, HL7Exception
    {
        String testHL7  ="MSH|^~\\&||DOE|DCC|DOE|20050829141336||ACK|1125342816253.100000055|P|2.3.1";
        String expectedHL7 = "MSH|^~\\&||DOE|DCC|DOE|20050829141336||ACK|1125342816253.100000055|P|2.3.1||||||||||DOE";

        runCopyTrueTest(testHL7, expectedHL7, "/MSH-4-1", "/MSH-22-1");
    }

    @Test
    void testCopyComponentToComponentDifferentSegment() throws OperationException, HL7Exception {

        String testHl7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890|||||||||||||||20240516120000\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runCopyTrueTest(testHl7, expectedHL7, "/MSH-7-1", "/PID-33-1");

    }

    @Test
    void testCopyComponentToComponentDifferentSegmentNotExist() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890\r
GT1|||^John""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-5-2", "/GT1-3-2");
    }

    @Test
    void testCopySubComponentToSubComponentSameSegmentSameField() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe&Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-5-1-1", "/PID-5-1-2");
    }

    @Test
    void testCopySubComponentToSubComponentSameSegmentDifferentField() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob&Doe||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-5-1-1", "/PID-5-3-2");
    }

    @Test
    void testCopySubComponentToSubComponentDifferentSegment() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^123 Main St|||||||||||||||||1234567890""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-11-1-1", "/PV1-3-4-1");
    }

    @Test
    void testCopySubComponentToSubComponentDifferentSegmentNotExist() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890\r
GT1|||Doe""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-5-1-1", "/GT1-3-1-1");

    }

    @Test
    void testCopyComponentToSubComponentSameFieldSameSegment() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe&Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-5-1", "/PID-5-1-2");
    }

    @Test
    void testCopyComponentToSubComponentDifferentFieldSameSegment() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob&Doe||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-5-1", "/PID-5-3-2");
    }

    @Test
    void testCopyComponentToSubComponentDifferentSegment() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789^Doe|||||||||||||||||1234567890""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-5-1", "/PV1-3-4-1");
    }

    @Test
    void testCopyComponentToSubComponentDifferentSegmentNotExist() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890\r
GT1|||Doe""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-5-1", "/GT1-3-1-1");

    }

    @Test
    void testCopyComponentToComponentSameRepeatSegment() throws OperationException, HL7Exception {

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

        runCopyTrueTest(testHL7, expectedHL7, "/GT1-3-1-1", "/GT1-4-1-1");

    }

    @Test
    void testCopyComponentToComponentDifferentRepeatSegment() throws OperationException, HL7Exception {
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

        runCopyTrueTest(testHL7, expectedHL7, "/GT1(0)-3-1-1", "/GT1(1)-4-1-1");

    }

    @Test
    void testCopyComponentToComponentDifferentRepeatSegmentNotExist() throws OperationException, HL7Exception {

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

        runCopyTrueTest(testHL7, expectedHL7, "/GT1(0)-3-1-1", "/GT1(1)-3-1-1");

    }

    @Test
    void testCopyComponentToComponentFullSyntax() throws OperationException, HL7Exception {

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

        runCopyTrueTest(testHL7, expectedHL7, "/PATIENT/PV1-2", "/PATIENT/PV1-4");

    }

    @Test
    void testCopyComponentToComponentShortSyntax() throws OperationException, HL7Exception {

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

        runCopyTrueTest(testHL7, expectedHL7, "/.PV1-2", "/.PV1-4");

    }


    @Test
    void testCopyComponentToComponentFullSyntaxNotExist() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
GT1|||Doe""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1\r
GT1|||Doe""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-1-1", "/PATIENT/PV1-1-1");

    }

    @Test
    void testCopyComponentToComponentShortSyntaxNotExist() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
GT1|||Doe""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||VXU^V04|MSGID12345|P|2.5.1|||ER|AL\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1\r
GT1|||Doe""";

        runCopyTrueTest(testHL7, expectedHL7, "/PID-1-1", "/.PV1-1-1");

    }

    private void runCopyTrueTest(String testHL7, String expectedHL7, String sourceField, String destinationField) throws OperationException, HL7Exception {

        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", testHL7);

        Copy testClass = getCopyOperation(sourceField, destinationField);

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

}
