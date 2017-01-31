package co.smartreceipts.android.sync.widget.errors;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Event;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.sync.BackupProviderChangeListener;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.log.Logger;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class SyncErrorFragment extends Fragment implements BackupProviderChangeListener {

    private SyncErrorInteractor mSyncErrorInteractor;
    private SyncErrorPresenter mSyncErrorPresenter;
    private BackupProvidersManager mBackupProvidersManager;
    private Analytics mAnalytics;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SmartReceiptsApplication application = (SmartReceiptsApplication) getActivity().getApplication();
        mBackupProvidersManager = application.getBackupProvidersManager();
        mAnalytics = application.getAnalyticsManager();
        mSyncErrorInteractor = new SyncErrorInteractor(getActivity(), mBackupProvidersManager, mAnalytics);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.error_information_tooltip, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSyncErrorPresenter = new SyncErrorPresenter(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCompositeSubscription = new CompositeSubscription();

        mCompositeSubscription.add(mSyncErrorPresenter.getClickStream()
            .subscribe(new Action1<SyncErrorType>() {
                @Override
                public void call(SyncErrorType syncErrorType) {
                    mSyncErrorInteractor.handleClick(syncErrorType);
                }
            }));

        mBackupProvidersManager.registerChangeListener(this);
        updateForProvider(mBackupProvidersManager.getSyncProvider());
    }

    @Override
    public void onPause() {
        mBackupProvidersManager.unregisterChangeListener(this);
        mCompositeSubscription.unsubscribe();
        super.onPause();
    }

    @Override
    public void onProviderChanged(@NonNull SyncProvider newProvider) {
        updateForProvider(newProvider);
    }

    private void updateForProvider(@NonNull SyncProvider provider) {
        mSyncErrorPresenter.present(provider);
        mCompositeSubscription.add(mSyncErrorInteractor.getErrorStream()
            .doOnNext(new Action1<SyncErrorType>() {
                @Override
                public void call(SyncErrorType syncErrorType) {
                    mAnalytics.record(new DefaultDataPointEvent(Events.Sync.DisplaySyncError).addDataPoint(new DataPoint(SyncErrorType.class.getName(), syncErrorType)));
                    Logger.info(this, "Received sync error: {}.", syncErrorType);
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<SyncErrorType>() {
                @Override
                public void call(SyncErrorType syncErrorType) {
                    mSyncErrorPresenter.present(syncErrorType);
                }
            }));
    }
}
