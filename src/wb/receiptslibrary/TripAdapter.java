package wb.receiptslibrary;

import wb.android.ui.ListItemView;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TripAdapter extends BaseAdapter {
	
	@SuppressWarnings("unused") private static final boolean D = SmartReceiptsActivity.D;
	@SuppressWarnings("unused") private static final String TAG = "TripsAdapter";
	
	//Number of Headers (i.e. New Trip)
	private static final int MAIN_HEADERS = 1;
	
	private TripRow[] trips;
	private final TripViewHolder tripHolder;
	private int largestWidth;
	private final LayoutInflater inflater;
	
	public TripAdapter(final TripViewHolder tripHolder, final TripRow[] trips) {
		this.trips = trips;
		this.tripHolder = tripHolder;
		largestWidth = layoutWidthHack();
		inflater = LayoutInflater.from(tripHolder.getActivity());
	}
	
	public int getCount() {
		return trips.length + MAIN_HEADERS;
	}
	
	public TripRow getItem(final int i) {
		if (i==0)
			return null;
		else
			return trips[i-MAIN_HEADERS];
	}
	
	public long getItemId(final int i) {
		return i;
	}
	
	// Considered optimizing with convertView at some point: http://android-decoded.blogspot.com/2011/12/so-what-exactly-is-convertview.html
	public View getView(final int i, View convertView, ViewGroup parent) {
		if (i == 0) {
			convertView = inflater.inflate(R.layout.list_item_trip_add, null);
			convertView.setOnClickListener(new NewTripClickListener(tripHolder));
		}
		else {
			TripRow trip = trips[i-MAIN_HEADERS];
			final String currency = SRUtils.CurrencyValue(trip.price, trip.currency);
			final String from = DateFormat.getDateFormat(tripHolder.getActivity()).format(trip.from);
			final String to = DateFormat.getDateFormat(tripHolder.getActivity()).format(trip.to); 
			convertView = new ListItemView(tripHolder.getActivity(), currency, largestWidth, trip.dir.getName(), from + tripHolder.getActivity().getFlex().getString(R.string.trip_adapter_list_item_to) + to);
			convertView.setOnClickListener(new ViewTripClickListener(tripHolder, trip));
			convertView.setOnLongClickListener(new EditTripClickListener(tripHolder, trip));
		}
		return convertView;
	}
	
	public final void notifyDataSetChanged(final TripRow[] trips) {
		this.trips = trips;
		largestWidth = layoutWidthHack();
		super.notifyDataSetChanged();
	}
	
	private final int layoutWidthHack() {
		int width = 0;
		final int size = trips.length;
		int curr, decIdx;
		String price;
		for (int i = 0; i < size; i++) {
			price = trips[i].price;
			if (trips[i].currency != null)
				price = trips[i].currency.format(price); 				
			decIdx = price.indexOf(".");
			if (decIdx == -1)
				curr = price.length() + 3;
			else 
				curr = decIdx + 3;
			if (curr > width)
				width = curr;
		}
		return width*((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, tripHolder.getActivity().getResources().getDisplayMetrics()));
	}
	
	private final class NewTripClickListener implements OnClickListener {
		private final TripViewHolder mTripHolder;
		public NewTripClickListener(final TripViewHolder tripHolder) {this.mTripHolder = tripHolder;}
		@Override public final void onClick(final View v) {mTripHolder.tripMenu(null);}
	}
	
	private final class ViewTripClickListener implements OnClickListener {
		private final TripViewHolder mTripHolder;
		private final TripRow mTrip;
		public ViewTripClickListener(final TripViewHolder tripHolder, final TripRow trip) {this.mTripHolder = tripHolder; this.mTrip = trip;}
		@Override public final void onClick(final View v) {((HomeHolder)mTripHolder.getParent()).viewTrip(mTrip);}
	}
	
	private final class EditTripClickListener implements OnLongClickListener {
		private final TripViewHolder mTripHolder;
		private final TripRow mTrip;
		public EditTripClickListener(final TripViewHolder tripHolder, final TripRow trip) {this.mTripHolder = tripHolder; this.mTrip = trip;}
		@Override public final boolean onLongClick(final View v) {return mTripHolder.editTrip(mTrip);}
	}
	
}
