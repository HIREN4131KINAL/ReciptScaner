package co.smartreceipts.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.persistence.Preferences;

public class DistanceAdapter extends CardAdapter<Distance> {

    public DistanceAdapter(Context context, Preferences preferences) {
        super(context, preferences);
    }

    @Override
    protected String getPrice(Distance data) {
        return data.getDecimalFormattedDistance();
    }

    @Override
    protected void setPriceTextView(TextView textView, Distance data) {
        textView.setText(data.getDecimalFormattedDistance());
    }

    @Override
    protected void setNameTextView(TextView textView, Distance data) {
        if (!TextUtils.isEmpty(data.getLocation())) {
            textView.setText(data.getLocation());
            textView.setVisibility(View.VISIBLE);
        } else if (!TextUtils.isEmpty(data.getComment())) {
            textView.setText(data.getComment());
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setCategory(TextView textView, Distance data) {
        textView.setText(data.getFormattedDate(getContext(), getPreferences().getDateSeparator()));
    }

    @Override
    protected void setDateTextView(TextView textView, Distance data) {
        textView.setText(data.getPrice().getCurrencyFormattedPrice());
    }
}
