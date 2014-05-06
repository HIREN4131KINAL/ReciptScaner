package co.smartreceipts.android.adapters;

import android.content.Context;
import android.widget.TextView;
import co.smartreceipts.android.R;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.persistence.Preferences;

public class TripCardAdapter extends CardAdapter<TripRow> {

	public TripCardAdapter(Context context, Preferences preferences) {
		super(context, preferences);
	}
	
	@Override
	protected String getPrice(TripRow data) {
		return data.getCurrencyFormattedPrice();
	}
	
	@Override
	protected void setPriceTextView(TextView textView, TripRow data) {
		textView.setText(getPrice(data));
	}
	
	@Override
	protected void setNameTextView(TextView textView, TripRow data) {
		textView.setText(data.getName());
	}
	
	@Override
	protected void setDateTextView(TextView textView, TripRow data) {
		final String dateSeparator = getPreferences().getDateSeparator();
		final String from = data.getFormattedStartDate(getContext(), dateSeparator);
		final String to = data.getFormattedEndDate(getContext(), dateSeparator);
		textView.setText(from + getContext().getString(R.string.trip_adapter_list_item_to) + to);
	}

}
