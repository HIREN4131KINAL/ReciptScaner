package co.smartreceipts.android.utils;

public enum FeatureFlags {

    SmartReceiptsLogin(false);

    private final boolean isEnabled;

    FeatureFlags(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
