package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static gov.cdc.izgateway.transformation.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Hl7v2SetOperationTests {

    @Test
    void testSetComponentExistingField() throws HL7Exception {

        String testHL7 = """
                MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
                PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
                PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
                MSH|^~\\&|SendingSystem|PCC|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
                PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
                PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runSetTrueTest(testHL7, expectedHL7, "/MSH-4-1", "PCC");
    }

    @Test
    void testSetComponentNonExistingField() throws HL7Exception {

        String testHL7 = """
                MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
                PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
                PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
                MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1||||||||||Sample\r
                PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
                PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runSetTrueTest(testHL7, expectedHL7, "/MSH-22-1", "Sample");

    }

    @Test
    void testSetComponentNonExistingSegment() throws HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runSetTrueTest(testHL7, expectedHL7, "/PID-1", "1");

    }

    @Test
    void testSetSubComponentExistingField() throws HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
PV1|1|I|1^Room456^Bed789|||||||||||||||||1234567890""";

        runSetTrueTest(testHL7, expectedHL7, "/PV1-3-1-1", "1");

    }

    @Test
    void testSetSubComponentNonExistingField() throws HL7Exception{

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
PV1|1|I|Ward123&1^Room456^Bed789|||||||||||||||||1234567890""";

        runSetTrueTest(testHL7, expectedHL7, "/PV1-3-1-2", "1");
    }

    private void runSetTrueTest(String testHL7, String expectedHL7, String destinationField, String setValue) throws HL7Exception {
        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(), "", "", DataType.HL7V2, "", testHL7);

        Hl7v2SetOperation testClass = getSetOperation(destinationField, setValue);

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);
    }

}
