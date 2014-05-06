package co.smartreceipts.android.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.persistence.Preferences;

public class ReceiptCardAdapter extends CardAdapter<ReceiptRow> {

	public ReceiptCardAdapter(Context context, Preferences preferences) {
		super(context, preferences);
	}
	
	@Override
	protected String getPrice(ReceiptRow data) {
		return data.getCurrencyFormattedPrice();
	}
	
	@Override
	protected void setPriceTextView(TextView textView, ReceiptRow data) {
		textView.setText(getPrice(data));
	}
	
	@Override
	protected void setNameTextView(TextView textView, ReceiptRow data) {
		textView.setText(data.getName());
	}
	
	@Override
	protected void setDateTextView(TextView textView, ReceiptRow data) {
		if (getPreferences().isShowDate()) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getFormattedDate(getContext(), getPreferences().getDateSeparator()));
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void setCategory(TextView textView, ReceiptRow data) {
		if (getPreferences().isShowCategory()) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getCategory());
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void setMarker(TextView textView, ReceiptRow data) {
		if (getPreferences().isShowPhotoPDFMarker()) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getMarkerAsString(getContext()));
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}

}
