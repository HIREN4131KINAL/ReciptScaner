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

    // Receipt Preferences
    private float mMinReceiptPrice;
    private float mDefaultTaxPercentage;
    private boolean mPredictCategories, mEnableAutoCompleteSuggestions, mOnlyIncludeReimbursable, mReceiptsDefaultAsReimbursable, mDefaultToFirstReportDate,
            mMatchNameCats, mMatchCommentCats, mShowReceiptID, mUsePreTaxPrice, mDefaultToFullPage,
            mUsePaymentMethods;

    // Distance Preferences
    private float mDefaultDistanceRate;
    private boolean mShouldDistancePriceBeIncludedInReports, mPrintDistanceTable, mShowDistanceAsPriceInSubtotal;

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

    private void initMinReceiptPrice(SharedPreferences prefs) {
        this.mMinReceiptPrice = prefs.getFloat(mContext.getString(R.string.pref_receipt_minimum_receipts_price_key), -Float.MAX_VALUE);
    }

    private void initDefaultTaxPercentage(SharedPreferences prefs) {
        this.mDefaultTaxPercentage = prefs.getFloat(mContext.getString(R.string.pref_receipt_tax_percent_key), 0f);
    }

    private void initPredictCategories(SharedPreferences prefs) {
        this.mPredictCategories = prefs.getBoolean(mContext.getString(R.string.pref_receipt_predict_categories_key), true);
    }

    private void initEnableAutoCompleteSuggestions(SharedPreferences prefs) {
        this.mEnableAutoCompleteSuggestions = prefs.getBoolean(mContext.getString(R.string.pref_receipt_enable_autocomplete_key), true);
    }

    private void initOnlyIncludeReimbursable(SharedPreferences prefs) {
        this.mOnlyIncludeReimbursable = prefs.getBoolean(mContext.getString(R.string.pref_receipt_reimbursable_only_key), false);
    }

    private void initReceiptsDefaultAsReimbursable(SharedPreferences prefs) {
        this.mReceiptsDefaultAsReimbursable = prefs.getBoolean(mContext.getString(R.string.pref_receipt_reimbursable_default_key), mContext.getResources().getBoolean(R.bool.pref_receipt_reimbursable_default_defaultValue));
    }

    private void initDefaultToFirstReportDate(SharedPreferences prefs) {
        this.mDefaultToFirstReportDate = prefs.getBoolean(mContext.getString(R.string.pref_receipt_default_to_report_start_date_key), false);
    }

    private void initMatchNameCats(SharedPreferences prefs) {
        this.mMatchNameCats = prefs.getBoolean(mContext.getString(R.string.pref_receipt_match_name_to_category_key), false);
    }

    private void initMatchCommentCats(SharedPreferences prefs) {
        this.mMatchCommentCats = prefs.getBoolean(mContext.getString(R.string.pref_receipt_match_comment_to_category_key), false);
    }

    private void initShowReceiptID(SharedPreferences prefs) {
        this.mShowReceiptID = prefs.getBoolean(mContext.getString(R.string.pref_receipt_show_id_key), false);
    }

    private void initUsePreTaxPrice(SharedPreferences prefs) {
        this.mUsePreTaxPrice = prefs.getBoolean(mContext.getString(R.string.pref_receipt_pre_tax_key), true);
    }

    private void initDefaultToFullPage(SharedPreferences prefs) {
        this.mDefaultToFullPage = prefs.getBoolean(mContext.getString(R.string.pref_receipt_full_page_key), false);
    }

    private void initUsePaymentMethods(SharedPreferences prefs) {
        this.mUsePaymentMethods = prefs.getBoolean(mContext.getString(R.string.pref_receipt_use_payment_methods_key), mContext.getResources().getBoolean(R.bool.pref_receipt_use_payment_methods_defaultValue));
    }

    private void initDefaultMileageRate(SharedPreferences prefs) {
        final TypedValue typedValue = new TypedValue();
        mContext.getResources().getValue(R.dimen.pref_distance_rate_defaultValue, typedValue, true);
        this.mDefaultDistanceRate = prefs.getFloat(mContext.getString(R.string.pref_distance_rate_key), typedValue.getFloat());
    }

    private void initPrintDistanceTable(SharedPreferences prefs) {
        this.mPrintDistanceTable = prefs.getBoolean(mContext.getString(R.string.pref_distance_print_table_key), mContext.getResources().getBoolean(R.bool.pref_distance_print_table_defaultValue));
    }

    private void initShouldDistancePriceBeIncludedInReports(SharedPreferences prefs) {
        this.mShouldDistancePriceBeIncludedInReports = prefs.getBoolean(mContext.getString(R.string.pref_distance_include_price_in_report_key), mContext.getResources().getBoolean(R.bool.pref_distance_include_price_in_report_defaultValue));
    }

    private void initShowDistanceAsPriceInSubtotal(SharedPreferences prefs) {
        this.mShowDistanceAsPriceInSubtotal = prefs.getBoolean(mContext.getString(R.string.pref_distance_as_price_key), mContext.getResources().getBoolean(R.bool.pref_distance_as_price_defaultValue));
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

        // Receipt Preferences
        this.initMinReceiptPrice(prefs);
        this.initDefaultTaxPercentage(prefs);
        this.initPredictCategories(prefs);
        this.initEnableAutoCompleteSuggestions(prefs);
        this.initOnlyIncludeReimbursable(prefs);
        this.initReceiptsDefaultAsReimbursable(prefs);
        this.initDefaultToFirstReportDate(prefs);
        this.initMatchNameCats(prefs);
        this.initMatchCommentCats(prefs);
        this.initShowReceiptID(prefs);
        this.initUsePreTaxPrice(prefs);
        this.initDefaultToFullPage(prefs);
        this.initUsePaymentMethods(prefs);

        // Distance Preferences
        this.initShouldDistancePriceBeIncludedInReports(prefs);
        this.initDefaultMileageRate(prefs);
        this.initPrintDistanceTable(prefs);
        this.initShowDistanceAsPriceInSubtotal(prefs);

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

    public boolean predictCategories() {
        return mPredictCategories;
    }

    public boolean matchCommentToCategory() {
        return mMatchCommentCats;
    }

    public boolean matchNameToCategory() {
        return mMatchNameCats;
    }

    public boolean onlyIncludeReimbursableReceiptsInReports() {
        return mOnlyIncludeReimbursable;
    }

    public boolean doReceiptsDefaultAsReimbursable() {
        return mReceiptsDefaultAsReimbursable;
    }

    public boolean shouldDefaultToFullPage() {
        return this.mDefaultToFullPage;
    }

    public boolean enableAutoCompleteSuggestions() {
        return mEnableAutoCompleteSuggestions;
    }

    public String getDefaultCurreny() {
        return mDefaultCurrency;
    }

    public int getDefaultTripDuration() {
        return mDefaultTripDuration;
    }

    public float getMinimumReceiptPriceToIncludeInReports() {
        return mMinReceiptPrice;
    }

    public boolean defaultToFirstReportDate() {
        return mDefaultToFirstReportDate;
    }

    public boolean isShowReceiptID() {
        return mShowReceiptID;
    }

    public float getDefaultTaxPercentage() {
        return mDefaultTaxPercentage;
    }

    public boolean getUsesPaymentMethods() {
        return this.mUsePaymentMethods;
    }

    public String getDateSeparator() {
        return mDateSeparator;
    }

    public boolean getUsesPreTaxPrice() {
        return this.mUsePreTaxPrice;
    }

    public boolean hasDefaultDistanceRate() {
        return this.mDefaultDistanceRate > 0;
    }

    public float getDefaultDistanceRate() {
        return this.mDefaultDistanceRate;
    }

    public boolean getPrintDistanceTable() {
        return mPrintDistanceTable;
    }

    public boolean getShowDistanceAsPriceInSubtotal() {
        return mShowDistanceAsPriceInSubtotal;
    }

    public boolean getIncludeCostCenter() {
        return mIncludeCostCenter;
    }

    /**
     * @return - a folder in which preference files (e.g. images) can be stored
     */
    public boolean getShouldTheDistancePriceBeIncludedInReports() {
        return mShouldDistancePriceBeIncludedInReports;
    }

    public boolean getAutoBackupOnWifiOnly() {
        return mAutoBackupOnWifiOnly;
    }

}