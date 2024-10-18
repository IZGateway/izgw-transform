package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Hl7V2SaveStateTest {

    @ParameterizedTest
    @CsvSource({
            "/MSH-9-2,TriggerEvent,V04",
            "/PID-3-4-2,PID.3.4.2,PID.3.4.2",
            "/ORDER/ORC-2-1,ORC.2.1,ORC.2.1",
            "/ORDER/OBSERVATION(0)/OBX-3-2,OBX.3.2.FIRST_REPETITION,OBX.3.2.FIRST_REPETITION",
            "/ORDER/OBSERVATION(1)/OBX-3-2,OBX.3.2.SECOND_REPETITION,OBX.3.2.SECOND_REPETITION"
    })
    void testSaveState(String field, String key, String expected) throws OperationException, HL7Exception {
        ServiceContext context = new ServiceContext(UUID.randomUUID(),"","", DataType.HL7V2, "", TestMessage1());

        SaveState baseSaveState = new SaveState();
        baseSaveState.setField(field);
        baseSaveState.setKey(key);

        Hl7v2SaveState testClass = new Hl7v2SaveState(baseSaveState);
        testClass.execute(context);

        assertEquals(expected, context.getState().get(key));
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
