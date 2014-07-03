package co.smartreceipts.android.filters;

import co.smartreceipts.android.model.ReceiptRow;

public interface ReceiptFilter {
	
	/**
	 * Apply a filter operation in order to determine if we should accept this particular receipt as part
	 * of our output total.
	 * @param receipt - the {@link ReceiptRow} to check
	 * @return {@link true} if it should be accepted, {@link false} otherwise
	 */
	public boolean accept(ReceiptRow receipt);

}
