package co.smartreceipts.android.sync.widget.errors;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import rx.Observable;
import rx.subjects.PublishSubject;

public class SyncErrorPresenter {

    private final View mErrorLayout;
    private final View mErrorIconView;
    private final View mCloseIconView;
    private final TextView mMessageTextView;

    private final PublishSubject<SyncErrorType> mClickStream = PublishSubject.create();

    private SyncProvider mSyncProvider = SyncProvider.None;

    public SyncErrorPresenter(@NonNull View view) {
        mErrorLayout = Preconditions.checkNotNull(view);
        mErrorIconView = Preconditions.checkNotNull(view.findViewById(R.id.error_icon));
        mCloseIconView = Preconditions.checkNotNull(view.findViewById(R.id.close_icon));
        mMessageTextView = Preconditions.checkNotNull((TextView) view.findViewById(R.id.error_message));
        mErrorLayout.setVisibility(View.GONE);
    }

    public void present(@NonNull final SyncErrorType syncErrorType) {
        if (mSyncProvider == SyncProvider.GoogleDrive) {
            if (syncErrorType == SyncErrorType.UserRevokedRemoteRights) {
                mErrorLayout.setClickable(true);
                mErrorIconView.setVisibility(View.VISIBLE);
                mCloseIconView.setVisibility(View.GONE);
                mMessageTextView.setText(mMessageTextView.getContext().getText(R.string.drive_sync_error_no_permissions));
                mErrorLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickStream.onNext(syncErrorType);
                    }
                });
                mCloseIconView.setOnClickListener(null);
            } else if (syncErrorType == SyncErrorType.UserDeletedRemoteData) {
                mErrorLayout.setClickable(true);
                mErrorIconView.setVisibility(View.VISIBLE);
                mCloseIconView.setVisibility(View.GONE);
                mMessageTextView.setText(mMessageTextView.getContext().getText(R.string.drive_sync_error_lost_data));
                mErrorLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickStream.onNext(syncErrorType);
                    }
                });
                mCloseIconView.setOnClickListener(null);
            } else if (syncErrorType == SyncErrorType.NoRemoteDiskSpace) {
                mErrorLayout.setClickable(false);
                mErrorIconView.setVisibility(View.VISIBLE);
                mCloseIconView.setVisibility(View.VISIBLE);
                mMessageTextView.setText(mMessageTextView.getContext().getText(R.string.drive_sync_error_no_space));
                mErrorLayout.setOnClickListener(null);
                mCloseIconView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mErrorLayout.setVisibility(View.GONE);
                        mClickStream.onNext(syncErrorType);
                    }
                });
            }
            mErrorLayout.setVisibility(View.VISIBLE);
        }
    }

    public void present(@NonNull SyncProvider syncProvider) {
        if (syncProvider == SyncProvider.None) {
            mErrorLayout.setVisibility(View.GONE);
        }
        mSyncProvider = syncProvider;
    }

    @NonNull
    public Observable<SyncErrorType> getClickStream() {
        return mClickStream.asObservable();
    }

}
