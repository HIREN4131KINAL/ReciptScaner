package wb.receiptslibrary.fragments;

import wb.android.ui.ListItemView;
import wb.receiptslibrary.R;
import wb.receiptslibrary.model.TripRow;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TripAdapter extends BaseAdapter {
	
	@SuppressWarnings("unused") private static final String TAG = "TripsAdapter";
	
	//Number of Headers (i.e. New Trip)
	private static final int MAIN_HEADERS = 1;
	
	private TripFragment mFragment;
	private TripRow[] trips;
	private float largestWidth;
	private final LayoutInflater inflater;
	
	public TripAdapter(final TripFragment fragment, final TripRow[] trips) {
		this.trips = trips;
		mFragment = fragment;
		largestWidth = layoutWidthHack();
		inflater = LayoutInflater.from(fragment.getSherlockActivity());
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
	
	// Considered optimizing with convertView at some point
	public View getView(final int i, View convertView, ViewGroup parent) {
		if (i == 0) {
			convertView = inflater.inflate(R.layout.list_item_trip_add, null);
			convertView.setOnClickListener(new NewTripClickListener(mFragment));
		}
		else {
			TripRow trip = trips[i-MAIN_HEADERS];
			final String from = trip.getFormattedStartDate(mFragment.getSherlockActivity(), mFragment.getPersistenceManager().getPreferences().getDateSeparator());
			final String to = trip.getFormattedEndDate(mFragment.getSherlockActivity(), mFragment.getPersistenceManager().getPreferences().getDateSeparator()); 
			convertView = new ListItemView(mFragment.getSherlockActivity(), 
										   trip.getCurrencyFormattedPrice(), 
										   (int) largestWidth, 
										   trip.getName(), 
										   from + mFragment.getFlexString(R.string.trip_adapter_list_item_to) + to,
										   R.drawable.default_selector);
			convertView.setOnClickListener(new ViewTripClickListener(mFragment, trip));
			convertView.setOnLongClickListener(new EditTripClickListener(mFragment, trip));
		}
		return convertView;
	}
	
	public final void notifyDataSetChanged(final TripRow[] trips) {
		this.trips = trips;
		largestWidth = layoutWidthHack();
		super.notifyDataSetChanged();
	}
	
	/*
	private final float layoutWidthHack() {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(24);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		String currString;
		float maxWidth = -1f, currWidth;
		for (int i = 0; i < trips.length; i++) {
			currString = trips[i].getCurrencyFormattedPrice();
			if (currString != null) {
				currWidth = paint.measureText(currString);
				if (currWidth > maxWidth) {
					maxWidth = currWidth;
				}
			}
		}
		return maxWidth + 3*Utils.convertPixelsToDp(20);
	}*/
	
	private final int layoutWidthHack() {
		int width = 0;
		final int size = trips.length;
		int curr, decIdx;
		String price;
		for (int i = 0; i < size; i++) {
			price = trips[i].getPrice();
			if (trips[i].getCurrency() != null)
				price = trips[i].getCurrency().format(price); 				
			decIdx = price.indexOf(".");
			if (decIdx == -1)
				curr = price.length() + 3;
			else 
				curr = decIdx + 3;
			if (curr > width)
				width = curr;
		}
		return width*((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mFragment.getSherlockActivity().getResources().getDisplayMetrics()));
	}
	
	private final class NewTripClickListener implements OnClickListener {
		private final TripFragment mFragment;
		public NewTripClickListener(final TripFragment tripHolder) {mFragment = tripHolder;}
		@Override public final void onClick(final View v) {mFragment.tripMenu(null);}
	}
	
	private final class ViewTripClickListener implements OnClickListener {
		private final TripFragment mFragment;
		private final TripRow mTrip;
		public ViewTripClickListener(final TripFragment tripHolder, final TripRow trip) {mFragment = tripHolder; this.mTrip = trip;}
		@Override public final void onClick(final View v) {mFragment.viewReceipts(mTrip);}
	}
	
	private final class EditTripClickListener implements OnLongClickListener {
		private final TripFragment mFragment;
		private final TripRow mTrip;
		public EditTripClickListener(final TripFragment tripHolder, final TripRow trip) {mFragment = tripHolder; this.mTrip = trip;}
		@Override public final boolean onLongClick(final View v) {return mFragment.editTrip(mTrip);}
	}
	
}
