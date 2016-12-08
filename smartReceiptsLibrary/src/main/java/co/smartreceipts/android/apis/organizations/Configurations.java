package co.smartreceipts.android.apis.organizations;

import com.google.gson.annotations.SerializedName;

public class Configurations {

    @SerializedName("IsSettingsEnable")
    private final boolean isSettingsEnabled;

    public Configurations(boolean isSettingsEnabled) {
        this.isSettingsEnabled = isSettingsEnabled;
    }

    public boolean isSettingsEnabled() {
        return isSettingsEnabled;
    }
}
