package co.smartreceipts.android.settings.versions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;

public class AppVersionManager {

    private static final String INT_VERSION_CODE = "VersionCode";

    private final Context context;
    private final UserPreferenceManager userPreferenceManager;

    public AppVersionManager(@NonNull Context context, @NonNull UserPreferenceManager userPreferenceManager) {
        this.context = Preconditions.checkNotNull(context);
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
    }

    public void onLaunch(@NonNull VersionUpgradedListener listener) {
        Preconditions.checkNotNull(listener);

        try {
            final int newVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            final int oldVersion = userPreferenceManager.getSharedPreferences().getInt(INT_VERSION_CODE, -1);
            if (newVersion > oldVersion) {
                userPreferenceManager.getSharedPreferences().edit().putInt(INT_VERSION_CODE, newVersion).apply();
                listener.onVersionUpgrade(oldVersion, newVersion);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.error(this, e);
        }
    }
}
