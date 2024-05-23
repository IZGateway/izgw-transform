package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.configuration.OperationRegexReplaceConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Hl7v2RegexReplaceOperationTests {

    @Test
    void testReplaceNonAlphaNumExistingComponet() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("PID-13-1");
        message.setRegex("[^a-zA-Z0-9]");
        message.setReplacement("");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||5554345543|||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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

    @Test
    void testReplaceNonAlphaNumExistingSubComponet() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("PID-5");
        message.setRegex("[^a-zA-Z0-9]");
        message.setReplacement("");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||!@#$%*  ()_+^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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

    @Test
    void testReplaceNonAlphaNumNonExistingComponet() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("PID-4");
        message.setRegex("[^a-zA-Z0-9]");
        message.setReplacement("");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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

    @Test
    void testReplaceNonAlphaNumNonExistingSubComponet() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("PV1-3-4-1");
        message.setRegex("[^a-zA-Z0-9]");
        message.setReplacement("");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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

    @Test
    void testReplaceNonAlphaNumExistingComponetNoChange() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("MSH-4");
        message.setRegex("[^a-zA-Z0-9]");
        message.setReplacement("");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|LettersAndNumbers12345Only|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|LettersAndNumbers12345Only|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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

    @Test
    void testAddHyphenExistingComponet() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("PID-2");
        message.setRegex("^(\\d{5})(\\d{4})$");
        message.setReplacement("$1-$2");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|376040001|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
        "PID|1|37604-0001|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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
    @Test
    void testAddHyphenExistingComponetTooShort() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("PID-2");
        message.setRegex("^(\\d{5})(\\d{4})$");
        message.setReplacement("$1-$2");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|37604|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
        "PID|1|37604|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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

    @Test
    void testAddHyphenExistingComponetToShort2() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("PID-2");
        message.setRegex("^(\\d{5})(\\d{4})$");
        message.setReplacement("$1-$2");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|376041|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
        "PID|1|376041|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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
 
    @Test
    void testAddHyphenNonExistingComponet() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("PID-5");
        message.setRegex("^(\\d{5})(\\d{4})$");
        message.setReplacement("$1-$2");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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

    @Test
    void testAddHyphenNonExistingSubComponet() throws HL7Exception{
        OperationRegexReplaceConfig message = new OperationRegexReplaceConfig();
        message.setField("PV1-3-4-1");
        message.setRegex("^(\\d{5})(\\d{4})$");
        message.setReplacement("$1-$2");
        Hl7v2RegexReplaceOperation testClass = new Hl7v2RegexReplaceOperation(message);
        String testHL7  ="MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
                        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
                        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
        String expectedHL7 = "MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r\n" + //
        "PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA|||||||1234567890\r\n" + //
        "PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890";
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
