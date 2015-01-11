package co.smartreceipts.android.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.Preferences;

public class ReceiptCardAdapter extends CardAdapter<Receipt> {

	public ReceiptCardAdapter(Context context, Preferences preferences) {
		super(context, preferences);
	}
	
	@Override
	protected String getPrice(Receipt data) {
		return data.getPrice().getCurrencyFormattedPrice();
	}
	
	@Override
	protected void setPriceTextView(TextView textView, Receipt data) {
		textView.setText(getPrice(data));
	}
	
	@Override
	protected void setNameTextView(TextView textView, Receipt data) {
		textView.setText(data.getName());
	}
	
	@Override
	protected void setDateTextView(TextView textView, Receipt data) {
		if (getPreferences().isShowDate()) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getFormattedDate(getContext(), getPreferences().getDateSeparator()));
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void setCategory(TextView textView, Receipt data) {
		if (getPreferences().isShowCategory()) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getCategory());
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void setMarker(TextView textView, Receipt data) {
		if (getPreferences().isShowPhotoPDFMarker()) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getMarkerAsString(getContext()));
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}

}
