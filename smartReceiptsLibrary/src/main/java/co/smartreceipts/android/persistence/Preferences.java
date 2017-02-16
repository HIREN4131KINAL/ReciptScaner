package co.smartreceipts.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.TypedValue;

import java.io.File;
import java.util.Currency;
import java.util.Locale;

import co.smartreceipts.android.R;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.utils.log.Logger;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

@Deprecated
public class Preferences implements OnSharedPreferenceChangeListener {

    //Preference Identifiers - Global
    public static final String SMART_PREFS = SharedPreferenceDefinitions.SmartReceipts_Preferences.toString();

    private static final String INT_VERSION_CODE = "VersionCode";

    // General Preferences
    private int mDefaultTripDuration;
    private String mDefaultCurrency, mDateSeparator;
    private boolean mIncludeCostCenter;

    // No Category
    private boolean mAutoBackupOnWifiOnly;

    // Misc (i.e. inaccessible preferences) for app use only
    private int mVersionCode;

    //Other Instance Variables
    private final Context mContext;
    private final Flex mFlex;
    private final StorageManager mStorageManager;


    public interface VersionUpgradeListener {
        public void onVersionUpgrade(int oldVersion, int newVersion);
    }

    private void initDefaultTripDuration(SharedPreferences prefs) {
        this.mDefaultTripDuration = prefs.getInt(mContext.getString(R.string.pref_general_trip_duration_key), mContext.getResources().getInteger(R.integer.pref_general_trip_duration_defaultValue));
    }

    private void initDefaultDateSeparator(SharedPreferences prefs) {
        final String localeDefault = DateUtils.getDateSeparator(mContext);
        this.mDateSeparator = prefs.getString(mContext.getString(R.string.pref_general_default_date_separator_key), localeDefault);
        if (TextUtils.isEmpty(mDateSeparator)) {
            mDateSeparator = localeDefault;
        }
    }

    private void initDefaultCurrency(SharedPreferences prefs) {
        try {
            final String assignedPreferenceCurrencyCode = mContext.getString(R.string.pref_general_default_currency_defaultValue);
            final String currencyCode;
            if (TextUtils.isEmpty(assignedPreferenceCurrencyCode)) {
                currencyCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
            } else {
                currencyCode = assignedPreferenceCurrencyCode;
            }
            mDefaultCurrency = prefs.getString(mContext.getString(R.string.pref_general_default_currency_key), currencyCode);
            if (TextUtils.isEmpty(mDefaultCurrency)) {
                this.mDefaultCurrency = currencyCode;
            }

        } catch (IllegalArgumentException ex) {
            this.mDefaultCurrency = "USD";
        }
    }

    private void initIncludeCostCenter(SharedPreferences prefs) {
        this.mIncludeCostCenter = prefs.getBoolean(mContext.getString(R.string.pref_general_track_cost_center_key), mContext.getResources().getBoolean(R.bool.pref_general_track_cost_center_defaultValue));
    }

    private void initAutoBackupOnWifiOnly(SharedPreferences prefs) {
        this.mAutoBackupOnWifiOnly = prefs.getBoolean(mContext.getString(R.string.pref_no_category_auto_backup_wifi_only_key), mContext.getResources().getBoolean(R.bool.pref_no_category_auto_backup_wifi_only_defaultValue));
    }

    @VisibleForTesting
    public Preferences(Context context, Flex flex, StorageManager storageManager) {
        this.mContext = context;
        this.mFlex = flex;
        this.mStorageManager = storageManager;
        SharedPreferences prefs = mContext.getSharedPreferences(SMART_PREFS, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
        initAllPreferences(prefs);
    }

    private void initAllPreferences(SharedPreferences prefs) {
        Logger.debug(this, "Initializing Shared Preferences");
        // General Preferences
        this.initDefaultTripDuration(prefs);
        this.initDefaultDateSeparator(prefs);
        this.initDefaultCurrency(prefs);
        this.initIncludeCostCenter(prefs);

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
                    SharedPreferences prefs = mContext.getSharedPreferences(SMART_PREFS, 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(INT_VERSION_CODE, mVersionCode);
                    editor.apply();
                }
            } catch (NameNotFoundException e) {
                Logger.error(this, e);
            } catch (RuntimeException e) {
                // Only intended for RoboE
                Logger.error(this, e);
            }
        } else {
            Logger.error(this, "A null VersionUpgradeListener was provided. Updates will not be registered");
        }
    }

    public SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(SMART_PREFS, 0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (TextUtils.isEmpty(key)) {
            return; // Exit early if bad key
        }
        initAllPreferences(prefs);
    }

    public String getDefaultCurreny() {
        return mDefaultCurrency;
    }

    public int getDefaultTripDuration() {
        return mDefaultTripDuration;
    }

    public String getDateSeparator() {
        return mDateSeparator;
    }

    public boolean getIncludeCostCenter() {
        return mIncludeCostCenter;
    }

    public boolean getAutoBackupOnWifiOnly() {
        return mAutoBackupOnWifiOnly;
    }

}