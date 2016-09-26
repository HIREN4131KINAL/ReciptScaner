package co.smartreceipts.android.sync.model;

import android.support.annotation.NonNull;

import java.util.Date;

import co.smartreceipts.android.sync.model.impl.Identifier;

public interface RemoteBackupMetadata {

    @NonNull
    Identifier getId();

    @NonNull
    Identifier getSyncDeviceId();

    @NonNull
    String getSyncDeviceName();

    @NonNull
    Date getLastModifiedDate();
}
