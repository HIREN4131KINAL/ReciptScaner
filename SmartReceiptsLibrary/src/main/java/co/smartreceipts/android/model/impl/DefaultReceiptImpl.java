package co.smartreceipts.android.model.impl;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.TimeZone;

import co.smartreceipts.android.R;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import wb.android.storage.StorageManager;

/**
 * A mostly immutable implementation of the {@link co.smartreceipts.android.model.Receipt} interface that
 * serves as the default implementation.
 */
public final class DefaultReceiptImpl implements Receipt {

    private final int mId;
    private final Trip mTrip;
    private final PaymentMethod mPaymentMethod;
    private final int mIndex; // Tracks the index in the list (if specified)
    private final String mName, mCategory, mComment;
    private final BigDecimal mPrice, mTax;
    private final WBCurrency mCurrency;
    private final Date mDate;
    private final TimeZone mTimeZone;
    private final boolean mIsExpensable, mIsFullPage;
    private final Source mSource;
    private final String mExtraEditText1, mExtraEditText2, mExtraEditText3;
    private DecimalFormat mDecimalFormat;
    private boolean mIsSelected;
    private File mFile;

    public DefaultReceiptImpl(int id, int index, Trip trip, PaymentMethod paymentMethod, String name, String category, String comment,
                               BigDecimal price, BigDecimal tax, WBCurrency currency, Date date, TimeZone timeZone, boolean isExpensable,
                               boolean isFullPage, Source source, String extraEditText1, String extraEditText2, String extraEditText3) {
        mId = id;
        mIndex = index;
        mTrip = trip;
        mPaymentMethod = paymentMethod;
        mName = name;
        mCategory = category;
        mComment = comment;
        mPrice = price;
        mTax = tax;
        mCurrency = currency;
        mDate = date;
        mTimeZone = timeZone;
        mIsExpensable = isExpensable;
        mIsFullPage = isFullPage;
        mSource = source;
        mExtraEditText1 = extraEditText1;
        mExtraEditText2 = extraEditText2;
        mExtraEditText3 = extraEditText3;
        mIsSelected = false;
    }

    private DefaultReceiptImpl(Parcel in) {
        mTrip = in.readParcelable(Trip.class.getClassLoader());
        mPaymentMethod = in.readParcelable(PaymentMethod.class.getClassLoader());
        mId = in.readInt();
        mName = in.readString();
        mCategory = in.readString();
        mComment = in.readString();
        mPrice = new BigDecimal(in.readFloat());
        mTax = new BigDecimal(in.readFloat());
        final String fileName = in.readString();
        mFile = TextUtils.isEmpty(fileName) ? null : new File(fileName);
        mDate = new Date(in.readLong());
        mCurrency = WBCurrency.getInstance(in.readString());
        mIsExpensable = (in.readByte() != 0);
        mIsFullPage = (in.readByte() != 0);
        mExtraEditText1 = in.readString();
        mExtraEditText2 = in.readString();
        mExtraEditText3 = in.readString();
        mIndex = in.readInt();
        mTimeZone = TimeZone.getTimeZone(in.readString());
        mSource = Source.Parcel;
        mIsSelected = false;
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
    public PaymentMethod getPaymentMethod() {
        return mPaymentMethod;
    }

    @Override
    public boolean hasPaymentMethod() {
        return mPaymentMethod != null;
    }

    @Override
    @NonNull
    public String getName() {
        return mName;
    }

    @Override
    public boolean hasFile() {
        return (mFile != null && mFile.exists());
    }

    @Override
    public boolean hasImage() {
        if (mFile != null && mFile.exists()) {
            final String extension = StorageManager.getExtension(mFile);
            if (extension != null
                    && (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") || extension
                    .equalsIgnoreCase("png"))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean hasPDF() {
        if (mFile != null && mFile.exists()) {
            final String extension = StorageManager.getExtension(mFile);
            if (extension != null && extension.equalsIgnoreCase("pdf")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String getMarkerAsString(Context context) {
        if (context == null) {
            return "";
        } else if (hasImage()) {
            return context.getString(R.string.image);
        } else if (hasPDF()) {
            return context.getString(R.string.pdf);
        } else {
            return "";
        }
    }

    @Override
    public File getImage() {
        return mFile;
    }

    @Override
    public File getPDF() {
        return mFile;
    }

    @Override
    public File getFile() {
        return mFile;
    }

    @Override
    public String getFilePath() {
        if (hasFile()) {
            return mFile.getAbsolutePath();
        } else {
            return "";
        }
    }

    @Override
    public String getFileName() {
        if (hasFile()) {
            return mFile.getName();
        } else {
            return "";
        }
    }

    @Override
    public void setFile(File file) {
        mFile = file;
    }

    @Override
    public Source getSource() {
        return mSource;
    }

    @Override
    public String getCategory() {
        return mCategory;
    }

    @Override
    public String getComment() {
        return mComment;
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
        return getDecimalFormat().format(mPrice);
    }

    @Override
    public String getCurrencyFormattedPrice() {
        if (getCurrency() != null) {
            return getCurrency().format(mPrice);
        } else {
            return getDecimalFormattedPrice();
        }
    }

    @Override
    public boolean isPriceEmpty() {
        return TextUtils.isEmpty(getPrice());
    }

    @Override
    public String getTax() {
        return getDecimalFormattedTax();
    }

    @Override
    public float getTaxAsFloat() {
        return mTax.floatValue();
    }

    @Override
    public String getDecimalFormattedTax() {
        return getDecimalFormat().format(mTax);
    }

    @Override
    public WBCurrency getCurrency() {
        return mCurrency;
    }

    @Override
    public String getCurrencyCode() {
        return getCurrency().getCurrencyCode();
    }

    @Override
    public String getCurrencyFormattedTax() {
        if (getCurrency() != null) {
            return getCurrency().format(mTax);
        } else {
            return getDecimalFormattedTax();
        }
    }

    @Override
    public Date getDate() {
        return mDate;
    }

    @Override
    public String getFormattedDate(Context context, String separator) {
        final TimeZone timeZone = (mTimeZone != null) ? mTimeZone : TimeZone.getDefault();
        java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
        format.setTimeZone(timeZone);
        String formattedDate = format.format(mDate);
        formattedDate = formattedDate.replace(DateUtils.getDateSeparator(context), separator);
        return formattedDate;
    }

    @Override
    public TimeZone getTimeZone() {
        return mTimeZone;
    }

    @Override
    public boolean isExpensable() {
        return mIsExpensable;
    }

    @Override
    public boolean isFullPage() {
        return mIsFullPage;
    }

    @Override
    public boolean isSelected() {
        return mIsSelected;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public String getExtraEditText1() {
        return mExtraEditText1;
    }

    @Override
    public String getExtraEditText2() {
        return mExtraEditText2;
    }

    @Override
    public String getExtraEditText3() {
        return mExtraEditText3;
    }

    @Override
    public boolean hasExtraEditText1() {
        return (mExtraEditText1 != null);
    }

    @Override
    public boolean hasExtraEditText2() {
        return (mExtraEditText2 != null);
    }

    @Override
    public boolean hasExtraEditText3() {
        return (mExtraEditText3 != null);
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
        return this.getClass().getSimpleName() + "::\n" + "[" + "source => " + getSource() + "; \n" + "id => "
                + getId() + "; \n" + "name => " + getName() + "; \n" + "category =>" + getCategory() + "; \n"
                + "comment =>" + getComment() + "; \n" + "price =>" + getPrice() + "; \n" + "tax =>" + getTax()
                + "; \n" + "filePath =>" + getFilePath() + "; \n" + "currency =>" + getCurrencyCode() + "; \n"
                + "date =>" + getDate().toGMTString() + "; \n" + "isExpensable =>" + isExpensable() + "; \n"
                + "isFullPage =>" + isFullPage() + "; \n" + "extraEditText1 =>" + getExtraEditText1() + "; \n"
                + "extraEditText2 =>" + getExtraEditText2() + "; \n" + "extraEditText3 =>" + getExtraEditText3() + "]";
    }

    @Override
    public int hashCode() {
        return 31 * mId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultReceiptImpl other = (DefaultReceiptImpl) obj;
        if (mId != other.mId) {
            return false;
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getTrip(), flags);
        dest.writeParcelable(getPaymentMethod(), flags);
        dest.writeInt(getId());
        dest.writeString(getName());
        dest.writeString(getCategory());
        dest.writeString(getComment());
        dest.writeFloat(getPriceAsFloat());
        dest.writeFloat(getTaxAsFloat());
        dest.writeString(getFilePath());
        dest.writeLong(getDate().getTime());
        dest.writeString(getCurrencyCode());
        dest.writeByte((byte) (isExpensable() ? 1 : 0));
        dest.writeByte((byte) (isFullPage() ? 1 : 0));
        dest.writeString(getExtraEditText1());
        dest.writeString(getExtraEditText2());
        dest.writeString(getExtraEditText3());
        dest.writeInt(getIndex());
        dest.writeString(mTimeZone.getID());
    }

    public static Creator<DefaultReceiptImpl> CREATOR = new Creator<DefaultReceiptImpl>() {

        @Override
        public DefaultReceiptImpl createFromParcel(Parcel source) {
            return new DefaultReceiptImpl(source);
        }

        @Override
        public DefaultReceiptImpl[] newArray(int size) {
            return new DefaultReceiptImpl[size];
        }

    };

}