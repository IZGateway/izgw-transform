package gov.cdc.izgateway.xform.operations;

import ca.uhn.hl7v2.HL7Exception;

import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static gov.cdc.izgateway.xform.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Hl7V2RegexReplaceTests {

    @Test
    void testReplaceNonAlphaNumExistingComponent() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||5554345543|||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/PID-13-1", "[^a-zA-Z0-9]", "");

    }

    @Test
    void testReplaceNonAlphaNumExistingSubComponent() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||!@#$%*  ()_+^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/PID-5", "[^a-zA-Z0-9]", "");

    }

    @Test
    void testReplaceNonAlphaNumNonExistingComponent() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/PID-4", "[^a-zA-Z0-9]", "");

    }

    @Test
    void testReplaceNonAlphaNumNonExistingSubComponet() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/PV1-3-4-1", "[^a-zA-Z0-9]", "");

    }

    @Test
    void testReplaceNonAlphaNumExistingComponentNoChange() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|LettersAndNumbers12345Only|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|LettersAndNumbers12345Only|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/MSH-4", "[^a-zA-Z0-9]", "");

    }

    @Test
    void testAddHyphenExistingComponent() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|376040001|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|37604-0001|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/PID-2", "^(\\d{5})(\\d{4})$", "$1-$2");

    }

    @Test
    void testAddHyphenExistingComponentTooShort() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|37604|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|37604|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/PID-2", "^(\\d{5})(\\d{4})$", "$1-$2");

    }

    @Test
    void testAddHyphenExistingComponentToShort2() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|376041|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|376041|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/PID-2", "^(\\d{5})(\\d{4})$", "$1-$2");

    }

    @Test
    void testAddHyphenNonExistingComponent() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/PID-5", "^(\\d{5})(\\d{4})$", "$1-$2");

    }

    @Test
    void testAddHyphenNonExistingSubComponet() throws OperationException, HL7Exception {

        String testHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        String expectedHL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

        runRegexTrueTest(testHL7, expectedHL7, "/PV1-3-4-1", "^(\\d{5})(\\d{4})$", "$1-$2");
    }

    private void runRegexTrueTest(String testHL7, String expectedHL7, String field, String regex, String replacement) throws OperationException, HL7Exception {
        ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", testHL7);

        RegexReplace testClass = getRegexOperation(field, regex, replacement);

        String expected = getEncodedHl7FromString(expectedHL7);

        testClass.execute(serviceContext);

        String test = serviceContext.getCurrentMessage().encode();
        assertEquals(expected, test);

    }

}
