package gov.cdc.izgateway.xform.logging.advice;

import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.xform.logging.XformTransactionData;

/**
 * Holds the {@link XformTransactionData} of the current thread, to which the advice aspect adds advice.
 */
public class XformAdviceCollector {
	private XformAdviceCollector() {}
	private static final ThreadLocal<XformTransactionData> transactionDataVar = new ThreadLocal<>();

    // TODO - See below simplify by casting to XformTransactionData from RequestContext.getTransactionData()
    // when I set this with the dervied, on read, it should still be the derived.
	/**
	 * Returns the transaction data of the current thread.
	 *
	 * @return the transaction data, or null if none is set
	 */
	public static XformTransactionData getTransactionData() {
		return transactionDataVar.get();
	}

	/**
	 * Sets the transaction data of the current thread and of the {@link RequestContext}. A null value clears both.
	 *
	 * @param transactionData the transaction data, or null to clear
	 */
	public static void setTransactionData(XformTransactionData transactionData) {
        RequestContext.setTransactionData(transactionData);
		if (transactionData == null) {
			clear();
			return;
		}
		transactionDataVar.set(transactionData);
	}

	/**
	 * Removes the transaction data of the current thread and clears the {@link RequestContext}.
	 */
	public static void clear() {
		transactionDataVar.remove();
        RequestContext.clear();
	}
}
