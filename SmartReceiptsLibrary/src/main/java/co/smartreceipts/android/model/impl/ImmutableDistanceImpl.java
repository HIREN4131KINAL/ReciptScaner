package co.smartreceipts.android.model.impl;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.TimeZone;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;

/**
 * An immutable {@link co.smartreceipts.android.model.Distance} implementation to track distance.
 */
public class ImmutableDistanceImpl implements Distance {

    private final long mId;
    private final Trip mTrip;
    private final String mLocation;
    private final BigDecimal mDistance;
    private final Date mDate;
    private final TimeZone mTimezone;
    private final BigDecimal mRate;
    private final String mComment;
    private DecimalFormat mDecimalFormat;

    public ImmutableDistanceImpl(long id, Trip trip, String location, BigDecimal distance, BigDecimal rate, Date date, TimeZone timeZone, String comment) {
        mId = id;
        mTrip = trip;
        mLocation = location;
        mDistance = distance;
        mRate = rate;
        mDate = date;
        mTimezone = timeZone;
        mComment = comment;
    }

    protected ImmutableDistanceImpl(Parcel in) {
        mId = in.readLong();
        mTrip = in.readParcelable(Trip.class.getClassLoader());
        mLocation = in.readString();
        mDistance = (BigDecimal) in.readValue(BigDecimal.class.getClassLoader());
        long tmpDate = in.readLong();
        mDate = tmpDate != -1 ? new Date(tmpDate) : null;
        mTimezone = TimeZone.getTimeZone(in.readString());
        mRate = (BigDecimal) in.readValue(BigDecimal.class.getClassLoader());
        mComment = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeParcelable(mTrip, flags);
        dest.writeString(mLocation);
        dest.writeValue(mDistance);
        dest.writeLong(mDate != null ? mDate.getTime() : -1L);
        dest.writeString(mTimezone.getID());
        dest.writeValue(mRate);
        dest.writeString(mComment);
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    @NonNull
    public Trip getTrip() {
        return mTrip;
    }

    @Override
    public String getLocation() {
        return mLocation;
    }

    @Override
    public BigDecimal getDistance() {
        return mDistance;
    }

    @Override
    public String getDecimalFormattedDistance() {
        return getDecimalFormat().format(getDistance());
    }

    @Override
    public Date getDate() {
        return mDate;
    }

    @Override
    public String getFormattedDate(Context context, String separator) {
        java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
        format.setTimeZone(mTimezone);
        String formattedDate = format.format(mDate);
        formattedDate = formattedDate.replace(DateUtils.getDateSeparator(context), separator);
        return formattedDate;
    }

    @Override
    public TimeZone getTimezone() {
        return mTimezone;
    }

    @Override
    public BigDecimal getRate() {
        return mRate;
    }

    @Override
    public String getDecimalFormattedRate() {
        throw new UnsupportedOperationException("getDecimalFormattedRate has not been implemented yet");
    }

    @Override
    public String getCurrencyFormattedRate() {
        throw new UnsupportedOperationException("getCurrencyFormattedRate has not been implemented yet");
    }

    @Override
    public WBCurrency getCurrency() {
        throw new UnsupportedOperationException("getCurrency has not been implemented yet");
    }

    @Override
    public String getCurrencyCode() {
        throw new UnsupportedOperationException("getCurrencyCode has not been implemented yet");
    }

    @Override
    public String getComment() {
        return mComment;
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

    public static final Creator<ImmutableDistanceImpl> CREATOR = new Creator<ImmutableDistanceImpl>() {
        @Override
        public ImmutableDistanceImpl createFromParcel(Parcel in) {
            return new ImmutableDistanceImpl(in);
        }

        @Override
        public ImmutableDistanceImpl[] newArray(int size) {
            return new ImmutableDistanceImpl[size];
        }
    };

    @Override
    public String toString() {
        return "Distance [" + "mLocation=" + mLocation + ", mDistance=" + mDistance + ", mDate=" + mDate + ", mTimezone=" + mTimezone + ", mRate=" + mRate + ", mComment=" + mComment + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.valueOf(mId).hashCode();
        result = prime * result + ((mComment == null) ? 0 : mComment.hashCode());
        result = prime * result + ((mDate == null) ? 0 : mDate.hashCode());
        result = prime * result + ((mDistance == null) ? 0 : mDistance.hashCode());
        result = prime * result + ((mLocation == null) ? 0 : mLocation.hashCode());
        result = prime * result + ((mRate == null) ? 0 : mRate.hashCode());
        result = prime * result + ((mTimezone == null) ? 0 : mTimezone.hashCode());
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

        ImmutableDistanceImpl other = (ImmutableDistanceImpl) obj;

        if (mId != other.mId)
            return false;

        if (mComment == null) {
            if (other.mComment != null)
                return false;
        } else if (!mComment.equals(other.mComment))
            return false;

        if (mDate == null) {
            if (other.mDate != null)
                return false;
        } else if (!mDate.equals(other.mDate))
            return false;

        if (mDistance == null) {
            if (other.mDistance != null)
                return false;
        } else if (!mDistance.equals(other.mDistance))
            return false;

        if (mLocation == null) {
            if (other.mLocation != null)
                return false;
        } else if (!mLocation.equals(other.mLocation))
            return false;

        if (mRate == null) {
            if (other.mRate != null)
                return false;
        } else if (!mRate.equals(other.mRate))
            return false;

        if (mTimezone == null) {
            if (other.mTimezone != null)
                return false;
        } else if (!mTimezone.equals(other.mTimezone))
            return false;

        return true;
    }
}
