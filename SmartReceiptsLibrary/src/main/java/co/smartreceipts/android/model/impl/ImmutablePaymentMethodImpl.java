package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.PaymentMethod;

/**
 * An immutable implementation of {@link co.smartreceipts.android.model.PaymentMethod}.
 *
 * @author Will Baumann
 */
public class ImmutablePaymentMethodImpl implements PaymentMethod {

    private final int mId;
    private final String mMethod;

    public ImmutablePaymentMethodImpl(final int id, final String method) {
        mId = id;
        mMethod = method;
    }

    private ImmutablePaymentMethodImpl(final Parcel in) {
        mId = in.readInt();
        mMethod = in.readString();
    }

    /**
     * @return - the database primary key id for this method
     */
    @Override
    public int getId() {
        return mId;
    }

    @Override
    @NonNull
    public String getMethod() {
        return mMethod;
    }

    @Override
    public String toString() {
        return mMethod;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mId;
        result = prime * result + ((mMethod == null) ? 0 : mMethod.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ImmutablePaymentMethodImpl other = (ImmutablePaymentMethodImpl) obj;
        if (mId != other.mId) {
            return false;
        }
        if (mMethod == null) {
            if (other.mMethod != null) {
                return false;
            }
        } else if (!mMethod.equals(other.mMethod)) {
            return false;
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeInt(mId);
        out.writeString(mMethod);
    }

    public static Creator<ImmutablePaymentMethodImpl> CREATOR = new Creator<ImmutablePaymentMethodImpl>() {

        @Override
        public ImmutablePaymentMethodImpl createFromParcel(Parcel source) {
            return new ImmutablePaymentMethodImpl(source);
        }

        @Override
        public ImmutablePaymentMethodImpl[] newArray(int size) {
            return new ImmutablePaymentMethodImpl[size];
        }

    };

}
