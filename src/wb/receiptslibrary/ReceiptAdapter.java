package wb.receiptslibrary;

import wb.android.ui.ListItemView;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReceiptAdapter extends BaseAdapter {
	
	@SuppressWarnings("unused") private static final boolean D = SmartReceiptsActivity.D;
	@SuppressWarnings("unused") private static final String TAG = "ReceiptAdapter";
	
	//Number of Headers (i.e. New Trip)
	private static final int MAIN_HEADERS = 4;
	private static final int COUNT = 2;

	static enum ViewType {ADD_RECEIPT, EMAIL};
	
	private ReceiptRow[] receipts;
	private final ReceiptViewHolder receiptViewHolder;
	private int largestWidth;
	private final LayoutInflater inflater;
	
	public ReceiptAdapter(final ReceiptViewHolder receiptViewHolder, final ReceiptRow[] receipts) {
		this.receipts = receipts;
		this.receiptViewHolder = receiptViewHolder;
		largestWidth = layoutWidthHack();
		inflater = LayoutInflater.from(receiptViewHolder.getActivity());
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
	
	/*
	@Override
	public int getItemViewType(int position) {
		if (position < (MAIN_HEADERS))
			return 0;
		else
			return 1;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	private static class MyViewHolder {
		boolean isActionItem;
		TextView title, price, dates;
		ImageView icon;
	}*/
	
	// Considered optimizing with convertView at some point: http://android-decoded.blogspot.com/2011/12/so-what-exactly-is-convertview.html
	public View getView(final int i, View convertView, ViewGroup parent) {
		if (i == 0) {
			convertView = inflater.inflate(R.layout.list_item_receipt_picture, null);
			convertView.setOnClickListener(new NewPictureReceiptClickListener(receiptViewHolder));
		}
		else if (i == 1) {
			convertView = inflater.inflate(R.layout.list_item_receipt_text, null);
			convertView.setOnClickListener(new NewTextReceiptClickListener(receiptViewHolder));
		}
		else if (i == 2) {
			convertView = inflater.inflate(R.layout.list_item_receipt_send, null);
			convertView.setOnClickListener(new ShareTripClickListener(receiptViewHolder));
		}
		else if (i == 3) {
			convertView = inflater.inflate(R.layout.list_item_mileage, null);
			TextView textConvertView = (TextView) convertView;
			textConvertView.setText(receiptViewHolder.getActivity().getFlex().getString(R.string.receipt_adapter_distance) + receiptViewHolder.getMilesString());
			convertView.setOnClickListener(new MileageClickListener(receiptViewHolder));
		}
		else {
			//v.setBackgroundResource(R.drawable.list_selector_background);
			ReceiptRow receipt = receipts[i-MAIN_HEADERS];
			final String currency = SRUtils.CurrencyValue(receipt.price, receipt.currency);
			final String date = DateFormat.getDateFormat(receiptViewHolder.getActivity()).format(receipt.date); 
			convertView = new ListItemView(receiptViewHolder.getActivity(), currency, largestWidth, receipt.name, date);
			convertView.setOnClickListener(new EditReceiptClickListener(receiptViewHolder, receipt));
		}
		return convertView;
	}
	
	private final int layoutWidthHack() {
		int width = 0;
		final int size = receipts.length;
		int curr, decIdx;
		String price;
		for (int i = 0; i < size; i++) {
			price = receipts[i].price;
			if (receipts[i].currency != null)
				price = receipts[i].currency.format(price); 	
			decIdx = price.indexOf(".");
			if (decIdx == -1)
				curr = price.length() + 3;
			else 
				curr = decIdx + 3;
			if (curr > width)
				width = curr;
		}
		return width*((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, receiptViewHolder.getActivity().getResources().getDisplayMetrics()));
	}
	
	public final void notifyDataSetChanged(final ReceiptRow[] receipts) {
		this.receipts = receipts;
		this.largestWidth = layoutWidthHack();
		super.notifyDataSetChanged();
	}
	
	private final class NewPictureReceiptClickListener implements OnClickListener {
		private final ReceiptViewHolder mReceiptViewHolder;
		public NewPictureReceiptClickListener(final ReceiptViewHolder receiptViewHolder) {mReceiptViewHolder = receiptViewHolder;}
		@Override public final void onClick(final View v) {mReceiptViewHolder.addPictureReceipt();}
	}
	
	private final class NewTextReceiptClickListener implements OnClickListener {
		private final ReceiptViewHolder mReceiptViewHolder;
		public NewTextReceiptClickListener(final ReceiptViewHolder receiptViewHolder) {mReceiptViewHolder = receiptViewHolder;}
		@Override public final void onClick(final View v) {mReceiptViewHolder.addTextReceipt();}
	}
	
	private final class ShareTripClickListener implements OnClickListener {
		private final ReceiptViewHolder mReceiptViewHolder;
		public ShareTripClickListener(final ReceiptViewHolder receiptViewHolder) {mReceiptViewHolder = receiptViewHolder;}
		@Override public final void onClick(final View v) {mReceiptViewHolder.emailTrip();}
	}
	
	private final class MileageClickListener implements OnClickListener {
		private final ReceiptViewHolder mReceiptViewHolder;
		public MileageClickListener(final ReceiptViewHolder receiptViewHolder) {mReceiptViewHolder = receiptViewHolder;}
		@Override public final void onClick(final View v) {mReceiptViewHolder.showMileage();}
	}
	
	private final class EditReceiptClickListener implements OnClickListener {
		private final ReceiptViewHolder mReceiptViewHolder;
		private final ReceiptRow mReceipt;
		public EditReceiptClickListener(final ReceiptViewHolder receiptViewHolder, final ReceiptRow receipt) {mReceiptViewHolder = receiptViewHolder; mReceipt = receipt;}
		@Override public final void onClick(final View v) {mReceiptViewHolder.editReceipt(mReceipt);}
	}
	
}
