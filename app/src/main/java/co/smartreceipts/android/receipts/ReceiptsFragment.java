package co.smartreceipts.android.receipts;

import android.support.v7.app.ActionBar;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBListFragment;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class ReceiptsFragment extends WBListFragment {

    public static final String TAG = "ReceiptsFragment";

    protected Trip trip;
    private Disposable disposable;

    public static ReceiptsListFragment newListInstance() {
        return new ReceiptsListFragment();
    }

    @Override
    public void onPause() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        super.onPause();
    }

    protected void updateActionBarTitle(boolean updateSubtitle) {
        if (trip == null) {
            return;
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && getUserVisibleHint()) {
            if (updateSubtitle) {
                PersistenceManager persistenceManager = getPersistenceManager();
                if (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.ShowReceiptID)) {
                    disposable = persistenceManager.getDatabase().getNextReceiptAutoIncremenetIdHelper()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(receiptId ->  {
                                    if (isResumed()) {
                                        final ActionBar bar = getSupportActionBar();
                                        if (bar != null) {
                                            bar.setSubtitle(getString(R.string.next_id, receiptId));
                                        }
                                    }
                            });
                } else {
                    actionBar.setSubtitle(getString(R.string.daily_total, trip.getDailySubTotal().getCurrencyFormattedPrice()));
                }
            }
        }
    }

    protected abstract PersistenceManager getPersistenceManager();


}
