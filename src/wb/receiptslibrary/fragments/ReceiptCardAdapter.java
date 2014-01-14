package wb.receiptslibrary.fragments;

import java.security.spec.MGF1ParameterSpec;

import wb.android.cache.ImageCache;
import wb.android.workers.DiskImageFetcher;
import wb.receiptslibrary.R;
import wb.receiptslibrary.model.ReceiptRow;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReceiptCardAdapter extends BaseAdapter {
	
	@SuppressWarnings("unused") private static final String TAG = "ReceiptAdapter";
	
	private ReceiptRow[] mReceipts;
	private final ReceiptsFragment mFragment;
	private final LayoutInflater mInflater;
	
	public ReceiptCardAdapter(final ReceiptsFragment fragment, final ReceiptRow[] receipts) {
		mReceipts = receipts;
		mFragment = fragment;
		mInflater = LayoutInflater.from(mFragment.getSherlockActivity());
	}
	
	public int getCount() {
		return mReceipts.length;
	}
	
	public ReceiptRow getItem(int i) {
		return mReceipts[i];
	}
	
	public long getItemId(int i) {
		return i;
	}
	
	private static class MyViewHolder {
		public ImageView thumbnail;
		public TextView price;
		public TextView name;
		public TextView date;
	}
	
	public View getView(final int i, View convertView, ViewGroup parent) {
		MyViewHolder holder;
		final ReceiptRow receipt = getItem(i);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.receipt_card, parent, false);
			holder = new MyViewHolder();
			holder.thumbnail = (ImageView) convertView.findViewById(R.id.receipt_image);
			holder.price = (TextView) convertView.findViewById(android.R.id.title);
			holder.name = (TextView) convertView.findViewById(android.R.id.content);
			holder.date = (TextView) convertView.findViewById(android.R.id.summary);
			convertView.setTag(holder);
		}
		else {
			holder = (MyViewHolder) convertView.getTag();
		}
		holder.price.setText(receipt.getCurrencyFormattedPrice());
		holder.name.setText(receipt.getName());
		holder.date.setText(receipt.getFormattedDate(mFragment.getSherlockActivity(), mFragment.getPersistenceManager().getPreferences().getDateSeparator()));
		if (receipt.hasImage()) {
			mFragment.getImageFetcher().loadImage(receipt.getImagePath(), holder.thumbnail);
		}
		return convertView;
	}
	
	public final void notifyDataSetChanged(ReceiptRow[] receipts) {
		mReceipts = receipts;
		super.notifyDataSetChanged();
	}
	
}
