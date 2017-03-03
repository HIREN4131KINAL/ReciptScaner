package co.smartreceipts.android.trips;

import co.smartreceipts.android.rating.data.AppRatingManager;
import co.smartreceipts.android.rating.data.AppRatingStorageImpl;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class TripFragmentPresenter {

    private TripFragment mFragment;

    private AppRatingManager mRatingManager;

    public TripFragmentPresenter(TripFragment fragment) {
        mFragment = fragment;
        mRatingManager = AppRatingManager.getInstance(new AppRatingStorageImpl(mFragment.getContext()));
    }

    public void checkRating() {
        mRatingManager.checkIfNeedToAskRating()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean ratingPrompt) {
                        //// TODO: 02.03.2017 uncomment
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
