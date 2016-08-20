package co.smartreceipts.android.sync.model.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import java.sql.Date;

import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.model.Identifier;
import co.smartreceipts.android.sync.model.SyncState;

public class DefaultSyncState implements SyncState {

    private final IdentifierMap mIdentifierMap;
    private final MarkedForDeletionMap mDeletionInformation;
    private final Date mLastLocalModificationTime;

    public DefaultSyncState(@NonNull IdentifierMap identifierMap, @NonNull MarkedForDeletionMap deletionInformation, @NonNull Date lastLocalModificationTime) {
        mIdentifierMap = Preconditions.checkNotNull(identifierMap);
        mDeletionInformation = Preconditions.checkNotNull(deletionInformation);
        mLastLocalModificationTime = Preconditions.checkNotNull(lastLocalModificationTime);
    }

    public DefaultSyncState(@NonNull String identifierMapJson, @NonNull String deletionInformationJson, long lastLocalModificationTime) {
        final Gson gson = new Gson();
        mIdentifierMap = gson.fromJson(Preconditions.checkNotNull(identifierMapJson), IdentifierMap.class);
        mDeletionInformation = gson.fromJson(Preconditions.checkNotNull(deletionInformationJson), MarkedForDeletionMap.class);
        mLastLocalModificationTime = new Date(lastLocalModificationTime);
    }

    private DefaultSyncState(@NonNull Parcel source) {
        mIdentifierMap = (IdentifierMap) source.readSerializable();
        mDeletionInformation = (MarkedForDeletionMap) source.readSerializable();
        mLastLocalModificationTime = new Date(source.readLong());
    }

    @Nullable
    @Override
    public Identifier getSyncId(@NonNull SyncProvider provider) {
        return mIdentifierMap.getSyncId(provider);
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

        if (!mIdentifierMap.equals(that.mIdentifierMap)) return false;
        if (!mDeletionInformation.equals(that.mDeletionInformation)) return false;
        return mLastLocalModificationTime.equals(that.mLastLocalModificationTime);

    }

    @Override
    public int hashCode() {
        int result = mIdentifierMap.hashCode();
        result = 31 * result + mDeletionInformation.hashCode();
        result = 31 * result + mLastLocalModificationTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultSyncState{" +
                "mIdentifierMap=" + mIdentifierMap +
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
        dest.writeSerializable(mIdentifierMap);
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
