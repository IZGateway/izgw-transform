package gov.cdc.izgateway.transformation.logging.advice;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.transformation.logging.XformTransactionData;

public class XformAdviceCollector {
	private XformAdviceCollector() {}
	private static final ThreadLocal<XformTransactionData> transactionDataVar = new ThreadLocal<>();

    // TODO - See below simplify by casting to XformTransactionData from RequestContext.getTransactionData()
    // when I set this with the dervied, on read, it should still be the derived.
	public static XformTransactionData getTransactionData() {
		return transactionDataVar.get();
	}

	public static void setTransactionData(XformTransactionData transactionData) {
        RequestContext.setTransactionData(transactionData);
		if (transactionData == null) {
			clear();
			return;
		}
		transactionDataVar.set(transactionData);
	}

	public static void clear() {
		transactionDataVar.remove();
        RequestContext.clear();
	}
}
