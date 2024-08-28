package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexMatchTests {

    @ParameterizedTest
    @CsvSource(delimiterString = "~",
            value = {
                    "/MSH-3-1~MSH.3.1",
                    "/MSH-4-1~^([A-Z]{3})\\.([0-9]{1,})\\.([0-9]{1,})$",
                    "/PID-13-1~\\((\\d{3})\\) (\\d{3})-(\\d{4})",
                    "/PID-13-4~(\\w+)@(\\w+)\\.(com|org|net)?",
                    "/PID-13-4~^[A-Za-z0-9+_.-]+@(.+)$",
                    "/PID-6-1~\\w+(?:,\\w+)*",
                    "/PID-6-2~^(?!.*[0-9]).*$",
                    "/PID-19-1~^\\d{3}-?\\d{2}-?\\d{4}$",
                    "/PID-13-9~^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
            })
    void testTrueRegexes(String dataPath, String regex) throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());

        RegexMatch regexMatch = new RegexMatch(UUID.randomUUID(), dataPath, regex);

        assertTrue(
                regexMatch.evaluate(context)
        );
    }

    @ParameterizedTest
    @CsvSource(delimiterString = "~",
            value = {
                    "MSH.3.1~MSH.3.1",
                    "MSH.4.1~^([A-Z]{3})\\.([0-9]{1,})\\.([0-9]{1,})$",
                    "(123) 456-7890~\\((\\d{3})\\) (\\d{3})-(\\d{4})",
                    "info@example.com~(\\w+)@(\\w+)\\.(com|org|net)?",
                    "info@example.com~^[A-Za-z0-9+_.-]+@(.+)$",
                    "apple,banana,cherry~\\w+(?:,\\w+)*",
                    "Hello, World!~^(?!.*[0-9]).*$",
                    "111-23-4455~^\\d{3}-?\\d{2}-?\\d{4}$",
                    "192.168.0.1~^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
            })
    void testStateRegexes(String valueToTest, String regex) throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());
        context.getState().put("CONTEXT_KEY", valueToTest);
        RegexMatch regexMatch = new RegexMatch(UUID.randomUUID(), "state.CONTEXT_KEY", regex);

        assertTrue(
                regexMatch.evaluate(context)
        );
    }

    private String TestMessage1() {
        return "MSH|^~\\&|MSH.3.1|MSH.4.1|MSH.5.1|MSH.6.1|20240521111445|MSH.8.1|VXU^V04|MSH.10.1|D|2.5.1\r" +
                "PID|1||PID.3.1^^^PID.3.4.1&PID.3.4.2&HL7||PID.5.1.1|apple,banana,cherry^Hello, World!|||||||(123) 456-7890^^^info@example.com^^^^^192.168.0.1||||||111-23-4455\r" +
                "ORC|RE|ORC.2.1\r" +
                "RXA|1|1|20240521111910|20240521111916|03^RXA.5.2^CVX|0.5\r" +
                "OBX|1||64994-7^OBX.3.2.FIRST_REPETITION^LN||||||||F\r" +
                "OBX|2||30963-3^OBX.3.2.SECOND_REPETITION^LN||||||||F";
    }

}
