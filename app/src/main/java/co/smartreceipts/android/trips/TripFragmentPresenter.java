package co.smartreceipts.android.trips;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.rating.data.AppRatingManager;
import co.smartreceipts.android.rating.data.AppRatingPreferencesStorage;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class TripFragmentPresenter {

    private TripFragment mFragment;

    private AppRatingManager mRatingManager;

    public TripFragmentPresenter(@NonNull TripFragment fragment) {
        mFragment = Preconditions.checkNotNull(fragment);
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
