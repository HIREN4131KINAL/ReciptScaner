package wb.receiptslibrary.fragments;

import wb.android.ui.ListItemView;
import wb.receiptslibrary.R;
import wb.receiptslibrary.model.ReceiptRow;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ReceiptAdapter extends BaseAdapter {
	
	@SuppressWarnings("unused") private static final String TAG = "ReceiptAdapter";
	
	//Number of Headers (i.e. New Trip)
	private static final int MAIN_HEADERS = 4;

	static enum ViewType {ADD_RECEIPT, EMAIL};
	
	private ReceiptRow[] receipts;
	private final ReceiptsFragment mFragment;
	private float largestWidth;
	private final LayoutInflater inflater;
	
	public ReceiptAdapter(final ReceiptsFragment fragment, final ReceiptRow[] receipts) {
		this.receipts = receipts;
		mFragment = fragment;
		largestWidth = layoutWidthHack();
		inflater = LayoutInflater.from(mFragment.getSherlockActivity());
	}
	
	public int getCount() {
		return receipts.length + MAIN_HEADERS;
	}
	
	public ReceiptRow getItem(final int i) {
		if (i==0)
			return null;
		else
			return receipts[i-MAIN_HEADERS];
	}
	
	public long getItemId(int i) {
		return i;
	}
	
	// Considered optimizing with convertView at some point: http://android-decoded.blogspot.com/2011/12/so-what-exactly-is-convertview.html
	public View getView(final int i, View convertView, ViewGroup parent) {
		if (i == 0) {
			convertView = inflater.inflate(R.layout.list_item_receipt_picture, null);
			convertView.setOnClickListener(new NewPictureReceiptClickListener(mFragment));
		}
		else if (i == 1) {
			convertView = inflater.inflate(R.layout.list_item_receipt_text, null);
			convertView.setOnClickListener(new NewTextReceiptClickListener(mFragment));
		}
		else if (i == 2) {
			convertView = inflater.inflate(R.layout.list_item_receipt_send, null);
			convertView.setOnClickListener(new ShareTripClickListener(mFragment));
		}
		else if (i == 3) {
			convertView = inflater.inflate(R.layout.list_item_mileage, null);
			TextView textConvertView = (TextView) convertView;
			textConvertView.setText(mFragment.getFlex().getString(R.string.receipt_adapter_distance) + mFragment.getMilesString());
			convertView.setOnClickListener(new MileageClickListener(mFragment));
		}
		else {
			ReceiptRow receipt = receipts[i-MAIN_HEADERS]; 
			convertView = new ListItemView(mFragment.getActivity(), 
										   receipt.getCurrencyFormattedPrice(), 
										   (int) largestWidth, 
										   receipt.getName(), 
										   receipt.getFormattedDate(mFragment.getActivity(), mFragment.getPersistenceManager().getPreferences().getDateSeparator()),
										   R.drawable.default_selector);
			convertView.setOnClickListener(new EditReceiptClickListener(mFragment, receipt));
		}
		return convertView;
	}
	
	/*
	private final float layoutWidthHack() {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(24);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		String currString;
		float maxWidth = -1f, currWidth;
		for (int i = 0; i < receipts.length; i++) {
			currString = receipts[i].getCurrencyFormattedPrice();
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
		int curr, decIdx;
		String price;
		for (int i = 0; i < receipts.length; i++) {
			price = receipts[i].getCurrencyFormattedPrice();
			decIdx = price.indexOf(".");
			if (decIdx == -1)
				curr = price.length() + 3;
			else 
				curr = decIdx + 3;
			if (curr > width)
				width = curr;
		}
		return width*((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mFragment.getActivity().getResources().getDisplayMetrics()));
	}
	
	public final void notifyDataSetChanged(final ReceiptRow[] receipts) {
		this.receipts = receipts;
		this.largestWidth = layoutWidthHack();
		super.notifyDataSetChanged();
	}
	
	private final class NewPictureReceiptClickListener implements OnClickListener {
		private final ReceiptsFragment mReceiptsFragment;
		public NewPictureReceiptClickListener(final ReceiptsFragment mFragment) {mReceiptsFragment = mFragment;}
		@Override public final void onClick(final View v) {mReceiptsFragment.addPictureReceipt();}
	}
	
	private final class NewTextReceiptClickListener implements OnClickListener {
		private final ReceiptsFragment mReceiptsFragment;
		public NewTextReceiptClickListener(final ReceiptsFragment mFragment) {mReceiptsFragment = mFragment;}
		@Override public final void onClick(final View v) {mReceiptsFragment.addTextReceipt();}
	}
	
	private final class ShareTripClickListener implements OnClickListener {
		private final ReceiptsFragment mReceiptsFragment;
		public ShareTripClickListener(final ReceiptsFragment mFragment) {mReceiptsFragment = mFragment;}
		@Override public final void onClick(final View v) {mReceiptsFragment.emailTrip();}
	}
	
	private final class MileageClickListener implements OnClickListener {
		private final ReceiptsFragment mReceiptsFragment;
		public MileageClickListener(final ReceiptsFragment mFragment) {mReceiptsFragment = mFragment;}
		@Override public final void onClick(final View v) {mReceiptsFragment.showMileage();}
	}
	
	private final class EditReceiptClickListener implements OnClickListener {
		private final ReceiptsFragment mReceiptsFragment;
		private final ReceiptRow mReceipt;
		public EditReceiptClickListener(final ReceiptsFragment mFragment, final ReceiptRow receipt) {mReceiptsFragment = mFragment; mReceipt = receipt;}
		@Override public final void onClick(final View v) {mReceiptsFragment.editReceipt(mReceipt);}
	}
	
}
