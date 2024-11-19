package gov.cdc.izgateway.xform.preconditions;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExistsTests {

    @ParameterizedTest
    @CsvSource({
            "/MSH-4-1",
            "/PID-3-4-2",
            "/ORDER/ORC-2-1",
            "/ORDER/OBSERVATION(0)/OBX-3-2",
            "/ORDER/OBSERVATION(1)/OBX-3-2"
    })
    void testHl7True(String dataPath) throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());

        Hl7v2Exists exists = new Hl7v2Exists();
        exists.setDataPath(dataPath);

        assertTrue(
                exists.evaluate(context)
        );
    }

    @Test
    void testStateTrue() throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());
        context.getState().put("CONTEXT_KEY", "CONTEXT_VALUE");

        Exists exists = new Exists();
        exists.setId(UUID.randomUUID());
        exists.setDataPath("state.CONTEXT_KEY");

        assertTrue(
                exists.evaluate(context)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "/MSH-14-1",
            "/ZCD-1-1",
            "/PID-3-4-4",
            "/ORDER/OBSERVATION(2)/OBX-3-2"
    })
    void testHl7False(String dataPath) throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());

        Hl7v2Exists exists = new Hl7v2Exists();
        exists.setDataPath(dataPath);

        assertFalse(
                exists.evaluate(context)
        );
    }

    @Test
    void testStateFalse() throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());
        context.getState().put("CONTEXT_KEY", "CONTEXT_VALUE");

        Exists exists = new Exists();
        exists.setId(UUID.randomUUID());
        exists.setDataPath("state.CONTEXT_KEY_2");

        assertFalse(
                exists.evaluate(context)
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
}
