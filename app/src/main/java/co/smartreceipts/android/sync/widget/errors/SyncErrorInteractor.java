package co.smartreceipts.android.sync.widget.errors;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.CriticalSyncError;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;


public class SyncErrorInteractor {

    private final FragmentActivity mActivity;
    private final BackupProvidersManager mBackupProvidersManager;
    private final Analytics mAnalytics;

    public SyncErrorInteractor(@NonNull FragmentActivity activity, @NonNull BackupProvidersManager backupProvidersManager, @NonNull Analytics analytics) {
        mActivity = Preconditions.checkNotNull(activity);
        mBackupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        mAnalytics = Preconditions.checkNotNull(analytics);
    }

    @NonNull
    public Observable<SyncErrorType> getErrorStream() {
        return mBackupProvidersManager.getCriticalSyncErrorStream()
                .map(CriticalSyncError::getSyncErrorType);
    }

    public void handleClick(@NonNull SyncErrorType syncErrorType) {
        final SyncProvider syncProvider = mBackupProvidersManager.getSyncProvider();
        Preconditions.checkArgument(syncProvider == SyncProvider.GoogleDrive, "Only Google Drive clicks are supported");

        mAnalytics.record(new DefaultDataPointEvent(Events.Sync.ClickSyncError).addDataPoint(new DataPoint(SyncErrorType.class.getName(), syncErrorType)));
        Logger.info(this, "Handling click for sync error: {}.", syncErrorType);
        if (syncErrorType == SyncErrorType.NoRemoteDiskSpace) {
            mBackupProvidersManager.markErrorResolved(syncErrorType);
        } else if (syncErrorType == SyncErrorType.UserDeletedRemoteData) {
            new NavigationHandler(mActivity).showDialog(new DriveRecoveryDialogFragment());
        } else if (syncErrorType == SyncErrorType.UserRevokedRemoteRights) {
            mBackupProvidersManager.initialize(mActivity);
            mBackupProvidersManager.markErrorResolved(syncErrorType);
        } else {
            throw new IllegalArgumentException("Unknown SyncErrorType");
        }
    }
}
