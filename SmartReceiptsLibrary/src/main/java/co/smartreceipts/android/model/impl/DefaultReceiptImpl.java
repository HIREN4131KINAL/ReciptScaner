package co.smartreceipts.android.model.impl;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
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
    private final Price mPrice, mTax;
    private final Date mDate;
    private final TimeZone mTimeZone;
    private final boolean mIsExpensable, mIsFullPage;
    private final Source mSource;
    private final String mExtraEditText1, mExtraEditText2, mExtraEditText3;
    private boolean mIsSelected;
    private File mFile;

    public DefaultReceiptImpl(int id, int index, Trip trip, File file, PaymentMethod paymentMethod, String name, String category, String comment,
                              Price price, Price tax, Date date, TimeZone timeZone, boolean isExpensable,
                              boolean isFullPage, boolean isSelected, Source source, String extraEditText1, String extraEditText2, String extraEditText3) {
        mId = id;
        mIndex = index;
        mTrip = trip;
        mFile = file;
        mPaymentMethod = paymentMethod;
        mName = name;
        mCategory = category;
        mComment = comment;
        mPrice = price;
        mTax = tax;
        mDate = date;
        mTimeZone = timeZone;
        mIsExpensable = isExpensable;
        mIsFullPage = isFullPage;
        mSource = source;
        mExtraEditText1 = extraEditText1;
        mExtraEditText2 = extraEditText2;
        mExtraEditText3 = extraEditText3;
        mIsSelected = isSelected;
    }

    private DefaultReceiptImpl(Parcel in) {
        mTrip = in.readParcelable(Trip.class.getClassLoader());
        mPaymentMethod = in.readParcelable(PaymentMethod.class.getClassLoader());
        mId = in.readInt();
        mName = in.readString();
        mCategory = in.readString();
        mComment = in.readString();
        mPrice = in.readParcelable(Price.class.getClassLoader());
        mTax = in.readParcelable(Price.class.getClassLoader());
        final String fileName = in.readString();
        mFile = TextUtils.isEmpty(fileName) ? null : new File(fileName);
        mDate = new Date(in.readLong());
        mIsExpensable = (in.readByte() != 0);
        mIsFullPage = (in.readByte() != 0);
        mIsSelected = (in.readByte() != 0);
        mExtraEditText1 = in.readString();
        mExtraEditText2 = in.readString();
        mExtraEditText3 = in.readString();
        mIndex = in.readInt();
        mTimeZone = TimeZone.getTimeZone(in.readString());
        mSource = Source.Parcel;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
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

    @NonNull
    @Override
    public Price getPrice() {
        return mPrice;
    }

    @Override
    public Price getTax() {
        return mTax;
    }

    @Override
    @NonNull
    public Date getDate() {
        return mDate;
    }

    @Override
    public String getFormattedDate(Context context, String separator) {
        return ModelUtils.getFormattedDate(mDate, (mTimeZone != null) ? mTimeZone : TimeZone.getDefault(), context, separator);
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
        if (DatabaseHelper.NO_DATA.equals(mExtraEditText1)) {
            return null;
        }
        else {
            return mExtraEditText1;
        }
    }

    @Override
    public String getExtraEditText2() {
        if (DatabaseHelper.NO_DATA.equals(mExtraEditText2)) {
            return null;
        }
        else {
            return mExtraEditText2;
        }
    }

    @Override
    public String getExtraEditText3() {
        if (DatabaseHelper.NO_DATA.equals(mExtraEditText3)) {
            return null;
        }
        else {
            return mExtraEditText3;
        }
    }

    @Override
    public boolean hasExtraEditText1() {
        return (mExtraEditText1 != null) && !mExtraEditText1.equals(DatabaseHelper.NO_DATA);
    }

    @Override
    public boolean hasExtraEditText2() {
        return (mExtraEditText2 != null) && !mExtraEditText2.equals(DatabaseHelper.NO_DATA);
    }

    @Override
    public boolean hasExtraEditText3() {
        return (mExtraEditText3 != null)&& !mExtraEditText3.equals(DatabaseHelper.NO_DATA);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        return this.getClass().getSimpleName() + "::\n" + "[" + "source => " + getSource() + "; \n" + "id => "
                + getId() + "; \n" + "name => " + getName() + "; \n" + "category =>" + getCategory() + "; \n"
                + "comment =>" + getComment() + "; \n" + "price =>" + getPrice() + "; \n" + "tax =>" + getTax()
                + "; \n" + "filePath =>" + getFilePath() + "; \n" + "; \n"
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
        dest.writeParcelable(getPrice(), flags);
        dest.writeParcelable(getTax(), flags);
        dest.writeString(getFilePath());
        dest.writeLong(getDate().getTime());
        dest.writeByte((byte) (isExpensable() ? 1 : 0));
        dest.writeByte((byte) (isFullPage() ? 1 : 0));
        dest.writeByte((byte) (isSelected() ? 1 : 0));
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