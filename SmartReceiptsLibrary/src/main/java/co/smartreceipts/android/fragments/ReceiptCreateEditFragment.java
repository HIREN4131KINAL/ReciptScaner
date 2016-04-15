package co.smartreceipts.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.adapters.TaxAutoCompleteAdapter;
import co.smartreceipts.android.apis.ExchangeRateServiceManager;
import co.smartreceipts.android.apis.MemoryLeakSafeCallback;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionManager;
import co.smartreceipts.android.widget.HideSoftKeyboardOnTouchListener;
import co.smartreceipts.android.widget.NetworkRequestAwareEditText;
import co.smartreceipts.android.widget.ShowSoftKeyboardOnFocusChangeListener;
import co.smartreceipts.android.widget.UserSelectionTrackingOnItemSelectedListener;
import retrofit.RetrofitError;
import retrofit.client.Response;
import wb.android.autocomplete.AutoCompleteAdapter;

public class ReceiptCreateEditFragment extends WBFragment implements View.OnFocusChangeListener, NetworkRequestAwareEditText.RetryListener, DatabaseHelper.ReceiptAutoCompleteListener {

    private static final String TAG = ReceiptCreateEditFragment.class.getSimpleName();
    private static final String ARG_FILE = "arg_file";
    private static final String KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE = "key_is_exchange_rate_visible";

    // Metadata
    private Trip mTrip;
    private Receipt mReceipt;
    private File mFile;

    // Views
    private AutoCompleteTextView nameBox;
    private EditText priceBox;
    private AutoCompleteTextView taxBox;
    private Spinner currencySpinner;
    private NetworkRequestAwareEditText exchangeRateBox;
    private DateEditText dateBox;
    private AutoCompleteTextView commentBox;
    private Spinner categoriesSpinner;
    private CheckBox expensable;
    private CheckBox fullpage;
    private Spinner paymentMethodsSpinner;
    private EditText extra_edittext_box_1;
    private EditText extra_edittext_box_2;
    private EditText extra_edittext_box_3;
    private ViewGroup mPaymentMethodsContainer;
    private ViewGroup mExchangeRateContainer;
    private Toolbar mToolbar;
    private View mFocusedView;

    // Misc
    private MemoryLeakSafeCallback<ExchangeRate, EditText> mLastExchangeRateFetchCallback;
    private NavigationHandler mNavigationHandler;
    private ExchangeRateServiceManager mExchangeRateServiceManager;
    private ReceiptInputCache mReceiptInputCache;
    private AutoCompleteAdapter mReceiptsNameAutoCompleteAdapter, mReceiptsCommentAutoCompleteAdapter;
    private ArrayAdapter<CharSequence> mCurrenciesAdapter;
    private ArrayAdapter<CharSequence> mCategoriesAdpater;
    private ArrayAdapter<PaymentMethod> mPaymentMethodsAdapter;
    private ExecutorService mExecutorService;

    /**
     * Creates a new instance of this fragment for a new receipt
     *
     * @param trip - the parent trip of this receipt
     * @param file - the file associated with this receipt or null if we do not have one
     * @return the new instance of this fragment
     */
    public static ReceiptCreateEditFragment newInstance(@NonNull Trip trip, @Nullable File file) {
        return newInstance(trip, null, file);
    }

    /**
     * Creates a new instance of this fragment to edit an existing receipt
     *
     * @param trip          - the parent trip of this receipt
     * @param receiptToEdit - the receipt to edit
     * @return the new instance of this fragment
     */
    public static ReceiptCreateEditFragment newInstance(@NonNull Trip trip, @NonNull Receipt receiptToEdit) {
        return newInstance(trip, receiptToEdit, null);
    }

    private static ReceiptCreateEditFragment newInstance(@NonNull Trip trip, @Nullable Receipt receiptToEdit, @Nullable File file) {
        final ReceiptCreateEditFragment fragment = new ReceiptCreateEditFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);
        args.putParcelable(Receipt.PARCEL_KEY, receiptToEdit);
        args.putSerializable(ARG_FILE, file);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
        mReceipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        mFile = (File) getArguments().getSerializable(ARG_FILE);
        mReceiptInputCache = new ReceiptInputCache(getFragmentManager());
        mNavigationHandler = new NavigationHandler(getActivity(), new DefaultFragmentProvider());
        mExchangeRateServiceManager = new ExchangeRateServiceManager(getFragmentManager());
        mCurrenciesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCurrenciesList());
        mCategoriesAdpater = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCategoriesList());
        mPaymentMethodsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getPaymentMethods());
        setHasOptionsMenu(true);

        if (getPersistenceManager().getPreferences().isShowReceiptID()) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.update_receipt, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        this.nameBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_NAME);
        this.priceBox = (EditText) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_PRICE);
        this.taxBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_TAX);
        this.currencySpinner = (Spinner) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CURRENCY);
        this.exchangeRateBox = (NetworkRequestAwareEditText) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXCHANGE_RATE);
        mExchangeRateContainer = (ViewGroup) getFlex().getSubView(getActivity(), rootView, R.id.exchange_rate_container);
        this.dateBox = (DateEditText) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_DATE);
        this.commentBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_COMMENT);
        this.categoriesSpinner = (Spinner) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CATEGORY);
        this.expensable = (CheckBox) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXPENSABLE);
        this.fullpage = (CheckBox) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_FULLPAGE);
        this.paymentMethodsSpinner = (Spinner) getFlex().getSubView(getActivity(), rootView, R.id.dialog_receiptmenu_payment_methods_spinner);
        mPaymentMethodsContainer = (ViewGroup) getFlex().getSubView(getActivity(), rootView, R.id.payment_methods_container);

        // Extras
        final LinearLayout extras = (LinearLayout) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXTRAS);
        this.extra_edittext_box_1 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_1));
        this.extra_edittext_box_2 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_2));
        this.extra_edittext_box_3 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_3));

        // Toolbar stuff
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (mNavigationHandler.isDualPane()) {
            mToolbar.setVisibility(View.GONE);
        } else {
            setSupportActionBar(mToolbar);
        }

        // Set each focus listener, so we can track the focus view across resume -> pauses
        this.nameBox.setOnFocusChangeListener(this);
        this.priceBox.setOnFocusChangeListener(this);
        this.taxBox.setOnFocusChangeListener(this);
        this.currencySpinner.setOnFocusChangeListener(this);
        this.dateBox.setOnFocusChangeListener(this);
        this.commentBox.setOnFocusChangeListener(this);

        // Custom view properties
        exchangeRateBox.setFailedHint(R.string.DIALOG_RECEIPTMENU_HINT_EXCHANGE_RATE_FAILED);

        // Set click listeners
        dateBox.setOnTouchListener(new HideSoftKeyboardOnTouchListener());
        categoriesSpinner.setOnTouchListener(new HideSoftKeyboardOnTouchListener());
        currencySpinner.setOnTouchListener(new HideSoftKeyboardOnTouchListener());

        // Show default dictionary with auto-complete
        nameBox.setKeyListener(TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES));

        // Set-up tax layers
        if (getPersistenceManager().getPreferences().includeTaxField()) {
            priceBox.setHint(getFlexString(R.string.DIALOG_RECEIPTMENU_HINT_PRICE_SHORT));
            taxBox.setVisibility(View.VISIBLE);
        }

        // Configure dropdown defaults for currencies
        mCurrenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(mCurrenciesAdapter);

        // And categories
        mCategoriesAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(mCategoriesAdpater);

        // And payment methods
        mPaymentMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodsSpinner.setAdapter(mPaymentMethodsAdapter);

        // And the exchange rate processing for our currencies
        final boolean exchangeRateIsVisible = savedInstanceState != null && savedInstanceState.getBoolean(KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE);
        if (exchangeRateIsVisible) {
            // Note: the restoration of selected spinner items (in the currency spinner) is delayed so we use this state tracker to restore immediately
            mExchangeRateContainer.setVisibility(View.VISIBLE);
        }
        currencySpinner.setOnItemSelectedListener(new UserSelectionTrackingOnItemSelectedListener() {

            @Override
            public void onUserSelectedNewItem(AdapterView<?> parent, View view, int position, long id, int previousPosition) {
                // Then determine if we should show/hide the box
                final String baseCurrencyCode = mCurrenciesAdapter.getItem(position).toString();
                final String exchangeRateCurrencyCode = mTrip.getDefaultCurrencyCode();
                if (baseCurrencyCode.equals(exchangeRateCurrencyCode)) {
                    mExchangeRateContainer.setVisibility(View.GONE);
                    exchangeRateBox.setText(""); // Clear out if we're hiding the box
                } else {
                    mExchangeRateContainer.setVisibility(View.VISIBLE);
                    submitExchangeRateRequest(baseCurrencyCode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Intentional no-op
            }
        });

        // Outline date defaults
        dateBox.setFocusableInTouchMode(false);
        dateBox.setOnClickListener(getDateManager().getDateEditTextListener());

        // Lastly, preset adapters for "new" receipts
        final boolean isNewReceipt = mReceipt == null;
        if (isNewReceipt) {
            if (getPersistenceManager().getPreferences().includeTaxField()) {
                taxBox.setAdapter(new TaxAutoCompleteAdapter(getActivity(), priceBox, taxBox, getPersistenceManager().getPreferences(), getPersistenceManager().getPreferences().getDefaultTaxPercentage()));
            }

            final Preferences preferences = getPersistenceManager().getPreferences();
            if (preferences.matchCommentToCategory() && preferences.matchNameToCategory()) {
                categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(nameBox, commentBox, mCategoriesAdpater));
            } else if (preferences.matchCommentToCategory()) {
                categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(null, commentBox, mCategoriesAdpater));
            } else if (preferences.matchNameToCategory()) {
                categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(nameBox, null, mCategoriesAdpater));
            }
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Configure things if it's not a restored fragment
        if (savedInstanceState == null) {

            final boolean isNewReceipt = mReceipt == null;
            if (isNewReceipt) {

                final Time now = new Time();
                now.setToNow();
                if (mReceiptInputCache.getCachedDate() == null) {
                    if (getPersistenceManager().getPreferences().defaultToFirstReportDate()) {
                        dateBox.date = mTrip.getStartDate();
                    } else {
                        dateBox.date = new Date(now.toMillis(false));
                    }
                } else {
                    dateBox.date = mReceiptInputCache.getCachedDate();
                }
                dateBox.setText(DateFormat.getDateFormat(getActivity()).format(dateBox.date));

                final Preferences preferences = getPersistenceManager().getPreferences();
                expensable.setChecked(preferences.doReceiptsDefaultAsExpensable());
                if (preferences.matchCommentToCategory() && preferences.matchNameToCategory()) {
                    if (mFocusedView == null) {
                        mFocusedView = priceBox;
                    }
                } else if (preferences.matchNameToCategory()) {
                    if (mFocusedView == null) {
                        mFocusedView = priceBox;
                    }
                }

                if (preferences.predictCategories()) { // Predict Breakfast, Lunch, Dinner by the hour
                    if (mReceiptInputCache.getCachedCategory() == null) {
                        if (now.hour >= 4 && now.hour < 11) { // Breakfast hours
                            int idx = mCategoriesAdpater.getPosition(getString(R.string.category_breakfast));
                            if (idx > 0) {
                                categoriesSpinner.setSelection(idx);
                            }
                        } else if (now.hour >= 11 && now.hour < 16) { // Lunch hours
                            int idx = mCategoriesAdpater.getPosition(getString(R.string.category_lunch));
                            if (idx > 0) {
                                categoriesSpinner.setSelection(idx);
                            }
                        } else if (now.hour >= 16 && now.hour < 23) { // Dinner hours
                            int idx = mCategoriesAdpater.getPosition(getString(R.string.category_dinner));
                            if (idx > 0) {
                                categoriesSpinner.setSelection(idx);
                            }
                        }
                    } else {
                        int idx = mCategoriesAdpater.getPosition(mReceiptInputCache.getCachedCategory());
                        if (idx > 0) {
                            categoriesSpinner.setSelection(idx);
                        }
                    }
                }

                int idx = mCurrenciesAdapter.getPosition((mTrip != null) ? mTrip.getDefaultCurrencyCode() : preferences.getDefaultCurreny());
                int cachedIdx = (mReceiptInputCache.getCachedCurrency() != null) ? mCurrenciesAdapter.getPosition(mReceiptInputCache.getCachedCurrency()) : -1;
                idx = (cachedIdx > 0) ? cachedIdx : idx;
                if (idx > 0) {
                    currencySpinner.setSelection(idx);
                }
                fullpage.setChecked(preferences.shouldDefaultToFullPage());
                if (getPersistenceManager().getPreferences().getUsesPaymentMethods()) {
                    mPaymentMethodsContainer.setVisibility(View.VISIBLE);
                }

            } else {
                nameBox.setText(mReceipt.getName());
                priceBox.setText(mReceipt.getPrice().getDecimalFormattedPrice());
                dateBox.setText(mReceipt.getFormattedDate(getActivity(), getPersistenceManager().getPreferences().getDateSeparator()));
                dateBox.date = mReceipt.getDate();
                categoriesSpinner.setSelection(mCategoriesAdpater.getPosition(mReceipt.getCategory()));
                commentBox.setText(mReceipt.getComment());
                taxBox.setText(mReceipt.getTax().getDecimalFormattedPrice());

                final ExchangeRate exchangeRate = mReceipt.getPrice().getExchangeRate();
                if (exchangeRate.supportsExchangeRateFor(mTrip.getDefaultCurrencyCode())) {
                    exchangeRateBox.setText(exchangeRate.getDecimalFormattedExchangeRate(mTrip.getDefaultCurrencyCode()));
                }

                int idx = mCurrenciesAdapter.getPosition(mReceipt.getPrice().getCurrencyCode());
                if (idx > 0) {
                    currencySpinner.setSelection(idx);
                }

                if (mReceipt.getPrice().getCurrency().equals(mTrip.getPrice().getCurrency())) {
                    mExchangeRateContainer.setVisibility(View.GONE);
                } else {
                    mExchangeRateContainer.setVisibility(View.VISIBLE);
                }

                expensable.setChecked(mReceipt.isExpensable());
                fullpage.setChecked(mReceipt.isFullPage());

                if (getPersistenceManager().getPreferences().getUsesPaymentMethods()) {
                    mPaymentMethodsContainer.setVisibility(View.VISIBLE);
                    final PaymentMethod oldPaymentMethod = mReceipt.getPaymentMethod();
                    if (oldPaymentMethod != null) {
                        final int paymentIdx = mPaymentMethodsAdapter.getPosition(oldPaymentMethod);
                        if (paymentIdx > 0) {
                            paymentMethodsSpinner.setSelection(paymentIdx);
                        }
                    }
                }
                if (extra_edittext_box_1 != null && mReceipt.hasExtraEditText1()) {
                    extra_edittext_box_1.setText(mReceipt.getExtraEditText1());
                }
                if (extra_edittext_box_2 != null && mReceipt.hasExtraEditText2()) {
                    extra_edittext_box_2.setText(mReceipt.getExtraEditText2());
                }
                if (extra_edittext_box_3 != null && mReceipt.hasExtraEditText3()) {
                    extra_edittext_box_3.setText(mReceipt.getExtraEditText3());
                }
            }

            // Focused View
            if (mFocusedView == null) {
                mFocusedView = nameBox;
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        final boolean isNewReceipt = mReceipt == null;

        final String title;
        if (isNewReceipt) {
            title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW);
        } else {
            if (getPersistenceManager().getPreferences().isShowReceiptID()) {
                title = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT_ID), mReceipt.getId());
            } else {
                title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT);
            }
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
            actionBar.setTitle(title);
            actionBar.setSubtitle("");
        }

        if (isNewReceipt && getPersistenceManager().getPreferences().isShowReceiptID() && mExecutorService != null) {
            // To prevent any ANRs due to this DB action in onResume()
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    final String titleWithId = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW_ID), getPersistenceManager().getDatabase().getNextReceiptAutoIncremenetIdSerial());
                    if (isAdded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isAdded() && actionBar != null) {
                                    actionBar.setTitle(titleWithId);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (isNewReceipt) {
            if (getPersistenceManager().getPreferences().enableAutoCompleteSuggestions()) {
                final DatabaseHelper db = getPersistenceManager().getDatabase();
                if (mReceiptsNameAutoCompleteAdapter == null) {
                    mReceiptsNameAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_RECEIPTS_NAME, db, db);
                } else {
                    mReceiptsNameAutoCompleteAdapter.reset();
                }
                if (mReceiptsCommentAutoCompleteAdapter == null) {
                    mReceiptsCommentAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_RECEIPTS_COMMENT, db);
                } else {
                    mReceiptsCommentAutoCompleteAdapter.reset();
                }
                nameBox.setAdapter(mReceiptsNameAutoCompleteAdapter);
                commentBox.setAdapter(mReceiptsCommentAutoCompleteAdapter);
            }
        }

        if (mFocusedView != null) {
            mFocusedView.requestFocus(); // Make sure we're focused on the right view
        }

        exchangeRateBox.setRetryListener(this);
        getPersistenceManager().getDatabase().registerReceiptAutoCompleteListener(this);
    }

    @Override
    public void onPause() {
        // Notify the downstream adapters
        if (mReceiptsNameAutoCompleteAdapter != null) {
            mReceiptsNameAutoCompleteAdapter.onPause();
        }
        if (mReceiptsCommentAutoCompleteAdapter != null) {
            mReceiptsCommentAutoCompleteAdapter.onPause();
        }

        // Dismiss the soft keyboard
        final InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (mFocusedView != null) {
                inputMethodManager.hideSoftInputFromWindow(mFocusedView.getWindowToken(), 0);
            } else {
                Log.w(TAG, "Unable to dismiss soft keyboard due to a null view");
            }
        }

        exchangeRateBox.setRetryListener(null);
        getPersistenceManager().getDatabase().unregisterReceiptAutoCompleteListener();
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mExchangeRateContainer != null && outState != null) {
            outState.putBoolean(KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE, mExchangeRateContainer.getVisibility() == View.VISIBLE);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (mExecutorService != null) {
            mExecutorService.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationHandler.navigateToReportInfoFragment(mTrip);
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            saveReceipt();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        mFocusedView = hasFocus ? v : null;
        if (mReceipt == null) {
            // Only launch if we have focus and it's a new receipt
            new ShowSoftKeyboardOnFocusChangeListener().onFocusChange(v, hasFocus);
        }
    }

    @Override
    public void onUserRetry() {
        if (getPersistenceManager().getSubscriptionCache().getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPro)) {
            Log.i(TAG, "Attempting to retry with valid subscription. Submitting request directly");
            submitExchangeRateRequest((String) currencySpinner.getSelectedItem());
        } else {
            Log.i(TAG, "Attempting to retry without valid subscription. Directing user to purchase intent");
            final Activity activity = getActivity();
            if (activity instanceof SmartReceiptsActivity) {
                final SmartReceiptsActivity smartReceiptsActivity = (SmartReceiptsActivity) activity;
                final SubscriptionManager subscriptionManager = smartReceiptsActivity.getSubscriptionManager();
                if (subscriptionManager != null) {
                    subscriptionManager.queryBuyIntent(Subscription.SmartReceiptsPro);
                }
            }
        }
    }

    @Override
    public void onReceiptRowAutoCompleteQueryResult(String name, String price, String category) {
        if (isAdded()) {
            if (nameBox != null && name != null) {
                nameBox.setText(name);
                nameBox.setSelection(name.length());
            }
            if (priceBox != null && price != null && priceBox.getText().length() == 0) {
                priceBox.setText(price);
            }
            if (categoriesSpinner != null && category != null) {
                categoriesSpinner.setSelection(getPersistenceManager().getDatabase().getCategoriesList().indexOf(category));
            }
        }
    }

    private synchronized void submitExchangeRateRequest(@NonNull String baseCurrencyCode) {
        exchangeRateBox.setText(""); // Clear results to avoid stale data here
        if (getPersistenceManager().getSubscriptionCache().getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPro)) {
            Log.i(TAG, "Submitting exchange rate request");
            getWorkerManager().getLogger().logEvent(ReceiptCreateEditFragment.this, "Submit_Exchange_Rate_Request");
            final String exchangeRateCurrencyCode = mTrip.getDefaultCurrencyCode();
            exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Loading);
            if (mLastExchangeRateFetchCallback != null) {
                // Ignore any outstanding results to not confuse ourselves
                mLastExchangeRateFetchCallback.ignoreResult();
            }
            mLastExchangeRateFetchCallback = new MemoryLeakSafeCallback<ExchangeRate, EditText>(exchangeRateBox) {
                @Override
                public void success(EditText editText, ExchangeRate exchangeRate, Response response) {
                    if (exchangeRate != null) {
                        getWorkerManager().getLogger().logEvent(ReceiptCreateEditFragment.this, "Submit_Exchange_Rate_Success");
                        if (TextUtils.isEmpty(editText.getText())) {
                            editText.setText(exchangeRate.getDecimalFormattedExchangeRate(exchangeRateCurrencyCode));
                        } else {
                            Log.w(TAG, "User already started typing... Ignoring exchange rate result");
                        }
                        exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Success);
                    } else {
                        Log.e(TAG, "Received a null exchange rate");
                        getWorkerManager().getLogger().logEvent(ReceiptCreateEditFragment.this, "Submit_Exchange_Rate_Failed_Null");
                        exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Failure);
                    }
                }

                @Override
                public void failure(EditText editText, RetrofitError error) {
                    Log.e(TAG, "" + error);
                    getWorkerManager().getLogger().logEvent(ReceiptCreateEditFragment.this, "Submit_Exchange_Rate_Failed");
                    exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Failure);
                }
            };
            mExchangeRateServiceManager.getService().getExchangeRate(dateBox.date, baseCurrencyCode, exchangeRateCurrencyCode, mLastExchangeRateFetchCallback);
        } else {
            exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Ready);
            Log.i(TAG, "Ignoring exchange rate request, since there is no subscription for it");
        }
    }

    private void saveReceipt() {
        final String name = nameBox.getText().toString();
        final String category = categoriesSpinner.getSelectedItem().toString();
        final String currency = currencySpinner.getSelectedItem().toString();

        if (name.length() == 0) {
            Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_MISSING_NAME), Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateBox.date == null) {
            Toast.makeText(getActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
            return;
        } else {
            mReceiptInputCache.setCachedDate((Date) dateBox.date.clone());
        }

        mReceiptInputCache.setCachedCategory(category);
        mReceiptInputCache.setCachedCurrency(currency);

        if (!mTrip.isDateInsideTripBounds(dateBox.date)) {
            if (isAdded()) {
                Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
            }
        }

        final boolean isNewReceipt = mReceipt == null;

        final ReceiptBuilderFactory builderFactory = (isNewReceipt) ? new ReceiptBuilderFactory(-1) : new ReceiptBuilderFactory(mReceipt);
        builderFactory.setName(name);
        builderFactory.setTrip(mTrip);
        builderFactory.setDate((Date) dateBox.date.clone());
        builderFactory.setPrice(priceBox.getText().toString());
        builderFactory.setTax(taxBox.getText().toString());
        builderFactory.setExchangeRate(new ExchangeRateBuilderFactory().setBaseCurrency(currency).setRate(mTrip.getTripCurrency(), exchangeRateBox.getText().toString()).build());
        builderFactory.setCategory(category);
        builderFactory.setCurrency(currency);
        builderFactory.setComment(commentBox.getText().toString());
        builderFactory.setPaymentMethod((PaymentMethod) (getPersistenceManager().getPreferences().getUsesPaymentMethods() ? paymentMethodsSpinner.getSelectedItem() : null));
        builderFactory.setIsExpenseable(expensable.isChecked());
        builderFactory.setIsFullPage(fullpage.isChecked());
        builderFactory.setExtraEditText1((extra_edittext_box_1 == null) ? null : extra_edittext_box_1.getText().toString());
        builderFactory.setExtraEditText2((extra_edittext_box_2 == null) ? null : extra_edittext_box_2.getText().toString());
        builderFactory.setExtraEditText3((extra_edittext_box_3 == null) ? null : extra_edittext_box_3.getText().toString());


        if (isNewReceipt) {
            builderFactory.setFile(mFile);
            getWorkerManager().getLogger().logEvent(ReceiptCreateEditFragment.this, "Insert_Receipt");
            getPersistenceManager().getDatabase().insertReceiptParallel(builderFactory.build());
            getDateManager().setDateEditTextListenerDialogHolder(null);
        } else {
            getWorkerManager().getLogger().logEvent(ReceiptCreateEditFragment.this, "Update_Receipt");
            getPersistenceManager().getDatabase().updateReceiptParallel(mReceipt, builderFactory.build());
            getDateManager().setDateEditTextListenerDialogHolder(null);
        }

        mNavigationHandler.navigateToReportInfoFragment(mTrip);
    }

    private AdapterView.OnItemSelectedListener getSpinnerSelectionListener(TextView nameBox, TextView commentBox, ArrayAdapter<CharSequence> categories) {
        return new SpinnerSelectionListener(nameBox, commentBox, categories);
    }

    private final class SpinnerSelectionListener implements AdapterView.OnItemSelectedListener {

        private final TextView sNameBox, sCommentBox;
        private final ArrayAdapter<CharSequence> sCategories;

        public SpinnerSelectionListener(TextView nameBox, TextView commentBox, ArrayAdapter<CharSequence> categories) {
            sNameBox = nameBox;
            sCommentBox = commentBox;
            sCategories = categories;
        }

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            if (sNameBox != null) {
                sNameBox.setText(sCategories.getItem(position));
            }
            if (sCommentBox != null) {
                sCommentBox.setText(sCategories.getItem(position));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}
