package co.smartreceipts.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import co.smartreceipts.android.R;
import co.smartreceipts.android.utils.log.Logger;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

@Deprecated
public class Preferences implements OnSharedPreferenceChangeListener {

    //Preference Identifiers - Global
    private static final String PREFERENCES_FILE_NAME = SharedPreferenceDefinitions.SmartReceipts_Preferences.toString();

    private static final String INT_VERSION_CODE = "VersionCode";

    // No Category
    private boolean mAutoBackupOnWifiOnly;

    // Misc (i.e. inaccessible preferences) for app use only
    private int mVersionCode;

    //Other Instance Variables
    private final Context mContext;

    public interface VersionUpgradeListener {
        public void onVersionUpgrade(int oldVersion, int newVersion);
    }

    private void initAutoBackupOnWifiOnly(SharedPreferences prefs) {
        this.mAutoBackupOnWifiOnly = prefs.getBoolean(mContext.getString(R.string.pref_no_category_auto_backup_wifi_only_key), mContext.getResources().getBoolean(R.bool.pref_no_category_auto_backup_wifi_only_defaultValue));
    }

    @VisibleForTesting
    public Preferences(Context context, Flex flex, StorageManager storageManager) {
        this.mContext = context;
        SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
        initAllPreferences(prefs);
    }

    private void initAllPreferences(SharedPreferences prefs) {
        Logger.debug(this, "Initializing Shared Preferences");

        // No Category
        this.initAutoBackupOnWifiOnly(prefs);

        // Misc (i.e. inaccessible preferences) for app use only
        this.mVersionCode = prefs.getInt(INT_VERSION_CODE, 78);
    }

    // This was added after version 78 (version 79 is the first "new" one)
    public void setVersionUpgradeListener(VersionUpgradeListener listener) {
        if (listener != null) {
            int newVersion = -1;
            try {
                newVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
                if (newVersion > this.mVersionCode) {
                    listener.onVersionUpgrade(mVersionCode, newVersion);
                    this.mVersionCode = newVersion;
                    SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(INT_VERSION_CODE, mVersionCode);
                    editor.apply();
                }
            } catch (NameNotFoundException e) {
                Logger.error(this, e);
            }
        } else {
            Logger.error(this, "A null VersionUpgradeListener was provided. Updates will not be registered");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (TextUtils.isEmpty(key)) {
            return; // Exit early if bad key
        }
        initAllPreferences(prefs);
    }

    public boolean getAutoBackupOnWifiOnly() {
        return mAutoBackupOnWifiOnly;
    }

}