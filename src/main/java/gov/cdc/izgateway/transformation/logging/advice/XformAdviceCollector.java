package gov.cdc.izgateway.transformation.logging.advice;

public class XformAdviceCollector {
	private XformAdviceCollector() {}
	private static ThreadLocal<XformTransactionData> transactionDataVar = new ThreadLocal<>();

	public static XformTransactionData getTransactionData() {
		return transactionDataVar.get();
	}

	public static void setTransactionData(XformTransactionData transactionData) {
		if (transactionData == null) {
			clear();
			return;
		}
		transactionDataVar.set(transactionData);
	}

	public static void clear() {
		transactionDataVar.remove();
	}
}
