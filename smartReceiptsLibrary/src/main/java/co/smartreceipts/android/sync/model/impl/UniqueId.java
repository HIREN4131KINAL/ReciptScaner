package co.smartreceipts.android.sync.model.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.Serializable;

import co.smartreceipts.android.sync.model.Identifier;

/**
 * An implementation of the interface for unique ids for sync data.
 */
public class UniqueId implements Identifier {

    private final String mId;

    public UniqueId(@NonNull String id) {
        mId = Preconditions.checkNotNull(id);
    }

    private UniqueId(Parcel in) {
        mId = in.readString();
    }

    @Override
    @NonNull
    public String getId() {
        return mId;
    }

    @Override
    public int length() {
        return mId.length();
    }

    @Override
    public char charAt(int i) {
        return mId.charAt(i);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return mId.subSequence(start, end);
    }

    @Override
    @NonNull
    public String toString() {
        return mId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
    }

    public static final Parcelable.Creator<UniqueId> CREATOR = new Parcelable.Creator<UniqueId>() {
        public UniqueId createFromParcel(Parcel source) {
            return new UniqueId(source);
        }

        public UniqueId[] newArray(int size) {
            return new UniqueId[size];
        }
    };

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueId uniqueId = (UniqueId) o;

        return mId.equals(uniqueId.mId);
    }

}
