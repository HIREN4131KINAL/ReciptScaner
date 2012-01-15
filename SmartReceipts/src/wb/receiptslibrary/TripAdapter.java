package wb.receiptslibrary;

import wb.android.ui.ListItemView;

import android.R;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TripAdapter extends BaseAdapter {
	
	//Number of Headers (i.e. New Trip)
	private static final int MAIN_HEADERS = 1;
	
	private TripRow[] _trips;
	private final SmartReceiptsActivity _context;
	private int _largestWidth;
	
	public TripAdapter(final SmartReceiptsActivity context, final TripRow[] trips) {
		_trips = trips;
		_context = context;
		_largestWidth = layoutWidthHack();
	}
	
	public int getCount() {
		return _trips.length + MAIN_HEADERS;
	}
	
	public TripRow getItem(final int i) {
		if (i==0)
			return null;
		else
			return _trips[i-MAIN_HEADERS];
	}
	
	public long getItemId(final int i) {
		return i;
	}
	
	public View getView(final int i, final View convertView, final ViewGroup parent) {
		if (i == 0) {
			ListItemView v = new ListItemView(_context, "Expense Report", R.drawable.ic_menu_add);
			v.setOnClickListener(new NewTripClickListener(_context));
			return v;
		}
		else {
			TripRow trip = _trips[i-MAIN_HEADERS];
			final String currency = SmartReceiptsActivity.CurrencyValue(trip.price, trip.currency);
			final String from = DateFormat.getDateFormat(_context).format(trip.from);
			final String to = DateFormat.getDateFormat(_context).format(trip.to);
			ListItemView v = new ListItemView(_context, currency, _largestWidth, trip.dir.getName(), from + " to " + to);
			//ListItemView v = new ListItemView(_context, trip.dir.getName() + " - " + currency, from + " to " + to);
			v.setOnClickListener(new ViewTripClickListener(_context, trip));
			v.setOnLongClickListener(new EditTripClickListener(_context, trip));
			return v;
		}
	}
	
	public final void notifyDataSetChanged(final TripRow[] trips) {
		_trips = trips;
		_largestWidth = layoutWidthHack();
		super.notifyDataSetChanged();
	}
	
	private final int layoutWidthHack() {
		int width = 0;
		final int size = _trips.length;
		int curr, decIdx;
		String price;
		for (int i = 0; i < size; i++) {
			price = _trips[i].price;
			decIdx = price.indexOf(".");
			if (decIdx == -1)
				curr = price.length() + 4;
			else 
				curr = decIdx + 4;
			if (curr > width)
				width = curr;
		}
		return width*((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, _context.getResources().getDisplayMetrics()));
	}
	
	private final class NewTripClickListener implements OnClickListener {
		private final SmartReceiptsActivity _activity;
		public NewTripClickListener(final SmartReceiptsActivity activity) {_activity = activity;}
		@Override public final void onClick(final View v) {_activity.tripMenu(null);}
	}
	
	private final class ViewTripClickListener implements OnClickListener {
		private final SmartReceiptsActivity _activity;
		private final TripRow _trip;
		public ViewTripClickListener(final SmartReceiptsActivity activity, final TripRow trip) {_activity = activity; _trip = trip;}
		@Override public final void onClick(final View v) {_activity.viewTrip(_trip);}
	}
	
	private final class EditTripClickListener implements OnLongClickListener {
		private final SmartReceiptsActivity _activity;
		private final TripRow _trip;
		public EditTripClickListener(final SmartReceiptsActivity activity, final TripRow trip) {_activity = activity; _trip = trip;}
		@Override public final boolean onLongClick(final View v) {return _activity.editTrip(_trip);}
	}
	
}
