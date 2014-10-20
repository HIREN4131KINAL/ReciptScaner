package co.smartreceipts.android.model.impl;

import android.content.Context;
import android.os.Parcel;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;

public final class DefaultTripImpl implements Trip {

    private static final String EMPTY_PRICE = "0.00";

    private final File mReportDirectory;
    private String mComment;
    private BigDecimal mPrice, mDailySubTotal;
    private Date mStartDate, mEndDate;
    private TimeZone mStartTimeZone, mEndTimeZone;
    private WBCurrency mCurrency, mDefaultCurrency;
    private Source mSource;
    private DecimalFormat mDecimalFormat;
    private Filter<ReceiptRow> mFilter;

    public DefaultTripImpl(File directory, Date startDate, TimeZone startTimeZone, Date endDate, TimeZone endTimeZone, WBCurrency currency, WBCurrency defaultCurrency, String comment, Filter<ReceiptRow> filter, Source source) {
        mReportDirectory = directory;
        mStartDate = startDate;
        mStartTimeZone = startTimeZone;
        mEndDate = endDate;
        mEndTimeZone = endTimeZone;
        mCurrency = currency;
        mDefaultCurrency = defaultCurrency;
        mComment = comment;
        mFilter = filter;
        mSource = source;
    }

    private DefaultTripImpl(Parcel in) {
        mReportDirectory = new File(in.readString());
        mPrice = new BigDecimal(in.readFloat());
        mStartDate = new Date(in.readLong());
        mEndDate = new Date(in.readLong());
        mCurrency = WBCurrency.getInstance(in.readString());
        mStartTimeZone = TimeZone.getTimeZone(in.readString());
        mEndTimeZone = TimeZone.getTimeZone(in.readString());
        mDailySubTotal = new BigDecimal(in.readFloat());
        mComment = in.readString();
        mDefaultCurrency = WBCurrency.getInstance(in.readString());
        mSource = Source.Parcel;
    }

    @Override
    public String getName() {
        return mReportDirectory.getName();
    }

    @Override
    public File getDirectory() {
        return mReportDirectory;
    }

    @Override
    public String getDirectoryPath() {
        return mReportDirectory.getAbsolutePath();
    }

    @Override
    public Date getStartDate() {
        return mStartDate;
    }

    @Override
    public String getFormattedStartDate(Context context, String separator) {
        final TimeZone timeZone = getStartTimeZone();
        java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
        format.setTimeZone(timeZone);
        String formattedDate = format.format(mStartDate);
        formattedDate = formattedDate.replace(DateUtils.getDateSeparator(context), separator);
        return formattedDate;
    }

    @Override
    public TimeZone getStartTimeZone() {
        return (mStartTimeZone != null) ? mStartTimeZone : TimeZone.getDefault();
    }

    @Override
    public Date getEndDate() {
        return mEndDate;
    }

    @Override
    public String getFormattedEndDate(Context context, String separator) {
        final TimeZone timeZone = getEndTimeZone();
        java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
        format.setTimeZone(timeZone);
        String formattedDate = format.format(mEndDate);
        formattedDate = formattedDate.replace(DateUtils.getDateSeparator(context), separator);
        return formattedDate;
    }

    @Override
    public TimeZone getEndTimeZone() {
        return (mEndTimeZone != null) ? mEndTimeZone : TimeZone.getDefault();
    }

    /**
     * Tests if a particular date is included with the bounds of this particular trip When performing the test, it uses
     * the local time zone for the date, and the defined time zones for the start and end date bounds. The start date
     * time is assumed to occur at 00:01 of the start day and the end date is assumed to occur at 23:59 of the end day.
     * The reasoning behind this is to ensure that it appears properly from a UI perspective. Since the initial date
     * only shows the day itself, it may include an arbitrary time that is never shown to the user. Setting the time
     * aspect manually accounts for this. This returns false if the date is null.
     *
     * @param date - the date to test
     * @return true if it is contained within. false otherwise
     */
    @Override
    public boolean isDateInsideTripBounds(Date date) {
        if (date == null)
            return false;

        // Build a calendar for the date we intend to test
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.setTime(date);
        testCalendar.setTimeZone(TimeZone.getDefault());

        // Build a calendar for the start date
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(mStartDate);
        startCalendar.setTimeZone((mStartTimeZone != null) ? mStartTimeZone : TimeZone.getDefault());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        // Build a calendar for the end date
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(mEndDate);
        endCalendar.setTimeZone((mEndTimeZone != null) ? mEndTimeZone : TimeZone.getDefault());
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);

        if (testCalendar.compareTo(startCalendar) >= 0 && testCalendar.compareTo(endCalendar) <= 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getPrice() {
        return getDecimalFormattedPrice();
    }

    @Override
    public float getPriceAsFloat() {
        if (mPrice != null) {
            return mPrice.floatValue();
        } else {
            return 0f;
        }
    }

    @Override
    public String getDecimalFormattedPrice() {
        return getDecimalFormat().format(getPriceAsFloat());
    }

    @Override
    public String getCurrencyFormattedPrice() {
        if (mCurrency != null) {
            return mCurrency.format(mPrice);
        } else {
            return "Mixed";
        }
    }

    @Override
    public String getDailySubTotal() {
        return getDecimalFormattedDailySubTotal();
    }

    @Override
    public float getDailySubTotalAsFloat() {
        if (mDailySubTotal != null) {
            return mDailySubTotal.floatValue();
        } else {
            return 0f;
        }
    }

    @Override
    public String getDecimalFormattedDailySubTotal() {
        return getDecimalFormat().format(getDailySubTotalAsFloat());
    }

    @Override
    public String getCurrencyFormattedDailySubTotal() {
        if (mCurrency != null) {
            return mCurrency.format(getDailySubTotalAsFloat());
        } else {
            return EMPTY_PRICE;
        }
    }

    @Override
    public WBCurrency getCurrency() {
        return mCurrency;
    }

    @Override
    public String getCurrencyCode() {
        if (mCurrency != null) {
            return mCurrency.getCurrencyCode();
        } else {
            return WBCurrency.MISSING_CURRENCY;
        }
    }

    @Override
    public WBCurrency getDefaultCurrency() {
        return mDefaultCurrency;
    }

    @Override
    public String getDefaultCurrencyCode() {
        if (mDefaultCurrency != null) {
            return mDefaultCurrency.getCurrencyCode();
        } else {
            return Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        }
    }

    @Override
    public float getMileage() {
        return -1f;
    }

    @Override
    public String getMilesAsString() {
        return "0";
    }

    @Override
    public void setMileage(float mileage) {
        // Stub
    }

    @Override
    public String getComment() {
        return mComment;
    }

    @Override
    public Source getSource() {
        return mSource;
    }

    @Override
    public void setPrice(double price) {
        mPrice = new BigDecimal(price);
    }

    @Override
    public void setDailySubTotal(double dailyTotal) {
        mDailySubTotal = new BigDecimal(dailyTotal);
    }

    @Override
    public Filter<ReceiptRow> getFilter() {
        return mFilter;
    }

    @Override
    public boolean hasFilter() {
        return mFilter != null;
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
        return this.getClass().getSimpleName() + "::\n" + "[" + "source => " + getSource() + "; \n" + "name => "
                + getName() + "; \n" + "directory =>" + getDirectory().getAbsolutePath() + "; \n" + "startDate =>"
                + getStartDate().toGMTString() + "; \n" + "endDate => " + getEndDate().toGMTString() + "; \n"
                + "currency =>" + getCurrency().getCurrencyCode() + "; \n" + "miles => " + getMilesAsString() + "]";
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
        DefaultTripImpl other = (DefaultTripImpl) obj;
        if (mReportDirectory == null) {
            if (other.mReportDirectory != null)
                return false;
        } else if (!mReportDirectory.equals(other.mReportDirectory))
            return false;
        return true;
    }

    public static final Creator<DefaultTripImpl> CREATOR = new Creator<DefaultTripImpl>() {

        @Override
        public DefaultTripImpl createFromParcel(Parcel source) {
            return new DefaultTripImpl(source);
        }

        @Override
        public DefaultTripImpl[] newArray(int size) {
            return new DefaultTripImpl[size];
        }

    };

}
