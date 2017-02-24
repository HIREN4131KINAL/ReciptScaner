package co.smartreceipts.android.rating;

public class AppRatingModel {

    private boolean mCanShow;
    private boolean mCrashOccurred;
    private int mLaunchCount;
    private long mInstallTime;
    private int mAdditionalLaunchThreshold;

    private boolean mJustInstalled;

    public AppRatingModel(boolean canShow, boolean crashOccurred, int launchCount, int additionalLaunchThreshold,
                          long installTime, boolean justInstalled) {
        mCanShow = canShow;
        mCrashOccurred = crashOccurred;
        mLaunchCount = launchCount;
        mInstallTime = installTime;
        mJustInstalled = justInstalled;
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

    public boolean isJustInstalled() {
        return mJustInstalled;
    }
}
