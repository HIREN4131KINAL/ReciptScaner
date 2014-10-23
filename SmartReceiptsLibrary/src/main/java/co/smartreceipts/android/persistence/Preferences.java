package co.smartreceipts.android.persistence;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import java.util.Currency;
import java.util.Locale;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.utils.Utils;
import wb.android.flex.Flex;

public class Preferences implements OnSharedPreferenceChangeListener {

    private static final String TAG = "Preferences";

    //Preference Identifiers - Global
    public static final String SMART_PREFS = SharedPreferenceDefinitions.SmartReceipts_Preferences.toString();
    private static final String INT_VERSION_CODE = "VersionCode";
    private static final String BOOL_ACTION_SEND_SHOW_HELP_DIALOG = "ShowHelpDialog";

    // General Preferences
    private int mDefaultTripDuration;
    private String mDefaultCurrency, mDateSeparator;

    // Receipt Preferences
    private float mMinReceiptPrice;
    private float mDefaultTaxPercentage;
    private boolean mPredictCategories, mEnableAutoCompleteSuggestions, mOnlyIncludeExpensable, mDefaultToFirstReportDate,
            mMatchNameCats, mMatchCommentCats, mShowReceiptID, mIncludeTaxField, mUsePreTaxPrice, mDefaultToFullPage,
            mUsePaymentMethods;

    // Output Preferences
    private String mUserID;
    private boolean mIncludeCSVHeaders, mUseFileExplorerForOutput, mIncludeIDNotIndex, mOptimizePDFSpace;

    // Email Preferences
    private String mEmailTo, mEmailCC, mEmailBCC, mEmailSubject;

    // Camera Preferences
    private boolean mUseNativeCamera, mCameraGrayScale;

    // Layout Preferences
    private boolean mShowDate, mShowCategory, mShowPhotoPDFMarker;

    // Distance Preferences
    private float mDefaultDistanceRate;
    private boolean mPrintUniqueTable, mPrintDistanceAsDailyReceipt;

    // Misc (i.e. inaccessible preferences) for app use only
    private int mVersionCode;
    private boolean mShowActionSendHelpDialog;

    //Other Instance Variables
    private final Context mContext;
    private final Flex mFlex;

    public interface VersionUpgradeListener {
        public void onVersionUpgrade(int oldVersion, int newVersion);
    }

    private void initDefaultTripDuration(SharedPreferences prefs) {
        this.mDefaultTripDuration = prefs.getInt(mContext.getString(R.string.pref_general_trip_duration_key), 3);
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
            final String localeDefault = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
            mDefaultCurrency = prefs.getString(mContext.getString(R.string.pref_general_default_currency_key), localeDefault);
            if (TextUtils.isEmpty(mDefaultCurrency)) {
                this.mDefaultCurrency = localeDefault;
            }
        } catch (IllegalArgumentException ex) {
            this.mDefaultCurrency = "USD";
        }
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

    private void initOnlyIncludeExpensable(SharedPreferences prefs) {
        this.mOnlyIncludeExpensable = prefs.getBoolean(mContext.getString(R.string.pref_receipt_expensable_only_key), false);
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

    private void initIncludeTaxField(SharedPreferences prefs) {
        this.mIncludeTaxField = prefs.getBoolean(mContext.getString(R.string.pref_receipt_include_tax_field_key), false);
    }

    private void initUsePreTaxPrice(SharedPreferences prefs) {
        this.mUsePreTaxPrice = prefs.getBoolean(mContext.getString(R.string.pref_receipt_pre_tax_key), true);
    }

    private void initDefaultToFullPage(SharedPreferences prefs) {
        this.mDefaultToFullPage = prefs.getBoolean(mContext.getString(R.string.pref_receipt_full_page_key), false);
    }

    private void initUsePaymentMethods(SharedPreferences prefs) {
        this.mUsePaymentMethods = prefs.getBoolean(mContext.getString(R.string.pref_receipt_use_payment_methods_key), false);
    }

    private void initUserID(SharedPreferences prefs) {
        this.mUserID = prefs.getString(mContext.getString(R.string.pref_output_username_key), "");
    }

    private void initIncludeCSVHeaders(SharedPreferences prefs) {
        this.mIncludeCSVHeaders = prefs.getBoolean(mContext.getString(R.string.pref_output_csv_header_key), false);
    }

    private void initUseFileExplorerForOutput(SharedPreferences prefs) {
        this.mUseFileExplorerForOutput = prefs.getBoolean(mContext.getString(R.string.pref_output_launch_file_explorer_key), false);
    }

    private void initIncludeReceiptIdNotIndex(SharedPreferences prefs) {
        this.mIncludeIDNotIndex = prefs.getBoolean(mContext.getString(R.string.pref_output_print_receipt_id_by_photo_key), false);
    }

    private void initOptimizeSpaceForPDFOutput(SharedPreferences prefs) {
        this.mOptimizePDFSpace = prefs.getBoolean(mContext.getString(R.string.pref_output_optimize_space_key), true);
    }

    private void initEmailTo(SharedPreferences prefs) {
        this.mEmailTo = prefs.getString(mContext.getString(R.string.pref_email_default_email_to_key), "");
    }

    private void initEmailCC(SharedPreferences prefs) {
        this.mEmailCC = prefs.getString(mContext.getString(R.string.pref_email_default_email_cc_key), "");
    }

    private void initEmailBCC(SharedPreferences prefs) {
        this.mEmailBCC = prefs.getString(mContext.getString(R.string.pref_email_default_email_bcc_key), "");
    }

    private void initEmailSubject(SharedPreferences prefs) {
        this.mEmailSubject = prefs.getString(mContext.getString(R.string.pref_email_default_email_subject_key), mFlex.getString(mContext, R.string.EMAIL_DATA_SUBJECT));
    }

    private void initUseNativeCamera(SharedPreferences prefs) {
        this.mUseNativeCamera = prefs.getBoolean(mContext.getString(R.string.pref_camera_use_native_camera_key), false);
    }

    private void initCameraGrayScale(SharedPreferences prefs) {
        this.mCameraGrayScale = prefs.getBoolean(mContext.getString(R.string.pref_camera_bw_key), false);
    }

    private void initShowDate(SharedPreferences prefs) {
        this.mShowDate = prefs.getBoolean(mContext.getString(R.string.pref_layout_display_date_key), true);
    }

    private void initShowCategory(SharedPreferences prefs) {
        this.mShowCategory = prefs.getBoolean(mContext.getString(R.string.pref_layout_display_category_key), false);
    }

    private void initShowPhotoPDFMarker(SharedPreferences prefs) {
        this.mShowPhotoPDFMarker = prefs.getBoolean(mContext.getString(R.string.pref_layout_display_photo_key), false);
    }

    private void initDefaultMileageRate(SharedPreferences prefs) {
        final TypedValue typedValue = new TypedValue();
        mContext.getResources().getValue(R.dimen.pref_distance_rate_defaultValue, typedValue, true);
        this.mDefaultDistanceRate = prefs.getFloat(mContext.getString(R.string.pref_distance_rate_key), typedValue.getFloat());
    }

    private void initPrintDistanceTable(SharedPreferences prefs) {
        this.mPrintUniqueTable = prefs.getBoolean(mContext.getString(R.string.pref_distance_print_table_key), mContext.getResources().getBoolean(R.bool.pref_distance_print_table_defaultValue));
    }

    private void initPrintDistanceAsDailyReceipt(SharedPreferences prefs) {
        this.mPrintDistanceAsDailyReceipt = prefs.getBoolean(mContext.getString(R.string.pref_distance_print_daily_key), mContext.getResources().getBoolean(R.bool.pref_distance_print_daily_defaultValue));
    }

    Preferences(Context context, Flex flex) {
        this.mContext = context;
        this.mFlex = flex;
        SharedPreferences prefs = mContext.getSharedPreferences(SMART_PREFS, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
        initAllPreferences(prefs);
    }

    private Preferences(Context context, Flex flex, SharedPreferences preferences) {
        this.mContext = context;
        this.mFlex = flex;
        preferences.registerOnSharedPreferenceChangeListener(this);
        initAllPreferences(preferences);
    }

    /**
     * Only call this from RoboElectric for testing purposes. Do not use in general
     * production
     *
     * @param context
     * @return
     */
    public static final Preferences getRoboElectricInstance(Context context, Flex flex) {
        return new Preferences(context, flex, PreferenceManager.getDefaultSharedPreferences(context));
    }


    private void initAllPreferences(SharedPreferences prefs) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Initializing Shared Preferences");
        }
        // General Preferences
        this.initDefaultTripDuration(prefs);
        this.initDefaultDateSeparator(prefs);
        this.initDefaultCurrency(prefs);

        // Receipt Preferences
        this.initMinReceiptPrice(prefs);
        this.initDefaultTaxPercentage(prefs);
        this.initPredictCategories(prefs);
        this.initEnableAutoCompleteSuggestions(prefs);
        this.initOnlyIncludeExpensable(prefs);
        this.initDefaultToFirstReportDate(prefs);
        this.initMatchNameCats(prefs);
        this.initMatchCommentCats(prefs);
        this.initShowReceiptID(prefs);
        this.initIncludeTaxField(prefs);
        this.initUsePreTaxPrice(prefs);
        this.initDefaultToFullPage(prefs);
        this.initUsePaymentMethods(prefs);

        // Output Preferences
        this.initUserID(prefs);
        this.initIncludeCSVHeaders(prefs);
        this.initIncludeReceiptIdNotIndex(prefs);
        this.initUseFileExplorerForOutput(prefs);
        this.initOptimizeSpaceForPDFOutput(prefs);

        // Email Preferences
        this.initEmailTo(prefs);
        this.initEmailCC(prefs);
        this.initEmailBCC(prefs);
        this.initEmailSubject(prefs);

        // Camera Preferences
        this.initUseNativeCamera(prefs);
        this.initCameraGrayScale(prefs);

        // Layout Preferences
        this.initShowDate(prefs);
        this.initShowCategory(prefs);
        this.initShowPhotoPDFMarker(prefs);

        // Distance Preferences
        this.initDefaultMileageRate(prefs);
        this.initPrintDistanceTable(prefs);
        this.initPrintDistanceAsDailyReceipt(prefs);

        // Misc (i.e. inaccessible preferences) for app use only
        this.mShowActionSendHelpDialog = prefs.getBoolean(BOOL_ACTION_SEND_SHOW_HELP_DIALOG, true);
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
                    editor.commit();
                }
            } catch (NameNotFoundException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString());
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "A null VersionUpgradeListener was provided. Updates will not be registered");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void commit() {
        /**
         * *********************************
         * Don't remove everything here! Still need to keep internal app commit settings (i.e. versionCode, don't showHelp)
         * **********
         */
        SharedPreferences prefs = mContext.getSharedPreferences(SMART_PREFS, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(BOOL_ACTION_SEND_SHOW_HELP_DIALOG, mShowActionSendHelpDialog);
        /*
        editor.putInt(INT_DEFAULT_TRIP_DURATION, mDefaultTripDuration);
        editor.putFloat(FLOAT_MIN_RECEIPT_PRICE, mMinReceiptPrice);
        editor.putString(STRING_DEFAULT_EMAIL_TO, mEmailTo);
        editor.putString(STRING_CURRENCY, mDefaultCurrency);
        editor.putBoolean(BOOL_PREDICT_CATEGORIES, mPredictCategories);
        editor.putBoolean(BOOL_USE_NATIVE_CAMERA, mUseNativeCamera);
        editor.putBoolean(BOOL_MATCH_NAME_WITH_CATEGORIES, mMatchNameCats);
        editor.putBoolean(BOOL_MATCH_COMMENT_WITH_CATEGORIES, mMatchCommentCats);
        editor.putBoolean(BOOL_ONLY_INCLUDE_EXPENSABLE_ITEMS, mOnlyIncludeExpensable);
        editor.putBoolean(BOOL_INCLUDE_TAX_FIELD, mIncludeTaxField);
        editor.putBoolean(BOOL_ENABLE_AUTOCOMPLETE_SUGGESTIONS, mEnableAutoCompleteSuggestions);
        editor.putString(STRING_USERNAME, mUserID);
        editor.putString(STRING_DATE_SEPARATOR, mDateSeparator);
        editor.putBoolean(BOOL_DEFAULT_TO_FIRST_TRIP_DATE, mDefaultToFirstReportDate);*/
        if (Utils.ApiHelper.hasGingerbread()) {
            editor.apply();
        } else {
            editor.commit();
        }
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

    public void setPredictCategories(boolean predictCategories) {
        this.mPredictCategories = predictCategories;
    }

    public boolean matchCommentToCategory() {
        return mMatchCommentCats;
    }

    public void setMatchCommentToCategory(boolean matchCommentCats) {
        this.mMatchCommentCats = matchCommentCats;
    }

    public boolean matchNameToCategory() {
        return mMatchNameCats;
    }

    public void setMatchNameToCategory(boolean matchNameCats) {
        this.mMatchNameCats = matchNameCats;
    }

    public boolean useNativeCamera() {
        return mUseNativeCamera;
    }

    public void setUseNativeCamera(boolean useNativeCamera) {
        this.mUseNativeCamera = useNativeCamera;
    }

    public boolean onlyIncludeExpensableReceiptsInReports() {
        return mOnlyIncludeExpensable;
    }

    public void setOnlyIncludeExpensableReceiptsInReports(boolean onlyIncludeExpensable) {
        this.mOnlyIncludeExpensable = onlyIncludeExpensable;
    }

    public boolean includeTaxField() {
        return mIncludeTaxField;
    }

    public void setIncludeTaxField(boolean includeTaxField) {
        this.mIncludeTaxField = includeTaxField;
    }

    public boolean usePreTaxPrice() {
        return this.mUsePreTaxPrice;
    }

    public boolean shouldDefaultToFullPage() {
        return this.mDefaultToFullPage;
    }

    public void setDateSeparator(String dateSeparator) {
        this.mDateSeparator = dateSeparator;
    }

    public boolean enableAutoCompleteSuggestions() {
        return mEnableAutoCompleteSuggestions;
    }

    public void setEnableAutoCompleteSuggestions(boolean enableAutoCompleteSuggestions) {
        this.mEnableAutoCompleteSuggestions = enableAutoCompleteSuggestions;
    }

    public String getEmailTo() {
        return mEmailTo;
    }

    public void setDefaultEmailReceipient(String emailTo) {
        this.mEmailTo = emailTo;
    }

    public String getDefaultCurreny() {
        return mDefaultCurrency;
    }

    public void setDefaultCurreny(String currency) {
        this.mDefaultCurrency = currency;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setUserID(String userID) {
        this.mUserID = userID;
    }

    public int getDefaultTripDuration() {
        return mDefaultTripDuration;
    }

    public void setDefaultTripDuration(int defaultTripDuration) {
        this.mDefaultTripDuration = defaultTripDuration;
    }

    public float getMinimumReceiptPriceToIncludeInReports() {
        return mMinReceiptPrice;
    }

    public void setMinimumReceiptPriceToIncludeInReports(float minReceiptPrice) {
        this.mMinReceiptPrice = minReceiptPrice;
    }

    public boolean includeReceiptIdInsteadOfIndexByPhoto() {
        return this.mIncludeIDNotIndex;
    }

    public boolean defaultToFirstReportDate() {
        return mDefaultToFirstReportDate;
    }

    public void setDefaultToFirstReportDate(boolean defaultToFirstReportDate) {
        this.mDefaultToFirstReportDate = defaultToFirstReportDate;
    }

    public boolean showActionSendHelpDialog() {
        return mShowActionSendHelpDialog;
    }

    public void setShowActionSendHelpDialog(boolean showActionSendHelpDialog) {
        this.mShowActionSendHelpDialog = showActionSendHelpDialog;
    }

    public boolean includeCSVHeaders() {
        return mIncludeCSVHeaders;
    }

    public boolean isShowReceiptID() {
        return mShowReceiptID;
    }

    public float getDefaultTaxPercentage() {
        return mDefaultTaxPercentage;
    }

    public String getEmailCC() {
        return mEmailCC;
    }

    public String getEmailBCC() {
        return mEmailBCC;
    }

    public String getEmailSubject() {
        return mEmailSubject;
    }

    public boolean isCameraGrayScale() {
        return mCameraGrayScale;
    }

    public boolean isShowDate() {
        return mShowDate;
    }

    public boolean isShowCategory() {
        return mShowCategory;
    }

    public boolean isShowPhotoPDFMarker() {
        return mShowPhotoPDFMarker;
    }

    public boolean getUsesPaymentMethods() {
        return this.mUsePaymentMethods;
    }

    public boolean showAds() {
        return false;
    }

    public void setIncludeCSVHeaders(boolean includeCSVHeaders) {
        /*
		this.mIncludeCSVHeaders = includeCSVHeaders;
		SharedPreferences prefs = mContext.getSharedPreferences(SMART_PREFS, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(BOOL_INCL_CSV_HEADERS, this.mIncludeCSVHeaders);
        editor.commit();
        */
    }

    public String getDateSeparator() {
        return mDateSeparator;
    }

    public boolean getUsesPreTaxPrice() {
        return this.mUsePreTaxPrice;
    }

    public boolean getUsesFileExporerForOutputIntent() {
        return this.mUseFileExplorerForOutput;
    }

    public float getDefaultDistanceRate() {
        return this.mDefaultDistanceRate;
    }

    public boolean getPrintUniqueTable() {
        return mPrintUniqueTable;
    }

    public boolean getPrintDistanceAsDailyReceipt() {
        return mPrintDistanceAsDailyReceipt;
    }


}