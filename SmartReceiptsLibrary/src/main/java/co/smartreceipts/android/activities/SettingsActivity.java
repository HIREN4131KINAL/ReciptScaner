package co.smartreceipts.android.activities;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.fragments.preferences.DefaultTaxPercentagePreference;
import co.smartreceipts.android.fragments.preferences.MinimumPriceEditTextPreference;
import co.smartreceipts.android.fragments.preferences.PreferenceHeaderFragment;
import co.smartreceipts.android.fragments.preferences.UniversalPreferences;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.purchases.PurchaseableSubscriptions;
import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionEventsListener;
import co.smartreceipts.android.purchases.SubscriptionManager;
import co.smartreceipts.android.purchases.SubscriptionWallet;
import co.smartreceipts.android.workers.EmailAssistant;
import wb.android.preferences.SummaryEditTextPreference;
import wb.android.storage.StorageManager;
import wb.android.util.AppRating;

public class SettingsActivity extends AppCompatPreferenceActivity implements OnPreferenceClickListener, UniversalPreferences, SubscriptionEventsListener {

    public static final String TAG = "SettingsActivity";
    private static final int GET_SIGNATURE_PHOTO_REQUEST_CODE = 1;

    private volatile PurchaseableSubscriptions mPurchaseableSubscriptions;
    private SmartReceiptsApplication mApp;
    private SubscriptionManager mSubscriptionManager;
    private boolean mIsUsingHeaders;

    /**
     * Ugly hack to determine if a fragment header is currently showing or not. See if I can replace by counting the
     * fragment manager entries
     */
    private boolean mIsFragmentHeaderShowing = false;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = ((SmartReceiptsApplication) getApplication());
        mIsUsingHeaders = getResources().getBoolean(R.bool.isTablet);

        if (!mIsUsingHeaders) {
            // Load the legacy preferences headers
            getPreferenceManager().setSharedPreferencesName(Preferences.SMART_PREFS);
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(((SmartReceiptsApplication) getApplication()).getPersistenceManager().getPreferences());
            addPreferencesFromResource(R.xml.preference_legacy);
            configurePreferencesGeneral(this);
            configurePreferencesReceipts(this);
            configurePreferencesOutput(this);
            configurePreferencesEmail(this);
            configurePreferencesCamera(this);
            configurePreferencesLayoutCustomizations(this);
            configurePreferencesDistance(this);
            configureProPreferences(this);
            configurePreferencesHelp(this);
            configurePreferencesAbout(this);
        }

        mSubscriptionManager = new SubscriptionManager(this, ((SmartReceiptsApplication)getApplication()).getPersistenceManager().getSubscriptionCache());
        mSubscriptionManager.onCreate();
        mSubscriptionManager.addEventListener(this);
        mSubscriptionManager.querySubscriptions();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            if (root != null) {
                final Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
                root.addView(toolbar, 0); // insert at top
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        } else {
            final ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            if (root != null) {
                final ListView content = (ListView) root.getChildAt(0);
                final Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
                root.removeAllViews();

                final int height;
                final TypedValue tv = new TypedValue();
                if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                    height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                } else {
                    height = toolbar.getHeight();
                }

                content.setPadding(0, height, 0, 0);
                root.addView(content);
                root.addView(toolbar);
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        }

    }

    @Override
    protected void onStart() {
        mApp.getWorkerManager().getLogger().logScreen(this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_main_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // Called only on Honeycomb and later
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        // Called before onCreate it seems
        mIsUsingHeaders = getResources().getBoolean(R.bool.isTablet);
        if (mIsUsingHeaders) {
            loadHeadersFromResource(R.xml.preference_headers, target);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PreferenceHeaderFragment.class.getName().equals(fragmentName)) {
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mIsFragmentHeaderShowing) { // If we're actively showing a fragment, let it handle the call
            return super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == android.R.id.home) {
            final Intent upIntent = new Intent(this, mApp.getTopLevelActivity());
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent) // Add all of this activity's
                        // parents to the back stack
                        .startActivities(); // Navigate up to the closest parent
            } else {
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == GET_SIGNATURE_PHOTO_REQUEST_CODE) {
                final StorageManager storageManager = mApp.getPersistenceManager().getStorageManager();
                final Preferences preferences = mApp.getPersistenceManager().getPreferences();
                new CopySignatureImageClass(getContentResolver(), data, preferences, storageManager, getSharedPreferences(Preferences.SMART_PREFS, 0), getString(R.string.pref_output_signature_picture_key)).execute();
            } else{
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
        else {
            if (!mSubscriptionManager.onActivityResult(requestCode, resultCode, data)) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onDestroy() {
        mSubscriptionManager.removeEventListener(this);
        mSubscriptionManager.onDestroy();
        super.onDestroy();
    }

    public boolean isUsingHeaders() {
        return mIsUsingHeaders;
    }

    public void setFragmentHeaderIsShowing(boolean isShowing) {
        mIsFragmentHeaderShowing = isShowing;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Preference findPreference(int stringId) {
        return findPreference(getString(stringId));
    }

    public void configurePreferencesGeneral(UniversalPreferences universal) {
        // Get the currency list
        PersistenceManager persistenceManager = ((SmartReceiptsApplication) getApplication()).getPersistenceManager();
        ArrayList<CharSequence> currencyList = persistenceManager.getDatabase().getCurrenciesList();
        CharSequence[] currencyArray = new CharSequence[currencyList.size()];
        currencyList.toArray(currencyArray);

        // Get the date separator list
        final String defaultSepartor = persistenceManager.getPreferences().getDateSeparator();
        final CharSequence[] dateSeparators = getDateSeparatorOptions(persistenceManager.getPreferences());

        // Set up the ListPreference data
        ListPreference currencyPreference = (ListPreference) universal.findPreference(R.string.pref_general_default_currency_key);
        currencyPreference.setEntries(currencyArray);
        currencyPreference.setEntryValues(currencyArray);
        currencyPreference.setValue(persistenceManager.getPreferences().getDefaultCurreny());
        ListPreference dateSeparatorPreference = (ListPreference) universal.findPreference(R.string.pref_general_default_date_separator_key);
        dateSeparatorPreference.setEntries(dateSeparators);
        dateSeparatorPreference.setEntryValues(dateSeparators);
        dateSeparatorPreference.setValue(defaultSepartor);
    }

    private CharSequence[] getDateSeparatorOptions(Preferences preferences) {
        final int definedDateSeparatorCount = 3;
        CharSequence[] dateSeparators;
        final String defaultSepartor = preferences.getDateSeparator();
        if (!defaultSepartor.equals("-") && !defaultSepartor.equals("/")) {
            dateSeparators = new CharSequence[definedDateSeparatorCount + 1];
            dateSeparators[definedDateSeparatorCount] = defaultSepartor;
        } else {
            dateSeparators = new CharSequence[definedDateSeparatorCount];
        }
        dateSeparators[0] = "/";
        dateSeparators[1] = "-";
        dateSeparators[2] = ".";
        return dateSeparators;
    }

    public void configurePreferencesReceipts(UniversalPreferences universal) {
        // Set on Preference Click Listeners for all that require it
        universal.findPreference(R.string.pref_receipt_customize_categories_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_receipt_payment_methods_key).setOnPreferenceClickListener(this);

        // Here we restore our current values (easier than getting the FloatEditText stuff to work)
        Preferences preferences = ((SmartReceiptsApplication) getApplication()).getPersistenceManager().getPreferences();
        DefaultTaxPercentagePreference taxPercentagePreference = (DefaultTaxPercentagePreference) universal.findPreference(R.string.pref_receipt_tax_percent_key);
        taxPercentagePreference.setText(Float.toString(preferences.getDefaultTaxPercentage()));
        MinimumPriceEditTextPreference minimumPriceEditTextPreference = (MinimumPriceEditTextPreference) universal.findPreference(R.string.pref_receipt_minimum_receipts_price_key);
        minimumPriceEditTextPreference.setText(Float.toString(preferences.getMinimumReceiptPriceToIncludeInReports()));

    }

    public void configurePreferencesOutput(UniversalPreferences universal) {
        // Set on Preference Click Listeners for all that require it
        universal.findPreference(R.string.pref_output_custom_csv_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_output_custom_pdf_key).setOnPreferenceClickListener(this);
        final Preference signaturePreference = universal.findPreference(R.string.pref_output_signature_picture_key);
        if (signaturePreference != null) {
            signaturePreference.setOnPreferenceClickListener(this);
        }
    }

    public void configurePreferencesEmail(UniversalPreferences universal) {
        Preference subjectPreference = universal.findPreference(R.string.pref_email_default_email_subject_key);
        subjectPreference.setDefaultValue(((SmartReceiptsApplication) getApplication()).getFlex().getString(this, R.string.EMAIL_DATA_SUBJECT));
    }

    public void configurePreferencesCamera(UniversalPreferences universal) {

    }

    public void configurePreferencesLayoutCustomizations(UniversalPreferences universal) {

    }

    public void configurePreferencesDistance(UniversalPreferences universal) {

    }

    public void configureProPreferences(UniversalPreferences universal) {
        final boolean hasProSubscription = mApp.getPersistenceManager().getSubscriptionCache().getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPro);
        final SummaryEditTextPreference pdfFooterPreference = (SummaryEditTextPreference) universal.findPreference(R.string.pref_pro_pdf_footer_key);
        pdfFooterPreference.setAppearsEnabled(hasProSubscription);
        pdfFooterPreference.setOnPreferenceClickListener(this);
    }

    public void configurePreferencesHelp(UniversalPreferences universal) {
        // Set on Preference Click Listeners for all that require it
        universal.findPreference(R.string.pref_help_send_feedback_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_help_send_love_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_help_support_email_key).setOnPreferenceClickListener(this);
    }

    public void configurePreferencesAbout(UniversalPreferences universal) {
        // Set up Version Summary
        Preference versionPreference = universal.findPreference(R.string.pref_about_version_key);
        versionPreference.setSummary(getAppVersion());
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        if (key.equals(getString(R.string.pref_receipt_customize_categories_key)) || key.equals(getString(R.string.pref_output_custom_csv_key)) || key.equals(getString(R.string.pref_output_custom_pdf_key)) || key.equals(getString(R.string.pref_receipt_payment_methods_key))) {
            final Intent intent = new Intent(this, SettingsViewerActivity.class);
            intent.putExtra(SettingsViewerActivity.KEY_FLAG, key);
            startActivity(intent);
            return true;
        } else if (key.equals(getString(R.string.pref_help_send_love_key))) { // Dark Pattern... Send Love => AppStore.
            // Others for email
            startActivity(AppRating.getRatingIntent(this, getString(R.string.package_name)));
            return true;
        } else if (key.equals(getString(R.string.pref_help_send_feedback_key))) {
            final Intent intent = EmailAssistant.getEmailDeveloperIntent(getString(R.string.feedback, getString(R.string.sr_app_name)));
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
            return true;
        } else if (key.equals(getString(R.string.pref_help_support_email_key))) {
            final Intent intent = EmailAssistant.getEmailDeveloperIntent(getString(R.string.support, getString(R.string.sr_app_name)), getDebugScreen());
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
            return true;
        } else if (key.equals(getString(R.string.pref_output_signature_picture_key))) {
            final Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, GET_SIGNATURE_PHOTO_REQUEST_CODE);
            return true;
        } else if (key.equals(getString(R.string.pref_pro_pdf_footer_key))) {
            // Let's check if we should prompt the user to upgrade for this preference
            final boolean haveProSubscription = mApp.getPersistenceManager().getSubscriptionCache().getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPro);
            final boolean proSubscriptionIsAvailable = mPurchaseableSubscriptions != null && mPurchaseableSubscriptions.isSubscriptionAvailableForPurchase(Subscription.SmartReceiptsPro);

            // If we don't already have the pro subscription and it's available, let's buy it
            if (proSubscriptionIsAvailable && !haveProSubscription) {
                mSubscriptionManager.queryBuyIntent(Subscription.SmartReceiptsPro);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSubscriptionsAvailable(@NonNull PurchaseableSubscriptions purchaseableSubscriptions, @NonNull SubscriptionWallet subscriptionWallet) {
        Log.i(TAG, "The following subscriptions are available: " + purchaseableSubscriptions);
        mPurchaseableSubscriptions = purchaseableSubscriptions;
    }

    @Override
    public void onSubscriptionsUnavailable() {
        Log.w(TAG, "No subscriptions were found for this session");
        // Intentional no-op
    }

    @Override
    public void onPurchaseIntentAvailable(@NonNull Subscription subscription, @NonNull PendingIntent pendingIntent, @NonNull String key) {
        try {
            startIntentSenderForResult(pendingIntent.getIntentSender(), SubscriptionManager.REQUEST_CODE, new Intent(), 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SettingsActivity.this, R.string.purchase_unavailable, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onPurchaseIntentUnavailable(@NonNull Subscription subscription) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingsActivity.this, R.string.purchase_unavailable, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPurchaseSuccess(@NonNull Subscription subscription, @NonNull SubscriptionWallet updateSubscriptionWallet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu(); // To hide the subscription option
                Toast.makeText(SettingsActivity.this, R.string.purchase_succeeded, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPurchaseFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingsActivity.this, R.string.purchase_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getString(R.string.package_name), 0).versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private String getDebugScreen() {
        String debug = "Debug-information: \n" + "Smart Receipts Version: " + getAppVersion() + "\n" + "Brand: " + android.os.Build.BRAND + "\n" + "CPU: " + android.os.Build.CPU_ABI + "\n" + "OS API Level: " + android.os.Build.VERSION.SDK_INT + "\n" + "Device: " + android.os.Build.DEVICE + "\n" + "Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")\n" + "Two-Paned: " + mIsUsingHeaders;
        return debug;
    }

    private static class CopySignatureImageClass extends AsyncTask<Void, Void, Boolean> {

        private final ContentResolver mContentResolver;
        private final Intent mIntent;
        private final Preferences mPreferences;
        private final StorageManager mStorageManager;
        private final SharedPreferences mSharedPreferences;
        private final String mKey;

        public CopySignatureImageClass(ContentResolver contentResolver, Intent intent, Preferences preferences, StorageManager storageManager, SharedPreferences sharedPreferences, String key) {
            mContentResolver = contentResolver;
            mIntent = intent;
            mPreferences = preferences;
            mStorageManager = storageManager;
            mSharedPreferences = sharedPreferences;
            mKey = key;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (mIntent == null || mIntent.getData() == null) {
                return false;
            }
            final Uri uri = mIntent.getData();
            InputStream inputStream = null;
            try {
                final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                final String extension = mimeTypeMap.getExtensionFromMimeType(mContentResolver.getType(uri));
                final String filename = "signature." + extension;
                final File destination = mStorageManager.getFile(mPreferences.getPreferencesFolder(), filename);
                inputStream = mContentResolver.openInputStream(uri);
                mStorageManager.copy(inputStream, destination, true);
                mSharedPreferences.edit().putString(mKey, filename).apply();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Failed to save signature image in onActivityResult", e);
                return false;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) { /* Intentional Stub */ }
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // TODO: Toast Yay
            }
            else {
                // TODO: Toast Booo
            }
        }
    }

}