package co.smartreceipts.android.rating.data;

public class AppRatingModel {

    private boolean mCanShow;
    private boolean mCrashOccurred;
    private int mLaunchCount;
    private long mInstallTime;
    private int mAdditionalLaunchThreshold;

    public AppRatingModel(boolean canShow, boolean crashOccurred, int launchCount, int additionalLaunchThreshold,
                          long installTime) {
        mCanShow = canShow;
        mCrashOccurred = crashOccurred;
        mLaunchCount = launchCount;
        mInstallTime = installTime;
        mAdditionalLaunchThreshold = additionalLaunchThreshold;
    }

    public boolean canShow() {
        return mCanShow;
    }

    public boolean isCrashOccurred() {
        return mCrashOccurred;
    }

    public int getLaunchCount() {
        return mLaunchCount;
    }

    public long getInstallTime() {
        return mInstallTime;
    }

    public int getAdditionalLaunchThreshold() {
        return mAdditionalLaunchThreshold;
    }

}
