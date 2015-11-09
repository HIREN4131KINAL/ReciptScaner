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

import java.io.File;
import java.util.Currency;
import java.util.Locale;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.utils.Utils;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

public class Preferences implements OnSharedPreferenceChangeListener {

    private static final String TAG = "Preferences";

    //Preference Identifiers - Global
    public static final String SMART_PREFS = SharedPreferenceDefinitions.SmartReceipts_Preferences.toString();
    private static final String INT_VERSION_CODE = "VersionCode";
    private static final String BOOL_ACTION_SEND_SHOW_HELP_DIALOG = "ShowHelpDialog";

    //Preference Folder
    private static final String PREFERENCES_FOLDER_NAME = ".prefs_file";

    // General Preferences
    private int mDefaultTripDuration;
    private String mDefaultCurrency, mDateSeparator;
    private boolean mIncludeCostCenter;

    // Receipt Preferences
    private float mMinReceiptPrice;
    private float mDefaultTaxPercentage;
    private boolean mPredictCategories, mEnableAutoCompleteSuggestions, mOnlyIncludeExpensable, mDefaultToFirstReportDate,
            mMatchNameCats, mMatchCommentCats, mShowReceiptID, mIncludeTaxField, mUsePreTaxPrice, mDefaultToFullPage,
            mUsePaymentMethods;

    // Output Preferences
    private String mUserID;
    private File mSignaturePhoto;
    private boolean mIncludeCSVHeaders, mUseFileExplorerForOutput, mIncludeIDNotIndex, mIncludeCommentByReceiptPhoto, mOptimizePDFSpace, mShowSignature;

    // Email Preferences
    private String mEmailTo, mEmailCC, mEmailBCC, mEmailSubject;

    // Camera Preferences
    private boolean mUseNativeCamera, mCameraGrayScale, mRotateImages;

    // Layout Preferences
    private boolean mShowDate, mShowCategory, mShowPhotoPDFMarker;

    // Distance Preferences
    private float mDefaultDistanceRate;
    private boolean mShouldDistancePriceBeIncludedInReports, mPrintDistanceTable, mPrintDistanceAsDailyReceipt;

    // Pro Preferences
    private String mPdfFooterText;

    // Misc (i.e. inaccessible preferences) for app use only
    private int mVersionCode;
    private boolean mShowActionSendHelpDialog;

    //Other Instance Variables
    private final Context mContext;
    private final Flex mFlex;
    private final StorageManager mStorageManager;
    private final File mPreferenceFolder;

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
        this.mIncludeTaxField = prefs.getBoolean(mContext.getString(R.string.pref_receipt_include_tax_field_key), mContext.getResources().getBoolean(R.bool.pref_receipt_include_tax_field_defaultValue));
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

    private void initIncludeCommentByReceiptPhoto(SharedPreferences prefs) {
        this.mIncludeCommentByReceiptPhoto = prefs.getBoolean(mContext.getString(R.string.pref_output_print_receipt_comment_by_photo_key), mContext.getResources().getBoolean(R.bool.pref_output_print_receipt_comment_by_photo_defaultValue));
    }

    private void initOptimizeSpaceForPDFOutput(SharedPreferences prefs) {
        this.mOptimizePDFSpace = prefs.getBoolean(mContext.getString(R.string.pref_output_optimize_space_key), true);
    }

    private void initShowBlankSignature(SharedPreferences prefs) {
        this.mShowSignature = prefs.getBoolean(mContext.getString(R.string.pref_output_blank_signature_key), mContext.getResources().getBoolean(R.bool.pref_output_blank_signature_defaultValue));
    }

    private void initSignaturePhoto(SharedPreferences prefs) {
        final String filename = prefs.getString(mContext.getString(R.string.pref_output_signature_picture_key), mContext.getString(R.string.pref_output_signature_picture_defaultValue));
        final File signatureFile = mStorageManager.getFile(getPreferencesFolder(), filename);
        if (!signatureFile.exists() || !signatureFile.isFile()) {
            mSignaturePhoto = null;
        } else {
            mSignaturePhoto = signatureFile;
        }
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

    private void initRotateImages(SharedPreferences prefs) {
        this.mRotateImages = prefs.getBoolean(mContext.getString(R.string.pref_camera_rotate_key), mContext.getResources().getBoolean(R.bool.pref_camera_rotate_key_defaultValue));
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
        this.mPrintDistanceTable = prefs.getBoolean(mContext.getString(R.string.pref_distance_print_table_key), mContext.getResources().getBoolean(R.bool.pref_distance_print_table_defaultValue));
    }

    private void initShouldDistancePriceBeIncludedInReports(SharedPreferences prefs) {
        this.mShouldDistancePriceBeIncludedInReports = prefs.getBoolean(mContext.getString(R.string.pref_distance_include_price_in_report_key), mContext.getResources().getBoolean(R.bool.pref_distance_include_price_in_report_defaultValue));
    }

    private void initPrintDistanceAsDailyReceipt(SharedPreferences prefs) {
        this.mPrintDistanceAsDailyReceipt = prefs.getBoolean(mContext.getString(R.string.pref_distance_print_daily_key), mContext.getResources().getBoolean(R.bool.pref_distance_print_daily_defaultValue));
    }

    private void initPdfFooterText(SharedPreferences prefs) {
        this.mPdfFooterText = prefs.getString(mContext.getString(R.string.pref_pro_pdf_footer_key), mContext.getString(R.string.pref_pro_pdf_footer_defaultValue));
    }

    Preferences(Context context, Flex flex, StorageManager storageManager) {
        this.mContext = context;
        this.mFlex = flex;
        this.mStorageManager = storageManager;
        mPreferenceFolder = this.mStorageManager.mkdir(PREFERENCES_FOLDER_NAME);
        SharedPreferences prefs = mContext.getSharedPreferences(SMART_PREFS, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
        initAllPreferences(prefs);
    }

    private void initAllPreferences(SharedPreferences prefs) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Initializing Shared Preferences");
        }
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
        this.initIncludeCommentByReceiptPhoto(prefs);
        this.initUseFileExplorerForOutput(prefs);
        this.initOptimizeSpaceForPDFOutput(prefs);
        this.initShowBlankSignature(prefs);
        this.initSignaturePhoto(prefs);

        // Email Preferences
        this.initEmailTo(prefs);
        this.initEmailCC(prefs);
        this.initEmailBCC(prefs);
        this.initEmailSubject(prefs);

        // Camera Preferences
        this.initUseNativeCamera(prefs);
        this.initCameraGrayScale(prefs);
        this.initRotateImages(prefs);

        // Layout Preferences
        this.initShowDate(prefs);
        this.initShowCategory(prefs);
        this.initShowPhotoPDFMarker(prefs);

        // Distance Preferences
        this.initShouldDistancePriceBeIncludedInReports(prefs);
        this.initDefaultMileageRate(prefs);
        this.initPrintDistanceTable(prefs);
        this.initPrintDistanceAsDailyReceipt(prefs);

        // Pro Preferences
        this.initPdfFooterText(prefs);

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
                    editor.apply();
                }
            } catch (NameNotFoundException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString());
                }
            } catch (RuntimeException e) {
                // Only intended for RoboE
                Log.e(TAG, e.toString());
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

    public boolean matchCommentToCategory() {
        return mMatchCommentCats;
    }

    public boolean matchNameToCategory() {
        return mMatchNameCats;
    }

    public boolean useNativeCamera() {
        return mUseNativeCamera;
    }

    public void setUseNativeCamera(boolean useNativeCamera) {
        this.mUseNativeCamera = useNativeCamera;
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(mContext.getString(R.string.pref_camera_use_native_camera_key), true).apply();
    }

    public boolean onlyIncludeExpensableReceiptsInReports() {
        return mOnlyIncludeExpensable;
    }

    public boolean includeTaxField() {
        return mIncludeTaxField;
    }

    public boolean usePreTaxPrice() {
        return this.mUsePreTaxPrice;
    }

    public boolean shouldDefaultToFullPage() {
        return this.mDefaultToFullPage;
    }

    public boolean enableAutoCompleteSuggestions() {
        return mEnableAutoCompleteSuggestions;
    }

    public String getEmailTo() {
        return mEmailTo;
    }

    public String getDefaultCurreny() {
        return mDefaultCurrency;
    }

    public String getUserID() {
        return mUserID;
    }

    public int getDefaultTripDuration() {
        return mDefaultTripDuration;
    }

    public float getMinimumReceiptPriceToIncludeInReports() {
        return mMinReceiptPrice;
    }

    public boolean includeReceiptIdInsteadOfIndexByPhoto() {
        return this.mIncludeIDNotIndex;
    }

    public boolean getIncludeCommentByReceiptPhoto() { return this.mIncludeCommentByReceiptPhoto; }

    public boolean isBlankSignatureShownForPdfs() {
        return this.mShowSignature;
    }

    /**
     * @return - the {@link java.io.File} containing the signature file only if a photo has been set of exists. {@code null} will be returned otherwise.s
     */
    public File getSignaturePhoto() {
        return this.mSignaturePhoto;
    }

    public boolean defaultToFirstReportDate() {
        return mDefaultToFirstReportDate;
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

    public boolean getRotateImages() {
        return mRotateImages;
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

    public String getDateSeparator() {
        return mDateSeparator;
    }

    public boolean getUsesPreTaxPrice() {
        return this.mUsePreTaxPrice;
    }

    public boolean getUsesFileExporerForOutputIntent() {
        return this.mUseFileExplorerForOutput;
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

    public boolean getPrintDistanceAsDailyReceipt() {
        return mPrintDistanceAsDailyReceipt;
    }

    public boolean getIncludeCostCenter() {
        return mIncludeCostCenter;
    }

    /**
     * @return - a folder in which preference files (e.g. images) can be stored
     */
    public File getPreferencesFolder() {
        return mPreferenceFolder;
    }

    public boolean getShouldTheDistancePriceBeIncludedInReports() {
        return mShouldDistancePriceBeIncludedInReports;
    }

    public String getPdfFooterText() {
        return mPdfFooterText;
    }

}