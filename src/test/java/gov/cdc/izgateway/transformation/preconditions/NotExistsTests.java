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

class NotExistsTests {

    @ParameterizedTest
    @CsvSource({
            "/MSH-14-1",
            "/ZCD-1-1",
            "/PID-3-4-4",
            "/ORDER/OBSERVATION(2)/OBX-3-2"
    })
    void hl7TestWhereNotExists(String dataPath) throws HL7Exception {
        assertTrue(
                runHl7NotExists(dataPath)
        );

    }

    @Test
    void stateTestWhereNotExists() throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());
        assertTrue(
                runStateNotExists(context, "KEY")
        );
    }

    @ParameterizedTest
    @CsvSource({
            "/MSH-4-1",
            "/PID-3-4-2",
            "/ORDER/ORC-2-1",
            "/ORDER/OBSERVATION(0)/OBX-3-2",
            "/ORDER/OBSERVATION(1)/OBX-3-2"
    })
    void hl7TestWhereExists(String dataPath) throws HL7Exception {
        assertFalse(
                runHl7NotExists(dataPath)
        );
    }

    @Test
    void stateTestWhereExists() throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());
        context.getState().put("KEY", "VALUE");
        assertFalse(
                runStateNotExists(context, "state.KEY")
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

    private Boolean runHl7NotExists(String dataPath) throws HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());
        Hl7v2NotExists notExists = new Hl7v2NotExists();
        notExists.setDataPath(dataPath);
        return notExists.evaluate(context);
    }

    private Boolean runStateNotExists(ServiceContext context, String dataPath) {
        NotExists ne = new NotExists();
        ne.setId(UUID.randomUUID());
        ne.setDataPath(dataPath);
        return ne.evaluate(context);
    }
}
