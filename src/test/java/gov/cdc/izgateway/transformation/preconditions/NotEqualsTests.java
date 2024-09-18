package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotEqualsTests {

    @ParameterizedTest
    @CsvSource({
            "/MSH-4-1,NOT-MSH.4.1",
            "/PID-3-4-2,NOT-PID.3.4.2",
            "/ORDER/ORC-2-1,NOT-ORC.2.1",
            "/ORDER/OBSERVATION(0)/OBX-3-2,NOT-OBX.3.2.FIRST_REPETITION",
            "/ORDER/OBSERVATION(1)/OBX-3-2,NOT_OBX.3.2.SECOND_REPETITION"
    })
    void testHl7NonMatchingValues(String dataPath, String comparisonValue) throws HL7Exception {
        // Testing fields against values they DO NOT contain so Hl7v2NotEquals.evaluate() should return true.
        assertTrue(
                runHl7NotEquals(dataPath,comparisonValue)
        );
    }

    @ParameterizedTest
    @CsvSource(delimiterString = "~", value = {
            "MSH.3.1~1.3.HSM",
            "(123) 456-7890~0987-654 )321(",
            "info@example.com~moc.elpmaxe@ofni",
            "apple,banana,cherry~yrrehc,ananab,elppa",
            "Hello, World!~!dlroW ,olleH",
            "111-23-4455~5544-32-111",
            "192.168.0.1~1.0.861.291"
    })
    void testStateNonMatchingValues(String contextValue, String valueToTest) throws HL7Exception {
        // Testing state field against values they DO NOT contain so NotEquals.evaluate() should return true.
        assertTrue(
                runStateNotEquals(contextValue, valueToTest)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "/MSH-4-1,MSH.4.1",
            "/PID-3-4-2,PID.3.4.2",
            "/ORDER/ORC-2-1,ORC.2.1",
            "/ORDER/OBSERVATION(0)/OBX-3-2,OBX.3.2.FIRST_REPETITION",
            "/ORDER/OBSERVATION(1)/OBX-3-2,OBX.3.2.SECOND_REPETITION"
    })
    void testHl7MatchingValues(String dataPath, String comparisonValue) throws HL7Exception {
        // Testing fields against values they actually contain so Hl7v2NotEquals.evaluate() should return false.
        assertFalse(
            runHl7NotEquals(dataPath,comparisonValue)
        );
    }

    @ParameterizedTest
    @CsvSource(delimiterString = "~", value = {
            "MSH.3.1~MSH.3.1",
            "(123) 456-7890~(123) 456-7890",
            "info@example.com~info@example.com",
            "apple,banana,cherry~apple,banana,cherry",
            "Hello, World!~Hello, World!",
            "111-23-4455~111-23-4455",
            "192.168.0.1~192.168.0.1"
    })
    void testStateMatchingValues(String contextValue, String valueToTest) throws HL7Exception {
        // Testing state field against values they actually contain so NotEquals.evaluate() should return false.
        assertFalse(
                runStateNotEquals(contextValue, valueToTest)
        );

    }

    @ParameterizedTest
    @CsvSource({
            "/MSH-14-1,COMPONENT_NO_EXIST",
            "/ZCD-1-1,SEGMENT_NO_EXIST",
            "/PID-3-4-4,SUBCOMPONENT_NO_EXIST",
            "/ORDER/OBSERVATION(2)/OBX-3-2,REPETITION_NO_EXIST"
    })
    void testHl7FieldDoesNotExist(String dataPath, String comparisonValue) throws HL7Exception {
        assertTrue(
                runHl7NotEquals(dataPath, comparisonValue)
        );
    }

    @Test
    void testStateFieldDoesNotExist() throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());
        context.getState().put("CONTEXT_KEY", "WHATEVER");

        NotEquals neq = new NotEquals();
        neq.setId(UUID.randomUUID());
        neq.setDataPath("state.CONTEXT_KEY_2");
        neq.setComparisonValue("WHATEVER");

        assertTrue(
                neq.evaluate(context)
        );

    }

    private String TestMessage1() {
        return "MSH|^~\\&|MSH.3.1|MSH.4.1|MSH.5.1|MSH.6.1|20240521111445|MSH.8.1|VXU^V04|MSH.10.1|D|2.5.1\r" +
                "PID|1||PID.3.1^^^PID.3.4.1&PID.3.4.2&HL7||PID.5.1.1\r" +
                "ORC|RE|ORC.2.1\r" +
                "RXA|1|1|20240521111910|20240521111916|03^RXA.5.2^CVX|0.5\r" +
                "OBX|1||64994-7^OBX.3.2.FIRST_REPETITION^LN||||||||F\r" +
                "OBX|2||30963-3^OBX.3.2.SECOND_REPETITION^LN||||||||F";
    }

    private Boolean runStateNotEquals(String contextValue, String valueToTest) throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());
        context.getState().put("CONTEXT_KEY", contextValue);

        NotEquals neq = new NotEquals();
        neq.setId(UUID.randomUUID());
        neq.setDataPath("state.CONTEXT_KEY");
        neq.setComparisonValue(valueToTest);

        return neq.evaluate(context);
    }

    private Boolean runHl7NotEquals(String dataPath, String comparisonValue) throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());

        Hl7v2NotEquals ne = new Hl7v2NotEquals();
        ne.setDataPath(dataPath);
        ne.setComparisonValue(comparisonValue);

        return ne.evaluate(context);
    }
}
