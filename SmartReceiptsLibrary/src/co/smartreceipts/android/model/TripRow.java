package co.smartreceipts.android.model;

import java.io.File;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.filters.FilterFactory;

public final class TripRow implements Parcelable {
	
	public static final String PARCEL_KEY = "co.smartreceipts.android.TripRow";
	
	private static final String EMPTY_PRICE = "0.00";
	
	private final File mReportDirectory;
	private String mComment;
	private float mPrice, mDailySubTotal;
	private Date mStartDate, mEndDate;
	private TimeZone mStartTimeZone, mEndTimeZone;
	private WBCurrency mCurrency, mDefaultCurrency;
	private float mMiles;
	private SourceEnum mSource;
	private DecimalFormat mDecimalFormat;
	private Filter<ReceiptRow> mFilter;
	
	private TripRow(File directory, String price, Date startDate, Date endDate, WBCurrency currency, float miles, SourceEnum source) {
		mReportDirectory = directory;
		mPrice = tryParse(price);
		mStartDate = startDate;
		mEndDate = endDate;
		mCurrency = currency;
		mMiles = miles;
		mSource = source;
	}
	
	private TripRow(Parcel in) {
		mReportDirectory = new File(in.readString());
		mPrice = in.readFloat();
		mStartDate = new Date(in.readLong());
		mEndDate = new Date(in.readLong());
		mCurrency = WBCurrency.getInstance(in.readString());
		mMiles = in.readFloat();
		mStartTimeZone = TimeZone.getTimeZone(in.readString());
		mEndTimeZone = TimeZone.getTimeZone(in.readString());
		mDailySubTotal = in.readFloat();
		mComment = in.readString();
		mDefaultCurrency = WBCurrency.getInstance(in.readString());
		mSource = SourceEnum.Parcel;
	}
	
	private float tryParse(String number) {
		if (TextUtils.isEmpty(number)) {
			return 0f;
		}
		try {
			return Float.parseFloat(number);	
		} catch (NumberFormatException e) {
			return 0f;
		}
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
		final TimeZone timeZone = getStartTimeZone();
		java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
		format.setTimeZone(timeZone);
		return format.format(mStartDate);
	}
	
	public String getFormattedStartDate(Context context, String separator) {
		final TimeZone timeZone = getStartTimeZone();
		java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
		format.setTimeZone(timeZone);
		String formattedDate = format.format(mStartDate);
		formattedDate = formattedDate.replace(DateUtils.getDateSeparator(context), separator);
		return formattedDate;
	}
	
	public TimeZone getStartTimeZone() {
		return (mStartTimeZone != null) ? mStartTimeZone : TimeZone.getDefault();
	}
	
	public Date getEndDate() {
		return mEndDate;
	}
	
	public String getFormattedEndDate(Context context) {
		final TimeZone timeZone = getEndTimeZone();
		java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
		format.setTimeZone(timeZone);
		return format.format(mEndDate);
	}
	
	public String getFormattedEndDate(Context context, String separator) {
		final TimeZone timeZone = getEndTimeZone();
		java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
		format.setTimeZone(timeZone);
		String formattedDate = format.format(mEndDate);
		formattedDate = formattedDate.replace(DateUtils.getDateSeparator(context), separator);
		return formattedDate;
	}
	
	public TimeZone getEndTimeZone() {
		return (mEndTimeZone != null) ? mEndTimeZone : TimeZone.getDefault();
	}
	
	/**
	 * Tests if a particular date is included with the bounds of this particular trip
	 * When performing the test, it uses the local time zone for the date, and the defined
	 * time zones for the start and end date bounds. The start date time is assumed to occur 
	 * at 00:01 of the start day and the end date is assumed to occur at 23:59 of the end day.
	 * The reasoning behind this is to ensure that it appears properly from a UI perspective.
	 * Since the initial date only shows the day itself, it may include an arbitrary time that
	 * is never shown to the user. Setting the time aspect manually accounts for this. This returns
	 * false if the date is null.
	 * @param date - the date to test
	 * @return true if it is contained within. false otherwise
	 */
	public boolean isDateInsideTripBounds(Date date) {
		if (date == null)
			return false;
		
		//Build a calendar for the date we intend to test
		Calendar testCalendar = Calendar.getInstance(); 
		testCalendar.setTime(date);
		testCalendar.setTimeZone(TimeZone.getDefault());
		
		//Build a calendar for the start date
		Calendar startCalendar = Calendar.getInstance(); 
		startCalendar.setTime(mStartDate);
		startCalendar.setTimeZone((mStartTimeZone != null) ? mStartTimeZone : TimeZone.getDefault());
		startCalendar.set(Calendar.HOUR_OF_DAY, 0); 
		startCalendar.set(Calendar.MINUTE, 0); 
		startCalendar.set(Calendar.SECOND, 0); 
		startCalendar.set(Calendar.MILLISECOND, 0);
		
		//Build a calendar for the end date
		Calendar endCalendar = Calendar.getInstance(); 
		endCalendar.setTime(mEndDate);
		endCalendar.setTimeZone((mEndTimeZone != null) ? mEndTimeZone : TimeZone.getDefault());
		endCalendar.set(Calendar.HOUR_OF_DAY, 23); 
		endCalendar.set(Calendar.MINUTE, 59); 
		endCalendar.set(Calendar.SECOND, 59); 
		endCalendar.set(Calendar.MILLISECOND, 999);
		
		if (testCalendar.compareTo(startCalendar) >= 0 && testCalendar.compareTo(endCalendar) <= 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String getPrice() {
		return Float.toString(mPrice);
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
		if (mCurrency != null) {
			return mCurrency.format(getPriceAsFloat());
		}
		else {
			return "Mixed";
		}
	}
	
	public boolean isPriceEmpty() {
		return TextUtils.isEmpty(getPrice());
	}
	
	public String getDailySubTotal() {
		return Float.toString(mDailySubTotal);
	}
	
	public float getDailySubTotalAsFloat() {
		try {
			return Float.valueOf(mDailySubTotal);
		}
		catch(NumberFormatException e) {
			return 0f;
		}
	}
	
	public String getDecimalFormattedDailySubTotal() { 
		return getDecimalFormat().format(getDailySubTotalAsFloat());
	}
	
	public String getCurrencyFormattedDailySubTotal() {
		if (mCurrency != null) {
			return mCurrency.format(mDailySubTotal);
		}
		else {
			return EMPTY_PRICE;
		}
	}
	
	public boolean isDailySubTotalEmpty() {
		return TextUtils.isEmpty(getDailySubTotal());
	}
	
	public WBCurrency getCurrency() {
		return mCurrency;
	}
	
	public String getCurrencyCode() {
		if (mCurrency != null) {
			return mCurrency.getCurrencyCode();
		}
		else {
			return WBCurrency.MISSING_CURRENCY;
		}
	}
	
	public WBCurrency getDefaultCurrency() {
		return mDefaultCurrency;
	}
	
	public String getDefaultCurrencyCode() {
		if (mDefaultCurrency != null) {
			return mDefaultCurrency.getCurrencyCode();
		}
		else {
			return Currency.getInstance(Locale.getDefault()).getCurrencyCode();
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
	
	public String getComment() {
		return mComment;
	}
	
	public String getSource() {
		return mSource.name();
	}
	
	public void setPrice(String price) {
		mPrice = tryParse(price);
	}
	
	public void setDailySubTotal(String dailyTotal) {
		mDailySubTotal = tryParse(dailyTotal);
	}
	
	public void setCurrency(WBCurrency currency) {
		mCurrency = currency;
	}
	
	public void setCurrency(String currencyCode) {
		mCurrency = WBCurrency.getInstance(currencyCode);
	}
	
	public void setDefaultCurrency(WBCurrency currency) {
		mDefaultCurrency = currency;
	}
	
	public void setDefaultCurrency(String currencyCode) {
		mDefaultCurrency = WBCurrency.getInstance(currencyCode);
	}
	
	public Filter<ReceiptRow> getFilter() {
		return mFilter;
	}
	
	public boolean hasFilter() {
		return mFilter != null;
	}
	
	public void setFilter(Filter<ReceiptRow> filter) {
		mFilter = filter;
	}
	
	public void setMileage(float mileage) {
		mMiles = mileage;
	}
	
	public void setComment(String comment) {
		mComment = comment;
	}
	
	void setStartTimeZone(TimeZone startTimeZone) {
		mStartTimeZone = startTimeZone;
	}
	
	void setEndTimeZone(TimeZone endTimeZone) {
		mEndTimeZone = endTimeZone;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getDirectoryPath());
		dest.writeFloat(getPriceAsFloat());
		dest.writeLong(getStartDate().getTime());
		dest.writeLong(getEndDate().getTime());
		dest.writeString(getCurrencyCode());
		dest.writeFloat(getMileage());
		dest.writeString(getStartTimeZone().getID());
		dest.writeString(getEndTimeZone().getID());
		dest.writeFloat(getDailySubTotalAsFloat());
		dest.writeString(getComment());
		dest.writeString(getDefaultCurrencyCode());
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
	@SuppressWarnings("deprecation")
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
		
		private File _dir;
		private String _price, _dailySubTotal, _comment;
		private Date _startDate, _endDate;
		private TimeZone _startTimeZone, _endTimeZone;
		private WBCurrency _currency, _defaultCurrency;
		private float _miles;
		private Filter<ReceiptRow> _filter;
		private SourceEnum _source;
		
		public Builder() {
			_price = EMPTY_PRICE;
			_dailySubTotal = EMPTY_PRICE;
			_miles = 0;
			_source = SourceEnum.Undefined;
			_startTimeZone = TimeZone.getDefault();
			_endTimeZone = TimeZone.getDefault();
			//Be sure to update reset here too
		}
		
		public Builder setDirectory(File directory) {
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
		
		public Builder setDailySubTotal(String dailySubTotal) {
			if (TextUtils.isEmpty(dailySubTotal)) {
				_dailySubTotal = EMPTY_PRICE;
			}
			else {
				_dailySubTotal = dailySubTotal;
			}
			return this;
		}
		
		public Builder setStartDate(Date startDate) {
			if (startDate == null) {
				throw new IllegalArgumentException("The start date cannot be null");
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
				throw new IllegalArgumentException("The end date cannot be null");
			}
			_endDate = endDate;
			return this;
		}
		
		public Builder setEndDate(long endDate) {
			_endDate = new Date(endDate);
			return this;
		}
		
		public Builder setStartTimeZone(TimeZone startTimeZone) {
			_startTimeZone = startTimeZone;
			return this;
		}
		
		public Builder setStartTimeZone(String timeZoneId) {
			if (timeZoneId != null) {
				_startTimeZone = TimeZone.getTimeZone(timeZoneId);
			}
			return this;
		}
		
		public Builder setEndTimeZone(TimeZone endTimeZone) {
			_endTimeZone = endTimeZone;
			return this;
		}
		
		public Builder setEndTimeZone(String timeZoneId) {
			if (timeZoneId != null) {
				_endTimeZone = TimeZone.getTimeZone(timeZoneId);
			}
			return this;
		}
		
		public Builder setCurrency(WBCurrency currency) {
			_currency = currency;
			return this;
		}
		
		public Builder setCurrency(String currencyCode) {
			if (TextUtils.isEmpty(currencyCode)) {
				throw new IllegalArgumentException("The currency code cannot be null or empty");
			}
			_currency = WBCurrency.getInstance(currencyCode);
			return this;
		}
		
		public Builder setDefaultCurrency(WBCurrency currency) {
			_defaultCurrency = currency;
			return this;
		}
		
		public Builder setDefaultCurrency(String currencyCode) {
			_defaultCurrency = WBCurrency.getInstance(currencyCode);
			return this;
		}
		
		public Builder setDefaultCurrency(String currencyCode, String missingCodeDefault) {
			if (TextUtils.isEmpty(currencyCode)) {
				_defaultCurrency = WBCurrency.getInstance(missingCodeDefault);
			}
			else {
				_defaultCurrency = WBCurrency.getInstance(currencyCode);
			}
			return this;
		}
		
		public Builder setMileage(float miles) {
			_miles = miles;
			return this;
		}
		
		public Builder setComment(String comment) {
			_comment = comment;
			return this;
		}
		
		public Builder setFilter(Filter<ReceiptRow> filter) {
			_filter = filter;
			return this;
		}
		
		public Builder setFilter(JSONObject json) {
			try {
				_filter = FilterFactory.getReceiptFilter(json);
			} 
			catch (JSONException e) { }
			return this;
		}
		
		public Builder setFilter(String json) {
			try {
				_filter = FilterFactory.getReceiptFilter(new JSONObject(json));
			} 
			catch (JSONException e) { }
			return this;
		}
		
		public Builder setSourceAsCache() {
			_source = SourceEnum.Cache;
			return this;
		}
		
		public TripRow build() {
			TripRow tripRow =  new TripRow(_dir, _price, _startDate, _endDate, _currency, _miles, _source);
			tripRow.setStartTimeZone(_startTimeZone);
			tripRow.setEndTimeZone(_endTimeZone);
			tripRow.setDefaultCurrency(_defaultCurrency);
			tripRow.setComment(_comment);
			tripRow.setDailySubTotal(_dailySubTotal);
			tripRow.setFilter(_filter);
			return tripRow;
		}
		
		public void reset() {
			_dir = null;
			_startDate = null;
			_endDate = null;
			_startTimeZone = TimeZone.getDefault();
			_endTimeZone = TimeZone.getDefault();
			_currency = null;
			_price = EMPTY_PRICE;
			_dailySubTotal = EMPTY_PRICE;
			_miles = 0;
			_comment = null;
			_defaultCurrency = null;
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
