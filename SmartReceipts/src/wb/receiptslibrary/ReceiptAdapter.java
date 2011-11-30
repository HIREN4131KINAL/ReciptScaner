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

public class ReceiptAdapter extends BaseAdapter {
	
	//Number of Headers (i.e. New Trip)
	private static final int MAIN_HEADERS = 3;

	static enum ViewType {ADD_RECEIPT, EMAIL};
	
	private ReceiptRow[] _receipts;
	private final SmartReceiptsActivity _context;
	private int _largestWidth;
	
	public ReceiptAdapter(final SmartReceiptsActivity context, final ReceiptRow[] receipts) {
		_receipts = receipts;
		_context = context;
		_largestWidth = layoutWidthHack();
	}
	
	public int getCount() {
		return _receipts.length + MAIN_HEADERS;
	}
	
	public ReceiptRow getItem(final int i) {
		if (i==0)
			return null;
		else
			return _receipts[i-MAIN_HEADERS];
	}
	
	public long getItemId(int i) {
		return i;
	}
	
	public View getView(final int i, final View convertView, final ViewGroup parent) {
		if (i == 0) {
			ListItemView v = new ListItemView(_context, "Picture Receipt", R.drawable.ic_menu_add);
			v.setBackgroundResource(R.drawable.list_selector_background); 
			v.setOnClickListener(new NewPictureReceiptClickListener(_context));
			return v;
		}
		else if (i == 1) {
			ListItemView v = new ListItemView(_context, "Text-Only Receipt", R.drawable.ic_menu_add);
			v.setBackgroundResource(R.drawable.list_selector_background); 
			v.setOnClickListener(new NewTextReceiptClickListener(_context));
			return v;
		}
		else if (i == 2) {
			ListItemView v = new ListItemView(_context, "Email Report", R.drawable.ic_menu_share);
			v.setBackgroundResource(R.drawable.list_selector_background); 
			v.setOnClickListener(new ShareTripClickListener(_context));
			return v;
		}
		else {
			ReceiptRow receipt = _receipts[i-MAIN_HEADERS];
			final String currency = SmartReceiptsActivity.CurrencyValue(receipt.price);
			final String date = DateFormat.getDateFormat(_context).format(receipt.date);
			ListItemView v = new ListItemView(_context, currency, _largestWidth, receipt.name, date);
			//ListItemView v = new ListItemView(_context, receipt.name + " - " + currency, date);
			v.setBackgroundResource(R.drawable.list_selector_background); 
			v.setOnLongClickListener(new EditReceiptClickListener(_context, receipt));
			return v;
		}
	}
	
	private final int layoutWidthHack() {
		int width = 0;
		final int size = _receipts.length;
		int curr, decIdx;
		String price;
		for (int i = 0; i < size; i++) {
			price = _receipts[i].price;
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
	
	public final void notifyDataSetChanged(final ReceiptRow[] receipts) {
		_receipts = receipts;
		_largestWidth = layoutWidthHack();
		super.notifyDataSetChanged();
	}
	
	private final class NewPictureReceiptClickListener implements OnClickListener {
		private final SmartReceiptsActivity _activity;
		public NewPictureReceiptClickListener(final SmartReceiptsActivity activity) {_activity = activity;}
		@Override public final void onClick(final View v) {_activity.addPictureReceipt();}
	}
	
	private final class NewTextReceiptClickListener implements OnClickListener {
		private final SmartReceiptsActivity _activity;
		public NewTextReceiptClickListener(final SmartReceiptsActivity activity) {_activity = activity;}
		@Override public final void onClick(final View v) {_activity.addTextReceipt();}
	}
	
	private final class ShareTripClickListener implements OnClickListener {
		private final SmartReceiptsActivity _activity;
		public ShareTripClickListener(final SmartReceiptsActivity activity) {_activity = activity;}
		@Override public final void onClick(final View v) {_activity.emailTrip();}
	}
	
	private final class EditReceiptClickListener implements OnLongClickListener {
		private final SmartReceiptsActivity _activity;
		private final ReceiptRow _receipt;
		public EditReceiptClickListener(final SmartReceiptsActivity activity, final ReceiptRow receipt) {_activity = activity; _receipt = receipt;}
		@Override public final boolean onLongClick(final View v) {return _activity.editReceipt(_receipt);}
	}
	
}
