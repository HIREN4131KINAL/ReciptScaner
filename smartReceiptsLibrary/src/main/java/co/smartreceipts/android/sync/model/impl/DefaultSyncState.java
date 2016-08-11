package co.smartreceipts.android.sync.model.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;

import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.model.Identifier;
import co.smartreceipts.android.sync.model.SyncState;

public class DefaultSyncState implements SyncState {

    private final Identifier mIdentifier;
    private final DeletionMarkings mDeletionInformation;
    private final Date mLastLocalModificationTime;

    public DefaultSyncState(@NonNull Identifier identifier, @NonNull DeletionMarkings deletionInformation, @NonNull Date lastLocalModificationTime) {
        mIdentifier = Preconditions.checkNotNull(identifier);
        mDeletionInformation = Preconditions.checkNotNull(deletionInformation);
        mLastLocalModificationTime = Preconditions.checkNotNull(lastLocalModificationTime);
    }

    private DefaultSyncState(@NonNull Parcel source) {
        mIdentifier = source.readParcelable(getClass().getClassLoader());
        mDeletionInformation = (DeletionMarkings) source.readSerializable();
        mLastLocalModificationTime = new Date(source.readLong());
    }

    @NonNull
    @Override
    public Identifier getSyncId(@NonNull SyncProvider provider) {
        return mIdentifier;
    }

    @Override
    public boolean isMarkedForDeletion(@NonNull SyncProvider provider) {
        return mDeletionInformation.isMarkedForDeletion(provider);
    }

    @NonNull
    @Override
    public Date getLastLocalModificationTime() {
        return mLastLocalModificationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultSyncState)) return false;

        DefaultSyncState that = (DefaultSyncState) o;

        if (!mIdentifier.equals(that.mIdentifier)) return false;
        if (!mDeletionInformation.equals(that.mDeletionInformation)) return false;
        return mLastLocalModificationTime.equals(that.mLastLocalModificationTime);

    }

    @Override
    public int hashCode() {
        int result = mIdentifier.hashCode();
        result = 31 * result + mDeletionInformation.hashCode();
        result = 31 * result + mLastLocalModificationTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultSyncState{" +
                "mIdentifier=" + mIdentifier +
                ", mDeletionInformation=" + mDeletionInformation +
                ", mLastLocalModificationTime=" + mLastLocalModificationTime +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mIdentifier, flags);
        dest.writeSerializable(mDeletionInformation);
        dest.writeLong(mLastLocalModificationTime.getTime());
    }

    public static final Parcelable.Creator<DefaultSyncState> CREATOR = new Parcelable.Creator<DefaultSyncState>() {
        public DefaultSyncState createFromParcel(Parcel source) {
            return new DefaultSyncState(source);
        }

        public DefaultSyncState[] newArray(int size) {
            return new DefaultSyncState[size];
        }
    };

}
