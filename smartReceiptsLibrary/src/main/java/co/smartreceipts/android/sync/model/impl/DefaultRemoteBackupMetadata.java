package co.smartreceipts.android.sync.model.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Date;

import co.smartreceipts.android.sync.model.RemoteBackupMetadata;

public class DefaultRemoteBackupMetadata implements RemoteBackupMetadata {

    private final Identifier mIdentifier;
    private final Identifier mSyncDeviceIdentifier;
    private final String mSyncDeviceName;
    private final Date mLastModifiedDate;

    public DefaultRemoteBackupMetadata(@NonNull Identifier identifier, @NonNull Identifier syncDeviceIdentifier, @NonNull String syncDeviceName,
                                       @NonNull Date lastModifiedDate) {
        mIdentifier = Preconditions.checkNotNull(identifier);
        mSyncDeviceIdentifier = Preconditions.checkNotNull(syncDeviceIdentifier);
        mSyncDeviceName = Preconditions.checkNotNull(syncDeviceName);
        mLastModifiedDate = Preconditions.checkNotNull(lastModifiedDate);
    }

    @NonNull
    @Override
    public Identifier getId() {
        return mIdentifier;
    }

    @NonNull
    @Override
    public Identifier getSyncDeviceId() {
        return mSyncDeviceIdentifier;
    }

    @NonNull
    @Override
    public String getSyncDeviceName() {
        return mSyncDeviceName;
    }

    @NonNull
    @Override
    public Date getLastModifiedDate() {
        return mLastModifiedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultRemoteBackupMetadata)) return false;

        DefaultRemoteBackupMetadata that = (DefaultRemoteBackupMetadata) o;

        if (!mIdentifier.equals(that.mIdentifier)) return false;
        if (!mSyncDeviceIdentifier.equals(that.mSyncDeviceIdentifier)) return false;
        if (!mSyncDeviceName.equals(that.mSyncDeviceName)) return false;
        return mLastModifiedDate.equals(that.mLastModifiedDate);

    }

    @Override
    public int hashCode() {
        int result = mIdentifier.hashCode();
        result = 31 * result + mSyncDeviceIdentifier.hashCode();
        result = 31 * result + mSyncDeviceName.hashCode();
        result = 31 * result + mLastModifiedDate.hashCode();
        return result;
    }
}
