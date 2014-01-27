package co.smartreceipts.android.fragments;

import android.content.Context;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.persistence.Preferences;

public class ReceiptCardAdapter extends CardAdapter<ReceiptRow> {

	public ReceiptCardAdapter(Context context, Preferences preferences) {
		super(context, preferences);
	}
	
	@Override
	protected String getName(ReceiptRow data) {
		return data.getName();
	}
	
	@Override
	protected String getPrice(ReceiptRow data) {
		return data.getCurrencyFormattedPrice();
	}
	
	@Override
	protected String getDate(ReceiptRow data, Context context, String dateSeparator) {
		return data.getFormattedDate(context, dateSeparator);
	}

}
