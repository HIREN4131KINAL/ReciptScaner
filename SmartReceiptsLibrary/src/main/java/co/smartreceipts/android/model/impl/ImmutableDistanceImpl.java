package co.smartreceipts.android.model.impl;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * An immutable {@link co.smartreceipts.android.model.Distance} implementation to track distance.
 */
public final class ImmutableDistanceImpl implements Distance {

    private final int mId;
    private final Trip mTrip;
    private final String mLocation;
    private final BigDecimal mDistance;
    private final Date mDate;
    private final TimeZone mTimezone;
    private final BigDecimal mRate;
    private final WBCurrency mCurrency;
    private final String mComment;

    public ImmutableDistanceImpl(int id, Trip trip, String location, BigDecimal distance, BigDecimal rate, WBCurrency currency, Date date, TimeZone timeZone, String comment) {
        mId = id;
        mTrip = trip;
        mLocation = location;
        mDistance = distance;
        mRate = rate;
        mCurrency = currency;
        mDate = date;
        mTimezone = timeZone;
        mComment = comment;
    }

    protected ImmutableDistanceImpl(Parcel in) {
        mId = in.readInt();
        mTrip = in.readParcelable(Trip.class.getClassLoader());
        mLocation = in.readString();
        mDistance = (BigDecimal) in.readValue(BigDecimal.class.getClassLoader());
        long tmpDate = in.readLong();
        mDate = tmpDate != -1 ? new Date(tmpDate) : null;
        mTimezone = TimeZone.getTimeZone(in.readString());
        mRate = (BigDecimal) in.readValue(BigDecimal.class.getClassLoader());
        mCurrency = WBCurrency.getInstance(in.readString());
        mComment = in.readString();
    }

    @Override
    public int getId() {
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
        return ModelUtils.getDecimalFormattedValue(mDistance);
    }

    @Override
    public Date getDate() {
        return mDate;
    }

    @Override
    public String getFormattedDate(Context context, String separator) {
        return ModelUtils.getFormattedDate(mDate, mTimezone, context, separator);
    }

    @Override
    public TimeZone getTimeZone() {
        return mTimezone;
    }

    @Override
    public BigDecimal getRate() {
        return mRate;
    }

    @Override
    public String getDecimalFormattedRate() {
        return ModelUtils.getDecimalFormattedValue(mRate);
    }

    @Override
    public String getCurrencyFormattedRate() {
        return ModelUtils.getCurrencyFormattedValue(mRate, mCurrency);
    }

    @Override
    public BigDecimal getPrice() {
        return mRate.multiply(mDistance);
    }

    @Override
    public String getDecimalFormattedPrice() {
        return ModelUtils.getDecimalFormattedValue(getPrice());
    }

    @Override
    public String getCurrencyFormattedPrice() {
        return ModelUtils.getCurrencyFormattedValue(getPrice(), mCurrency);
    }

    @Override
    public WBCurrency getCurrency() {
        return mCurrency;
    }

    @Override
    public String getCurrencyCode() {
        return mCurrency.getCurrencyCode();
    }

    @Override
    public String getComment() {
        return mComment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeParcelable(mTrip, flags);
        dest.writeString(mLocation);
        dest.writeValue(mDistance);
        dest.writeLong(mDate != null ? mDate.getTime() : -1L);
        dest.writeString(mTimezone.getID());
        dest.writeValue(mRate);
        dest.writeString(mCurrency.getCurrencyCode());
        dest.writeString(mComment);
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
        return "Distance [" + "mLocation=" + mLocation + ", mDistance=" + mDistance + ", mDate=" + mDate + ", mTimezone=" + mTimezone + ", mRate=" + mRate + ", mCurrency= " + mCurrency + ", mComment=" + mComment + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Integer.valueOf(mId).hashCode();
        result = prime * result + ((mComment == null) ? 0 : mComment.hashCode());
        result = prime * result + ((mDate == null) ? 0 : mDate.hashCode());
        result = prime * result + ((mDistance == null) ? 0 : mDistance.hashCode());
        result = prime * result + ((mLocation == null) ? 0 : mLocation.hashCode());
        result = prime * result + ((mRate == null) ? 0 : mRate.hashCode());
        result = prime * result + ((mCurrency == null) ? 0 : mCurrency.hashCode());
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

        if (mCurrency == null) {
            if (other.mCurrency != null)
                return false;
        } else if (!mCurrency.equals(other.mCurrency))
            return false;

        if (mTimezone == null) {
            if (other.mTimezone != null)
                return false;
        } else if (!mTimezone.equals(other.mTimezone))
            return false;

        return true;
    }
}
