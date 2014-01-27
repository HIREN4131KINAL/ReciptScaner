package co.smartreceipts.android.fragments;

import android.content.Context;
import co.smartreceipts.android.R;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.persistence.Preferences;

public class TripCardAdapter extends CardAdapter<TripRow> {

	public TripCardAdapter(Context context, Preferences preferences) {
		super(context, preferences);
	}
	
	@Override
	protected String getName(TripRow data) {
		return data.getName();
	}
	
	@Override
	protected String getPrice(TripRow data) {
		return data.getCurrencyFormattedPrice();
	}
	
	@Override
	protected String getDate(TripRow data, Context context, String dateSeparator) {
		final String from = data.getFormattedStartDate(context, dateSeparator);
		final String to = data.getFormattedEndDate(context, dateSeparator);
		return from + context.getString(R.string.trip_adapter_list_item_to) + to;
	}

}
