package gov.cdc.izgateway.xform.operations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.exceptions.SolutionOperationException;
import gov.cdc.izgateway.xform.solutions.SolutionOperation;

import static gov.cdc.izgateway.xform.TestUtils.*;

class SolutionOperationTests {
	public static final String TEST_INPUT_HL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|ReceivingSystem|ReceivingFacility|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890""";

	public static final String TEST_EXPECTED_HL7 = """
MSH|^~\\&|SendingSystem|SendingFacility|PCC|PCC|20240516120000||ADT^A08|MSGID12345|P|2.5.1\r
PID|1|1234567890|A1234567^^^HospitalA^MR||Doe^John^Jacob||19800101|M|||123 Main St^^Metropolis^IL^44130^USA||(555) 434-5543|||||1234567890\r
PV1|1|I|Ward123^Room456^Bed789|||||||||||||||||1234567890\r""".replace("\n","");
	@Test
	void test() throws HL7Exception, SolutionOperationException {
		// If these operations are executed in the right order, MSH 5 and 6 will both set to PCC
		Set opToExecuteFirst = getSetOperation("/MSH-5-1", "PCC");
		opToExecuteFirst.setOrder(0);
		Copy opToExecuteSecond = getCopyOperation("/MSH-5-1", "/MSH-6-1");
		opToExecuteSecond.setOrder(1);

		List<Operation> misorderedList = new ArrayList<>();
		misorderedList.add(opToExecuteSecond);
		misorderedList.add(opToExecuteFirst);
		gov.cdc.izgateway.xform.model.SolutionOperation modelOp = new gov.cdc.izgateway.xform.model.SolutionOperation();
		// Make a copy of the list when setting it.
		List<Operation> sortedList = new ArrayList<>(misorderedList);  // First make a copy of misordered list.
		modelOp.setOperationList(sortedList);
		SolutionOperation actualOp = new SolutionOperation(modelOp);
		// Now sortedList is actually sorted.
		
		// Verify the list is no longer misordered.
		assertNotEquals(sortedList.get(0), misorderedList.get(0));
		assertNotEquals(sortedList.get(1), misorderedList.get(1));

		ServiceContext serviceContext = new ServiceContext(UUID.randomUUID(), "", "", DataType.HL7V2, "", TEST_INPUT_HL7);
		actualOp.execute(serviceContext);
		
		String test = serviceContext.getCurrentMessage().encode();
        assertEquals(TEST_EXPECTED_HL7, test);
	}

}
