package gov.cdc.izgateway.transformation.logging.advice;

import gov.cdc.izgateway.logging.RequestContext;

public class XformAdviceCollector {
	private XformAdviceCollector() {}
	private static final ThreadLocal<XformTransactionData> transactionDataVar = new ThreadLocal<>();

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
