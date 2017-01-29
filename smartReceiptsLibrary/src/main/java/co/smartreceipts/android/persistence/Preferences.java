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

public class Preferences implements OnSharedPreferenceChangeListener {

    //Preference Identifiers - Global
    public static final String SMART_PREFS = SharedPreferenceDefinitions.SmartReceipts_Preferences.toString();

    private static final String INT_VERSION_CODE = "VersionCode";

    //Preference Folder
    private static final String PREFERENCES_FOLDER_NAME = ".prefs_file";

    // General Preferences
    private int mDefaultTripDuration;
    private String mDefaultCurrency, mDateSeparator;
    private boolean mIncludeCostCenter;

    // Receipt Preferences
    private float mMinReceiptPrice;
    private float mDefaultTaxPercentage;
    private boolean mPredictCategories, mEnableAutoCompleteSuggestions, mOnlyIncludeReimbursable, mReceiptsDefaultAsReimbursable, mDefaultToFirstReportDate,
            mMatchNameCats, mMatchCommentCats, mShowReceiptID, mIncludeTaxField, mUsePreTaxPrice, mDefaultToFullPage,
            mUsePaymentMethods;

    // Output Preferences
    private String mUserID;
    private File mSignaturePhoto;
    private boolean mIncludeCSVHeaders, mIncludeIDNotIndex, mIncludeCommentByReceiptPhoto, mOptimizePDFSpace, mShowSignature, mReceiptsLandscapeMode;

    // Email Preferences
    private String mEmailTo, mEmailCC, mEmailBCC, mEmailSubject;

    // Camera Preferences
    private boolean mUseNativeCamera, mCameraGrayScale, mRotateImages;

    // Layout Preferences
    private boolean mShowDate, mShowCategory, mShowPhotoPDFMarker;

    // Distance Preferences
    private float mDefaultDistanceRate;
    private boolean mShouldDistancePriceBeIncludedInReports, mPrintDistanceTable, mPrintDistanceAsDailyReceipt, mShowDistanceAsPriceInSubtotal;

    // Pro Preferences
    private String mPdfFooterText;

    // No Category
    private boolean mAutoBackupOnWifiOnly;

    // Misc (i.e. inaccessible preferences) for app use only
    private int mVersionCode;

    //Other Instance Variables
    private final Context mContext;
    private final Flex mFlex;
    private final StorageManager mStorageManager;
    private final File mPreferenceFolder;


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
        this.mUsePaymentMethods = prefs.getBoolean(mContext.getString(R.string.pref_receipt_use_payment_methods_key), mContext.getResources().getBoolean(R.bool.pref_receipt_use_payment_methods_defaultValue));
    }

    private void initUserID(SharedPreferences prefs) {
        this.mUserID = prefs.getString(mContext.getString(R.string.pref_output_username_key), "");
    }

    private void initReceiptsLandscapeMode(SharedPreferences prefs) {
        this.mReceiptsLandscapeMode = prefs.getBoolean(mContext.getString(R.string.pref_output_receipts_landscape_key), mContext.getResources().getBoolean(R.bool.pref_output_receipts_landscape_defaultValue));
    }

    private void initIncludeCSVHeaders(SharedPreferences prefs) {
        this.mIncludeCSVHeaders = prefs.getBoolean(mContext.getString(R.string.pref_output_csv_header_key), mContext.getResources().getBoolean(R.bool.pref_output_csv_header_defaultValue));
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
        this.mEmailTo = prefs.getString(mContext.getString(R.string.pref_email_default_email_to_key), mContext.getString(R.string.pref_email_default_email_to_defaultValue));
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

    private void initShowDistanceAsPriceInSubtotal(SharedPreferences prefs) {
        this.mShowDistanceAsPriceInSubtotal = prefs.getBoolean(mContext.getString(R.string.pref_distance_as_price_key), mContext.getResources().getBoolean(R.bool.pref_distance_as_price_defaultValue));
    }

    private void initPdfFooterText(SharedPreferences prefs) {
        this.mPdfFooterText = prefs.getString(mContext.getString(R.string.pref_pro_pdf_footer_key), mContext.getString(R.string.pref_pro_pdf_footer_defaultValue));
    }

    private void initAutoBackupOnWifiOnly(SharedPreferences prefs) {
        this.mAutoBackupOnWifiOnly = prefs.getBoolean(mContext.getString(R.string.pref_no_category_auto_backup_wifi_only_key), mContext.getResources().getBoolean(R.bool.pref_no_category_auto_backup_wifi_only_defaultValue));
    }

    @VisibleForTesting
    public Preferences(Context context, Flex flex, StorageManager storageManager) {
        this.mContext = context;
        this.mFlex = flex;
        this.mStorageManager = storageManager;
        mPreferenceFolder = this.mStorageManager.mkdir(PREFERENCES_FOLDER_NAME);
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
        this.initIncludeTaxField(prefs);
        this.initUsePreTaxPrice(prefs);
        this.initDefaultToFullPage(prefs);
        this.initUsePaymentMethods(prefs);

        // Output Preferences
        this.initUserID(prefs);
        this.initReceiptsLandscapeMode(prefs);
        this.initIncludeCSVHeaders(prefs);
        this.initIncludeReceiptIdNotIndex(prefs);
        this.initIncludeCommentByReceiptPhoto(prefs);
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
        this.initShowDistanceAsPriceInSubtotal(prefs);

        // Pro Preferences
        this.initPdfFooterText(prefs);

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

    public boolean useNativeCamera() {
        return mUseNativeCamera;
    }

    public void setUseNativeCamera(boolean useNativeCamera) {
        this.mUseNativeCamera = useNativeCamera;
        getSharedPreferences().edit().putBoolean(mContext.getString(R.string.pref_camera_use_native_camera_key), true).apply();
    }

    public boolean onlyIncludeReimbursableReceiptsInReports() {
        return mOnlyIncludeReimbursable;
    }

    public boolean doReceiptsDefaultAsReimbursable() {
        return mReceiptsDefaultAsReimbursable;
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

    public boolean isReceiptsTableLandscapeMode() {
        return mReceiptsLandscapeMode;
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

    public boolean getShowDistanceAsPriceInSubtotal() {
        return mShowDistanceAsPriceInSubtotal;
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

    public boolean getAutoBackupOnWifiOnly() {
        return mAutoBackupOnWifiOnly;
    }

}