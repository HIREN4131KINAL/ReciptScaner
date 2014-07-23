package co.smartreceipts.android.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.TimeZone;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import co.smartreceipts.android.date.DateUtils;

public class DistanceRow implements Parcelable {

	public static final String PARCEL_KEY = "co.smartreceipts.android.model.DistanceRow";

	private final long mId;
	private final TripRow mTrip;
	private final String mLocation;
	private final BigDecimal mDistance;
	private final Date mDate;
	private final TimeZone mTimezone;
	private final BigDecimal mRate;
	private final String mComment;
    private DecimalFormat mDecimalFormat;

	public DistanceRow(long id, TripRow trip, String location, BigDecimal distance, BigDecimal rate, Date date, TimeZone timeZone, String comment) {
		mId = id;
		mTrip = trip;
		mLocation = location;
		mDistance = distance;
		mRate = rate;
		mDate = date;
		mTimezone = timeZone;
		mComment = comment;
	}

	protected DistanceRow(Parcel in) {
		mId = in.readLong();
		mTrip = in.readParcelable(TripRow.class.getClassLoader());
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

	public long getId() {
		return mId;
	}

	public TripRow getTrip() {
		return mTrip;
	}

	public String getLocation() {
		return mLocation;
	}

	public BigDecimal getDistance() {
		return mDistance;
	}

    public String getDecimalFormattedDistance() {
        return getDecimalFormat().format(getDistance());
    }

	public Date getDate() {
		return mDate;
	}

    public String getFormattedDate(Context context, String separator) {
        java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
        format.setTimeZone(mTimezone);
        String formattedDate = format.format(mDate);
        formattedDate = formattedDate.replace(DateUtils.getDateSeparator(context), separator);
        return formattedDate;
    }

	public TimeZone getTimezone() {
		return mTimezone;
	}

	public String getTimezoneCode() {
		return mTimezone.getID();
	}

	public BigDecimal getRate() {
		return mRate;
	}

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

	public static final class Builder {
		private long _id;
		private TripRow _trip;
		private String _location;
		private BigDecimal _distance;
		private Date _date;
		private TimeZone _timezone;
		private BigDecimal _rate;
		private String _comment;

		public Builder(long id) {
			_id = id;
		}

		public Builder setTrip(final TripRow trip) {
			_trip = trip;
			return this;
		}

		public Builder setLocation(String location) {
			_location = location;
			return this;
		}

		public Builder setDistance(BigDecimal distance) {
			_distance = distance;
			return this;
		}

		public Builder setDistance(double distance) {
			_distance = new BigDecimal(distance);
			return this;
		}

		public Builder setDate(Date date) {
			_date = date;
			return this;
		}

		public Builder setDate(long date) {
			_date = new Date(date);
			return this;
		}

		public Builder setTimezone(String timezone) {
			_timezone = TimeZone.getTimeZone(timezone);
			return this;
		}

		public Builder setTimezone(TimeZone timezone) {
			_timezone = timezone;
			return this;
		}

		public Builder setRate(BigDecimal rate) {
			_rate = rate;
			return this;
		}

		public Builder setRate(double rate) {
			_rate = new BigDecimal(rate);
			return this;
		}

		public Builder setComment(String comment) {
			_comment = comment;
			return this;
		}

		public DistanceRow build() {
			DistanceRow distance = new DistanceRow(_id, _trip, _location, _distance, _rate, _date, _timezone, _comment);
			return distance;
		}

	}

	public static final Parcelable.Creator<DistanceRow> CREATOR = new Parcelable.Creator<DistanceRow>() {
		@Override
		public DistanceRow createFromParcel(Parcel in) {
			return new DistanceRow(in);
		}

		@Override
		public DistanceRow[] newArray(int size) {
			return new DistanceRow[size];
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

		DistanceRow other = (DistanceRow) obj;

		if (mId != other.mId)
			return false;

		if (mComment == null) {
			if (other.mComment != null)
				return false;
		}
		else if (!mComment.equals(other.mComment))
			return false;

		if (mDate == null) {
			if (other.mDate != null)
				return false;
		}
		else if (!mDate.equals(other.mDate))
			return false;

		if (mDistance == null) {
			if (other.mDistance != null)
				return false;
		}
		else if (!mDistance.equals(other.mDistance))
			return false;

		if (mLocation == null) {
			if (other.mLocation != null)
				return false;
		}
		else if (!mLocation.equals(other.mLocation))
			return false;

		if (mRate == null) {
			if (other.mRate != null)
				return false;
		}
		else if (!mRate.equals(other.mRate))
			return false;

		if (mTimezone == null) {
			if (other.mTimezone != null)
				return false;
		}
		else if (!mTimezone.equals(other.mTimezone))
			return false;

		return true;
	}
}
