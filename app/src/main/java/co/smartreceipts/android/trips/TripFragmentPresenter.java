package co.smartreceipts.android.trips;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.rating.AppRatingManager;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import wb.android.storage.StorageManager;

@FragmentScope
public class TripFragmentPresenter {

    @Inject
    TripFragment fragment;
    @Inject
    AppRatingManager appRatingManager;
    @Inject
    StorageManager storageManager;

    @Inject
    public TripFragmentPresenter() {
    }

    public void checkRating() {
        appRatingManager.checkIfNeedToAskRating()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean ratingPrompt) {
                        if (ratingPrompt) {
                            fragment.showRatingTooltip();
                        }
                    }
                });
    }

    public void dontShowRatingPrompt() {
        appRatingManager.dontShowRatingPromptAgain();
    }

    public boolean isExternalStorage() {
        return storageManager.isExternal();
    }

}
