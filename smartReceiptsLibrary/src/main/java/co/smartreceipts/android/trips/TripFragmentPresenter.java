package co.smartreceipts.android.trips;

import co.smartreceipts.android.rating.data.AppRatingManager;
import co.smartreceipts.android.rating.data.AppRatingPreferencesStorage;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class TripFragmentPresenter {

    private TripFragment mFragment;

    private AppRatingManager mRatingManager;

    public TripFragmentPresenter(TripFragment fragment) {
        mFragment = fragment;
        mRatingManager = AppRatingManager.getInstance(new AppRatingPreferencesStorage(mFragment.getContext()));
    }

    public void checkRating() {
        mRatingManager.checkIfNeedToAskRating()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean ratingPrompt) {
                        if (ratingPrompt) {
                            mFragment.showRatingTooltip();
                        }
                    }
                });
    }

    public void dontShowRatingPrompt() {
        mRatingManager.dontShowRatingPromptAgain();
    }
}
