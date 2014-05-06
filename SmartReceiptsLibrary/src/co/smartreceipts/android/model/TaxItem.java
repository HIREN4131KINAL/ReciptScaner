package co.smartreceipts.android.model;

import java.math.BigDecimal;

import android.util.Log;
import co.smartreceipts.android.BuildConfig;

public class TaxItem {
	
	private static final String TAG = "TaxItem";
	
	private BigDecimal mPercent;
	private BigDecimal mPrice, mTax;
	
	public TaxItem(String percent) {
		try {
			mPercent = new BigDecimal(percent);
		}
		catch (NumberFormatException e) {
			mPercent = null;
		}
	}
	
	public TaxItem(float percent) {
		mPercent = new BigDecimal(percent);
	}
	
	public TaxItem(BigDecimal percent) {
		mPercent = percent;
	}
	
	public BigDecimal getPercent() {
		return mPercent;
	}
	
	public String getPercentAsString() {
		if (mPercent == null) {
			return new String();
		}
		else {
			return mPercent.toPlainString() + "%";
		}
	}
	
	public void setPrice(String price) {
		try {
			mPrice = new BigDecimal(price.trim());
			getTax();
		}
		catch (NumberFormatException e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
			mPrice = null;
		}
	}
	
	public boolean isValid() {
		return mTax != null;
	}
	
	public BigDecimal getTax() {
		if (mPercent == null || mPrice == null) {
			mTax = null;
		}
		else {
			mTax = mPercent.multiply(mPrice).divide(new BigDecimal(100));
		}
		return mTax;
	}
	
	@Override
	public String toString() {
		if (mTax == null) {
			return new String();
		}
		else {
			return mTax.toPlainString(); //Need to use decimal format
		}
	}

}
