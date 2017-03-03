package co.smartreceipts.android.rating.data;

import rx.Single;

public interface AppRatingStorage {

    Single<AppRatingModel> readAppRatingData();

    void incrementLaunchCount();

    void setDontShowRatingPromptMore();

    void prorogueRatingPrompt(int prorogueLaunches);

    void crashOccurred();
}
