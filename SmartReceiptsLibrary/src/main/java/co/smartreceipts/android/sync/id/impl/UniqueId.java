package co.smartreceipts.android.sync.id.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.id.Identifier;

/**
 * An implementation of the {@link co.smartreceipts.android.sync.id.Identifier} interface
 * for unique ids for sync data.
 */
public class UniqueId implements Identifier {

    private final String mId;

    public UniqueId(@NonNull String id) {
        mId = id;
    }

    private UniqueId(Parcel in) {
        this.mId = in.readString();
    }

    @Override
    @NonNull
    public String getId() {
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

    public static final Creator<UniqueId> CREATOR = new Creator<UniqueId>() {
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
