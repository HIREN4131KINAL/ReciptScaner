package co.smartreceipts.android.sync.widget.errors;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.AnalyticsLogger;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.CriticalSyncError;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import rx.Observable;
import rx.functions.Func1;

public class SyncErrorInteractor {

    private final BackupProvidersManager mBackupProvidersManager;
    private final Analytics mAnalytics;

    public SyncErrorInteractor(@NonNull BackupProvidersManager backupProvidersManager, @NonNull Analytics analytics) {
        mBackupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        mAnalytics = Preconditions.checkNotNull(analytics);
    }

    @NonNull
    public Observable<SyncErrorType> getErrorStream() {
        return mBackupProvidersManager.getCriticalSyncErrorStream()
                .map(new Func1<CriticalSyncError, SyncErrorType>() {
                    @Override
                    public SyncErrorType call(@NonNull CriticalSyncError criticalSyncError) {
                        return criticalSyncError.getSyncErrorType();
                    }
                });
    }

    public void handleClick(@NonNull SyncErrorType syncErrorType) {
        final SyncProvider syncProvider = mBackupProvidersManager.getSyncProvider();
        Preconditions.checkArgument(syncProvider == SyncProvider.GoogleDrive, "Only Google Drive clicks are supported");

        if (syncErrorType == SyncErrorType.NoRemoteDiskSpace) {

        } else if (syncErrorType == SyncErrorType.UserDeletedRemoteData) {

        } else if (syncErrorType == SyncErrorType.UserRevokedRemoteRights) {

        } else {
            throw new IllegalArgumentException("Unknown SyncErrorType");
        }
    }
}
