package co.smartreceipts.android.rating.data;

import co.smartreceipts.android.rating.data.AppRatingModel;
import rx.Single;

public interface AppRatingStorage {

    Single<AppRatingModel> readAppRatingData();

    void incrementLaunchCount();

    void saveInstallTime();

    void setDontShowRatingPromptMore();

    void prorogueRatingPrompt(int prorogueLaunches);

    void crashOccurred();
}
