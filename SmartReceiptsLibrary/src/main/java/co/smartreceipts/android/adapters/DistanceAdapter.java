package co.smartreceipts.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.DistanceRow;
import co.smartreceipts.android.persistence.Preferences;

public class DistanceAdapter extends CardAdapter<DistanceRow> {

   public DistanceAdapter(Context context, Preferences preferences) {
       super(context, preferences);
   }

    @Override
    protected void setPriceTextView(TextView textView, DistanceRow data) {
        textView.setText(data.getDecimalFormattedDistance());
    }

    @Override
    protected void setNameTextView(TextView textView, DistanceRow data) {
        if (!TextUtils.isEmpty(data.getLocation())) {
            textView.setText(data.getLocation());
            textView.setVisibility(View.VISIBLE);
        }
        else if (!TextUtils.isEmpty(data.getComment())) {
            textView.setText(data.getComment());
            textView.setVisibility(View.VISIBLE);
        }
        else {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setDateTextView(TextView textView, DistanceRow data) {
        textView.setText(data.getFormattedDate(getContext(), getPreferences().getDateSeparator()));
    }
}
