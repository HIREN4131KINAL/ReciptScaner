package co.smartreceipts.android.fragments;

import android.support.v7.app.ActionBar;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.catalog.UserPreference;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ReceiptsFragment extends WBListFragment {

    public static final String TAG = "ReceiptsFragment";

    protected Trip mTrip;
    private Subscription mIdSubscription;

    public static ReceiptsListFragment newListInstance() {
        return new ReceiptsListFragment();
    }

    @Override
    public void onPause() {
        if (mIdSubscription != null) {
            mIdSubscription.unsubscribe();
            mIdSubscription = null;
        }
        super.onPause();
    }

    protected void updateActionBarTitle(boolean updateSubtitle) {
        if (mTrip == null) {
            return;
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && getUserVisibleHint()) {
            if (updateSubtitle) {
                if (getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.ShowReceiptID)) {
                    mIdSubscription = getPersistenceManager().getDatabase().getNextReceiptAutoIncremenetIdHelper()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Integer>() {
                                @Override
                                public void call(Integer integer) {
                                    if (isResumed()) {
                                        final ActionBar actionBar = getSupportActionBar();
                                        if (actionBar != null) {
                                            actionBar.setSubtitle(getString(R.string.next_id, integer));
                                        }
                                    }
                                }
                            });
                } else {
                    actionBar.setSubtitle(getString(R.string.daily_total, mTrip.getDailySubTotal().getCurrencyFormattedPrice()));
                }
            }
        }
    }

}
