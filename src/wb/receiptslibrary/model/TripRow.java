package wb.receiptslibrary.model;

import java.io.File;
import java.sql.Date;
import java.text.DecimalFormat;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;

public final class TripRow implements Parcelable {
	
	private static final String EMPTY_PRICE = "0.00";
	
	private final File mReportDirectory;
	private String mPrice;
	private Date mStartDate, mEndDate;
	private WBCurrency mCurrency;
	private float mMiles;
	private SourceEnum mSource;
	private DecimalFormat mDecimalFormat;
	
	private TripRow(File directory, String price, Date startDate, Date endDate, WBCurrency currency, float miles, SourceEnum source) {
		mReportDirectory = directory;
		mPrice = price;
		mStartDate = startDate;
		mEndDate = endDate;
		mCurrency = currency;
		mMiles = 0;
		mSource = source;
	}
	
	private TripRow(Parcel in) {
		mReportDirectory = new File(in.readString());
		mPrice = in.readString();
		mStartDate = new Date(in.readLong());
		mEndDate = new Date(in.readLong());
		mCurrency = WBCurrency.getInstance(in.readString());
		mMiles = in.readFloat();
		mSource = SourceEnum.Parcel;
	}
	
	// TODO: Add null safety checks?
	public String getName() {
		return mReportDirectory.getName();
	}
	
	public File getDirectory() {
		return mReportDirectory;
	}
	
	// TODO: Add null safety checks?
	public String getDirectoryPath() {
		return mReportDirectory.getAbsolutePath();
	}
	
	public Date getStartDate() {
		return mStartDate;
	}
	
	public String getFormattedStartDate(Context context) {
		return DateFormat.getDateFormat(context).format(mStartDate);
	}
	
	public Date getEndDate() {
		return mEndDate;
	}
	
	public String getFormattedEndDate(Context context) {
		return DateFormat.getDateFormat(context).format(mEndDate);
	}
	
	public String getPrice() {
		return mPrice;
	}
	
	public float getPriceAsFloat() {
		try {
			return Float.valueOf(mPrice);
		}
		catch(NumberFormatException e) {
			return 0f;
		}
	}
	
	public String getDecimalFormattedPrice() { 
		return getDecimalFormat().format(getPriceAsFloat());
	}
	
	public String getCurrencyFormattedPrice() {
		if (getCurrency() != null) {
			return getCurrency().format(mPrice);
		}
		else {
			return "Mixed";
		}
	}
	
	public boolean isPriceEmpty() {
		return TextUtils.isEmpty(mPrice);
	}
	
	public WBCurrency getCurrency() {
		return mCurrency;
	}
	
	public String getCurrencyCode() {
		if (mCurrency != null) {
			return getCurrency().getCurrencyCode();
		}
		else {
			return WBCurrency.MISSING_CURRENCY;
		}
	}
	
	public float getMileage() {
		return mMiles;
	}
	
	public String getMilesAsString() {
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(2);
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setGroupingUsed(false);
		return decimalFormat.format(this.mMiles);
	}
	
	public String getSource() {
		return mSource.name();
	}
	
	public void setPrice(String price) {
		mPrice = price;
	}
	
	public void setCurrency(WBCurrency currency) {
		mCurrency = currency;
	}
	
	public void setMileage(float mileage) {
		mMiles = mileage;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getDirectoryPath());
		dest.writeString(getPrice());
		dest.writeLong(getStartDate().getTime());
		dest.writeLong(getEndDate().getTime());
		dest.writeString(getCurrencyCode());
		dest.writeFloat(getMileage());
	}
	
	private DecimalFormat getDecimalFormat() {
		if (mDecimalFormat == null) {
			mDecimalFormat = new DecimalFormat();
			mDecimalFormat.setMaximumFractionDigits(2);
			mDecimalFormat.setMinimumFractionDigits(2);
			mDecimalFormat.setGroupingUsed(false);
		}
		return mDecimalFormat;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "::\n" + "["
											   + "source => " + getSource() + "; \n"
											   + "name => " + getName() + "; \n"
											   + "directory =>" + getDirectory().getAbsolutePath() + "; \n" 
											   + "startDate =>" + getStartDate().toGMTString() + "; \n"
											   + "endDate => " + getEndDate().toGMTString() + "; \n"
											   + "currency =>" + getCurrency().getCurrencyCode() + "; \n"
											   + "miles => " + getMilesAsString() + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mReportDirectory == null) ? 0 : mReportDirectory.getAbsolutePath().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TripRow other = (TripRow) obj;
		if (mReportDirectory == null) {
			if (other.mReportDirectory != null)
				return false;
		} else if (!mReportDirectory.equals(other.mReportDirectory))
			return false;
		return true;
	}

	public static class Builder {
		
		public File _dir;
		public String _price;
		public Date _startDate, _endDate;
		public WBCurrency _currency;
		public float _miles;
		public SourceEnum _source;
		
		public Builder() {
			_price = EMPTY_PRICE;
			_miles = 0;
			_source = SourceEnum.Undefined;
		}
		
		public Builder setDirectory(File directory) {
			if (!directory.isDirectory()) {
				throw new IllegalArgumentException("The report file must be a directory");
			}
			_dir = directory;
			return this;
		}
		
		public Builder setPrice(String price) {
			if (TextUtils.isEmpty(price)) {
				_price = EMPTY_PRICE;
			}
			else {
				_price = price;
			}
			return this;
		}
		
		public Builder setStartDate(Date startDate) {
			if (startDate == null) {
				throw new IllegalArgumentException("The start mDate cannot be null");
			}
			_startDate = startDate;
			return this;
		}
		
		public Builder setStartDate(long startDate) {
			_startDate = new Date(startDate);
			return this;
		}
		
		public Builder setEndDate(Date endDate) {
			if (endDate == null) {
				throw new IllegalArgumentException("The end mDate cannot be null");
			}
			_endDate = endDate;
			return this;
		}
		
		public Builder setEndDate(long endDate) {
			_endDate = new Date(endDate);
			return this;
		}
		
		public Builder setCurrency(WBCurrency currency) {
			_currency = currency;
			return this;
		}
		
		public Builder setCurrency(String currencyCode) {
			if (TextUtils.isEmpty(currencyCode)) {
				throw new IllegalArgumentException("The mCurrency code cannot be null or empty");
			}
			_currency = WBCurrency.getInstance(currencyCode);
			return this;
		}
		
		public Builder setMileage(float miles) {
			_miles = miles;
			return this;
		}
		
		public Builder setSourceAsCache() {
			_source = SourceEnum.Cache;
			return this;
		}
		
		public TripRow build() {
			return new TripRow(_dir, _price, _startDate, _endDate, _currency, _miles, _source);
		}
		
		public void reset() {
			_dir = null;
			_startDate = null;
			_endDate = null;
			_currency = null;
			_price = EMPTY_PRICE;
			_miles = 0;
			_source = SourceEnum.Undefined;
		}
		
	}
	
	public static final Parcelable.Creator<TripRow> CREATOR = new Parcelable.Creator<TripRow>() {

		@Override
		public TripRow createFromParcel(Parcel source) {
			return new TripRow(source);
		}

		@Override
		public TripRow[] newArray(int size) {
			return new TripRow[size];
		}
		
	};
	
}
