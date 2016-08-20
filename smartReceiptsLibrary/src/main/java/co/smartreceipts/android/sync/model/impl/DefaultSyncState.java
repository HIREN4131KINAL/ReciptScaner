package co.smartreceipts.android.sync.model.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.sql.Date;

import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.model.SyncState;

public class DefaultSyncState implements SyncState {

    private final IdentifierMap mIdentifierMap;
    private final MarkedForDeletionMap mMarkedForDeletionMap;
    private final Date mLastLocalModificationTime;

    public DefaultSyncState() {
        this(null, null, new Date(System.currentTimeMillis()));
    }

    public DefaultSyncState(@Nullable IdentifierMap identifierMap, @Nullable MarkedForDeletionMap markedForDeletionMap, @Nullable Date lastLocalModificationTime) {
        mIdentifierMap = identifierMap;
        mMarkedForDeletionMap = markedForDeletionMap;
        mLastLocalModificationTime = lastLocalModificationTime;
    }

    private DefaultSyncState(@NonNull Parcel source) {
        mIdentifierMap = (IdentifierMap) source.readSerializable();
        mMarkedForDeletionMap = (MarkedForDeletionMap) source.readSerializable();
        mLastLocalModificationTime = new Date(source.readLong());
    }

    @Nullable
    @Override
    public Identifier getSyncId(@NonNull SyncProvider provider) {
        if (mIdentifierMap != null) {
            return mIdentifierMap.getSyncId(provider);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public IdentifierMap getIdentifierMap() {
        return mIdentifierMap;
    }

    @Override
    public boolean isMarkedForDeletion(@NonNull SyncProvider provider) {
        if (mMarkedForDeletionMap != null) {
            return mMarkedForDeletionMap.isMarkedForDeletion(provider);
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public MarkedForDeletionMap getMarkedForDeletionMap() {
        return mMarkedForDeletionMap;
    }

    @NonNull
    @Override
    public Date getLastLocalModificationTime() {
        if (mLastLocalModificationTime != null) {
            return mLastLocalModificationTime;
        } else {
            return new Date(0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultSyncState)) return false;

        DefaultSyncState syncState = (DefaultSyncState) o;

        if (mIdentifierMap != null ? !mIdentifierMap.equals(syncState.mIdentifierMap) : syncState.mIdentifierMap != null)
            return false;
        if (mMarkedForDeletionMap != null ? !mMarkedForDeletionMap.equals(syncState.mMarkedForDeletionMap) : syncState.mMarkedForDeletionMap != null)
            return false;
        return mLastLocalModificationTime != null ? mLastLocalModificationTime.equals(syncState.mLastLocalModificationTime) : syncState.mLastLocalModificationTime == null;

    }

    @Override
    public int hashCode() {
        int result = mIdentifierMap != null ? mIdentifierMap.hashCode() : 0;
        result = 31 * result + (mMarkedForDeletionMap != null ? mMarkedForDeletionMap.hashCode() : 0);
        result = 31 * result + (mLastLocalModificationTime != null ? mLastLocalModificationTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DefaultSyncState{" +
                "mIdentifierMap=" + mIdentifierMap +
                ", mMarkedForDeletionMap=" + mMarkedForDeletionMap +
                ", mLastLocalModificationTime=" + mLastLocalModificationTime +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mIdentifierMap);
        dest.writeSerializable(mMarkedForDeletionMap);
        dest.writeLong(getLastLocalModificationTime().getTime());
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
