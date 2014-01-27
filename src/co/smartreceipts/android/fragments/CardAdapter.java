package co.smartreceipts.android.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import co.smartreceipts.android.R;
import co.smartreceipts.android.persistence.Preferences;

public class CardAdapter<T> extends BaseAdapter {
	
	private static final int MAX_PRICE_WIDTH_DIVIDER = 2;
	private static final int MIN_PRICE_WIDTH_DIVIDER = 6;
	
	private final LayoutInflater mInflater;
	private final Preferences mPreferences;
	private final Context mContext;
	private T[] mData;
	private int mMinPriceWidth, mMaxPriceWidth, mCurrentPriceWidth;
	private final float mCardPriceTextSize;
	
	public CardAdapter(Context context, Preferences preferences) {
		mInflater = LayoutInflater.from(context);
		mPreferences = preferences;
		mContext = context;
		final Resources resources = mContext.getResources();
		final DisplayMetrics metrics = resources.getDisplayMetrics();
		mMaxPriceWidth = (int) (metrics.widthPixels / MAX_PRICE_WIDTH_DIVIDER); // Set to half width 
		mMinPriceWidth = (int) (metrics.widthPixels / MIN_PRICE_WIDTH_DIVIDER); // Set to 1/6 width
		mCurrentPriceWidth = mMinPriceWidth;
		mCardPriceTextSize = resources.getDimension(getCardPriceTextSizeResouce());
	}

	@Override
	public int getCount() {
		if (mData == null) {
			return 0;
		}
		else {
			return mData.length;
		}
	}

	@Override
	public T getItem(int i) {
		if (mData == null) {
			return null;
		}
		else {
			return mData[i];
		}
	}
	
	public long getItemId(int i) {
		return i;
	}
	
	private static class MyViewHolder {
		public TextView price;
		public TextView name;
		public TextView date;
	}

	@Override
	public View getView(final int i, View convertView, ViewGroup parent) {
		MyViewHolder holder;
		final T data = getItem(i);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.simple_card, parent, false);
			holder = new MyViewHolder();
			holder.price = (TextView) convertView.findViewById(android.R.id.text1);
			holder.name = (TextView) convertView.findViewById(android.R.id.title);
			holder.date = (TextView) convertView.findViewById(android.R.id.summary);
			convertView.setTag(holder);
		}
		else {
			holder = (MyViewHolder) convertView.getTag();
		}
		if (holder.price.getLayoutParams().width != mCurrentPriceWidth) {
			holder.price.getLayoutParams().width = mCurrentPriceWidth;
			holder.price.requestLayout();
		}
		holder.price.setText(getPrice(data));
		holder.name.setText(getName(data));
		holder.date.setText(getDate(data, mContext, mPreferences.getDateSeparator()));
		return convertView;
	}
	
	protected int getPriceLayoutWidth() {
		if (mData == null) {
			return mMaxPriceWidth;
		}
		else {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setTextSize(mCardPriceTextSize);
			paint.setTypeface(Typeface.DEFAULT_BOLD); // Set in the Price field
			int curr = mMinPriceWidth, measured;
			for (int i=0; i < mData.length; i++) {
				measured = (int) paint.measureText(getPrice(mData[i]));
				if (measured > curr) {
					curr = measured;
				}
			}
			if (curr < mMaxPriceWidth) {
				mCurrentPriceWidth = curr;
			}
			else {
				mCurrentPriceWidth = mMaxPriceWidth;
			}
			return mMaxPriceWidth;
		}
	}
	
	protected String getPrice(T data) {
		return "";
	}
	
	protected String getName(T data) {
		return "";
	}
	
	protected String getDate(T data, Context context, String dateSeparator) {
		return "";
	}
	
	protected int getCardPriceTextSizeResouce() {
		return R.dimen.card_price_size;
	}
	
	public final synchronized void notifyDataSetChanged(T[] newData) {
		mData = newData;
		getPriceLayoutWidth();
		super.notifyDataSetChanged();
	}

}