package co.smartreceipts.android.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.util.Log;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.persistence.Preferences;

public class TaxItem {
	
	private static final String TAG = "TaxItem";
	
	private BigDecimal mPercent;
	private BigDecimal mPrice, mTax;
	private boolean mUsePreTaxPrice;
	
	public TaxItem(String percent, Preferences preferences) {
		try {
			mPercent = new BigDecimal(percent);
		}
		catch (NumberFormatException e) {
			mPercent = null;
		}
		mUsePreTaxPrice = (preferences == null) ? false : preferences.getUsesPreTaxPrice();
	}
	
	public TaxItem(float percent, Preferences preferences) {
		mPercent = new BigDecimal(percent);
		mUsePreTaxPrice = (preferences == null) ? false : preferences.getUsesPreTaxPrice();
	}
	
	public TaxItem(BigDecimal percent, Preferences preferences) {
		mPercent = percent;
		mUsePreTaxPrice = (preferences == null) ? false : preferences.getUsesPreTaxPrice();
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
			if (mUsePreTaxPrice) {
				mTax = mPrice.multiply(mPercent).divide(new BigDecimal(100));
			}
			else {
				mTax = mPrice.divide(mPercent.add(new BigDecimal(1))).divide(new BigDecimal(100));
			}
		}
		return mTax;
	}
	
	@Override
	public String toString() {
		if (mTax == null) {
			return new String();
		}
		else {
			DecimalFormat format = new DecimalFormat();
			format.setMaximumFractionDigits(2);
			format.setMinimumFractionDigits(2);
			format.setGroupingUsed(false);
			return format.format(mTax.doubleValue());
		}
	}

}
