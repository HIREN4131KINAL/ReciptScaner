package co.smartreceipts.android.sync.widget.errors;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.widget.Tooltip;
import rx.Observable;
import rx.subjects.PublishSubject;

import static android.view.View.*;

public class SyncErrorPresenter {

    private final Tooltip mTooltip;

    private final PublishSubject<SyncErrorType> mClickStream = PublishSubject.create();

    private SyncProvider mSyncProvider = SyncProvider.None;

    public SyncErrorPresenter(@NonNull Tooltip tooltip) {

        mTooltip = Preconditions.checkNotNull(tooltip);
        mTooltip.setVisibility(GONE);
    }

    public void present(@NonNull final SyncErrorType syncErrorType) {
        if (mSyncProvider == SyncProvider.GoogleDrive) {
            if (syncErrorType == SyncErrorType.UserRevokedRemoteRights) {
                mTooltip.setErrorWithoutClose(R.string.drive_sync_error_no_permissions, new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickStream.onNext(syncErrorType);
                    }
                });
            } else if (syncErrorType == SyncErrorType.UserDeletedRemoteData) {
                mTooltip.setErrorWithoutClose(R.string.drive_sync_error_lost_data, new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTooltip.setVisibility(GONE);
                        mClickStream.onNext(syncErrorType);
                    }
                });
            } else if (syncErrorType == SyncErrorType.NoRemoteDiskSpace) {
                mTooltip.setError(R.string.drive_sync_error_no_space, new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTooltip.setVisibility(GONE);
                        mClickStream.onNext(syncErrorType);
                    }
                });
            }
            mTooltip.setVisibility(VISIBLE);
        }
    }

    public void present(@NonNull SyncProvider syncProvider) {
        if (syncProvider == SyncProvider.None) {
            mTooltip.setVisibility(GONE);
        }
        mSyncProvider = syncProvider;
    }

    @NonNull
    public Observable<SyncErrorType> getClickStream() {
        return mClickStream.asObservable();
    }

}
