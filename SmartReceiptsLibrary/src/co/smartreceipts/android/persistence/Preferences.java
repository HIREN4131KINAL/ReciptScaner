package co.smartreceipts.android.persistence;

import java.util.Currency;
import java.util.Locale;

import wb.android.flex.Flex;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.utils.Utils;

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
    				mMatchNameCats, mMatchCommentCats, mShowReceiptID, mIncludeTaxField, mUsePreTaxPrice;

    // Output Preferences
    private String mUserID;
    private boolean mIncludeCSVHeaders, mUseFileExplorerForOutput;

    // Email Preferences
    private String mEmailTo, mEmailCC, mEmailBCC, mEmailSubject;

    // Camera Preferences
    private boolean mUseNativeCamera, mCameraGrayScale;

    // Layout Preferences
    private boolean mShowDate, mShowCategory, mShowPhotoPDFMarker;

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

	private void initUserID(SharedPreferences prefs) {
		this.mUserID = prefs.getString(mContext.getString(R.string.pref_output_username_key), "");
	}

	private void initIncludeCSVHeaders(SharedPreferences prefs) {
		this.mIncludeCSVHeaders = prefs.getBoolean(mContext.getString(R.string.pref_output_csv_header_key), false);
	}
	
	private void initUseFileExplorerForOutput(SharedPreferences prefs) {
		this.mUseFileExplorerForOutput = prefs.getBoolean(mContext.getString(R.string.pref_output_launch_file_explorer_key), false);
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
     * @param context
     * @return
     */
    public static final Preferences getRoboElectricInstance(Context context, Flex flex) {
    	return new Preferences(context, flex, PreferenceManager.getDefaultSharedPreferences(context));
    }


    private void initAllPreferences(SharedPreferences prefs) {
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

		// Output Preferences
		this.initUserID(prefs);
		this.initIncludeCSVHeaders(prefs);

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
        	}
        	catch (NameNotFoundException e) {
        		if (BuildConfig.DEBUG) {
					Log.e(TAG, e.toString());
				}
        	}
    	}
    	else {
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

		/*
		 * In practice, I should just init the one that changed...
		 * But this way is easier so whatever
		 */

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

		// Output Preferences
		this.initUserID(prefs);
		this.initIncludeCSVHeaders(prefs);

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
	}

	/*
	@Override
	public boolean onPreferenceChange(Preference preference, Object value) {
		final String key = preference.getKey();


		////////////////////////
		// General Preferences
		////////////////////////
		if (key.equals(mContext.getString(R.string.pref_general_trip_duration_key))) {
			if (value instanceof Integer) {
				this.mDefaultTripDuration = (Integer) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_general_default_date_separator_key))) {
			if (value instanceof String) {
				this.mDateSeparator = (String) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_general_default_currency_key))) {
			if (value instanceof String) {
				this.mDefaultCurrency = (String) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		////////////////////////
		// Receipt Preferences
		////////////////////////
		else if (key.equals(mContext.getString(R.string.pref_receipt_minimum_receipts_price_key))) {
			if (value instanceof Float) {
				this.mMinReceiptPrice = (Float) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_receipt_include_tax_percent_key))) {
			if (value instanceof Integer) {
				this.mDefaultTaxPercentage = (Integer) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_receipt_predict_categories_key))) {
			if (value instanceof Boolean) {
				this.mPredictCategories = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_receipt_enable_autocomplete_key))) {
			if (value instanceof Boolean) {
				this.mEnableAutoCompleteSuggestions = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_receipt_expensable_only_key))) {
			if (value instanceof Boolean) {
				this.mOnlyIncludeExpensable = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_receipt_default_to_report_start_date_key))) {
			if (value instanceof Boolean) {
				this.mDefaultToFirstReportDate = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_receipt_match_name_to_category_key))) {
			if (value instanceof Boolean) {
				this.mMatchNameCats = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_receipt_match_comment_to_category_key))) {
			if (value instanceof Boolean) {
				this.mMatchCommentCats = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_receipt_show_id_key))) {
			if (value instanceof Boolean) {
				this.mShowReceiptID = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_receipt_include_tax_field_key))) {
			if (value instanceof Boolean) {
				this.mIncludeTaxField = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		// Output Preferences
		else if (key.equals(mContext.getString(R.string.pref_output_username_key))) {
			if (value instanceof String) {
				this.mUserID = (String) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_output_csv_header_key))) {
			if (value instanceof Boolean) {
				this.mIncludeCSVHeaders = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		////////////////////////
		// Email Preferences
		////////////////////////
		else if (key.equals(mContext.getString(R.string.pref_email_default_email_to_key))) {
			if (value instanceof String) {
				this.mEmailTo = (String) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_email_default_email_cc_key))) {
			if (value instanceof String) {
				this.mEmailCC = (String) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_email_default_email_bcc_key))) {
			if (value instanceof String) {
				this.mEmailBCC = (String) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_email_default_email_subject_key))) {
			if (value instanceof String) {
				this.mEmailSubject = (String) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		////////////////////////
		// Camera Preferences
		////////////////////////
		else if (key.equals(mContext.getString(R.string.pref_camera_use_native_camera_key))) {
			if (value instanceof Boolean) {
				this.mUseNativeCamera = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_camera_bw_key))) {
			if (value instanceof Boolean) {
				this.mCameraGrayScale = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		////////////////////////
		// Layout Preferences
		////////////////////////
		else if (key.equals(mContext.getString(R.string.pref_layout_display_date_key))) {
			if (value instanceof Boolean) {
				this.mShowDate = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_layout_display_category_key))) {
			if (value instanceof Boolean) {
				this.mShowCategory = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		else if (key.equals(mContext.getString(R.string.pref_layout_display_photo_key))) {
			if (value instanceof Boolean) {
				this.mShowPhotoPDFMarker = (Boolean) value;
				return true;
			}
			else {
				if (BuildConfig.DEBUG) Log.e(TAG, "Invalid value class type - " + value);
				return false;
			}
		}
		// Default Return
		else {
			return false;
		}
	}
    */

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

	public void setDefaultTaxPercentage(float defaultTaxPercentage) {
		this.mDefaultTaxPercentage = defaultTaxPercentage;
	}

	public void setShowReceiptID(boolean showReceiptID) {
		this.mShowReceiptID = showReceiptID;
	}

	public String getEmailCC() {
		return mEmailCC;
	}

	public void setEmailCC(String emailCC) {
		this.mEmailCC = emailCC;
	}

	public String getEmailBCC() {
		return mEmailBCC;
	}

	public void setEmailBCC(String emailBCC) {
		this.mEmailBCC = emailBCC;
	}

	public String getEmailSubject() {
		return mEmailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.mEmailSubject = emailSubject;
	}

	public boolean isCameraGrayScale() {
		return mCameraGrayScale;
	}

	public void setCameraGrayScale(boolean cameraGrayScale) {
		this.mCameraGrayScale = cameraGrayScale;
	}

	public boolean isShowDate() {
		return mShowDate;
	}

	public void setShowDate(boolean showDate) {
		this.mShowDate = showDate;
	}

	public boolean isShowCategory() {
		return mShowCategory;
	}

	public void setShowCategory(boolean showCategory) {
		this.mShowCategory = showCategory;
	}

	public boolean isShowPhotoPDFMarker() {
		return mShowPhotoPDFMarker;
	}

	public void setmShowPhotoPDFMarker(boolean showPhotoPDFMarker) {
		this.mShowPhotoPDFMarker = showPhotoPDFMarker;
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

	/*
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void writeLastActivityTag(String tag) {
		SharedPreferences prefs = mContext.getSharedPreferences(SMART_PREFS, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(STRING_LAST_ACTIVITY_TAG, tag);
		if (Utils.ApiHelper.hasGingerbread())
    		editor.apply();
    	else
    		editor.commit();
	}

	public String getLastActivityTag() {
		SharedPreferences prefs = mContext.getSharedPreferences(SMART_PREFS, 0);
		return prefs.getString(STRING_LAST_ACTIVITY_TAG, "");
	}*/

}