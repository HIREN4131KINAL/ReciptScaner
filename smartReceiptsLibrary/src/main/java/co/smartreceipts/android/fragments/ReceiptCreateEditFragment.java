package co.smartreceipts.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.adapters.TaxAutoCompleteAdapter;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.apis.ExchangeRateServiceManager;
import co.smartreceipts.android.apis.MemoryLeakSafeCallback;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.ocr.info.tooltip.OcrInformationalTooltipFragment;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.purchases.PurchaseSource;
import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.NetworkRequestAwareEditText;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import co.smartreceipts.android.widget.UserSelectionTrackingOnItemSelectedListener;
import retrofit2.Call;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import wb.android.autocomplete.AutoCompleteAdapter;

public class ReceiptCreateEditFragment extends WBFragment implements View.OnFocusChangeListener, NetworkRequestAwareEditText.RetryListener, DatabaseHelper.ReceiptAutoCompleteListener {

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
    private CheckBox reimbursable;
    private CheckBox fullpage;
    private Spinner paymentMethodsSpinner;
    private EditText extra_edittext_box_1;
    private EditText extra_edittext_box_2;
    private EditText extra_edittext_box_3;
    private ViewGroup mPaymentMethodsContainer;
    private ViewGroup mExchangeRateContainer;
    private Toolbar mToolbar;
    private View mFocusedView;

    // Rx
    private rx.Subscription mIdSubscription;
    private TableEventsListener<Category> mCategoryTableEventsListener;
    private TableEventsListener<PaymentMethod> mPaymentMethodTableEventsListener;

    // Misc
    private MemoryLeakSafeCallback<ExchangeRate, EditText> mLastExchangeRateFetchCallback;
    private NavigationHandler mNavigationHandler;
    private ExchangeRateServiceManager mExchangeRateServiceManager;
    private ReceiptInputCache mReceiptInputCache;
    private AutoCompleteAdapter mReceiptsNameAutoCompleteAdapter, mReceiptsCommentAutoCompleteAdapter;
    private ArrayAdapter<CharSequence> mCurrenciesAdapter;
    private List<Category> mCategoriesList;
    private ArrayAdapter<Category> mCategoriesAdpater;
    private ArrayAdapter<PaymentMethod> mPaymentMethodsAdapter;

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
        mNavigationHandler = new NavigationHandler(getActivity(), new FragmentProvider());
        mExchangeRateServiceManager = new ExchangeRateServiceManager(getFragmentManager());
        mCurrenciesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCurrenciesList());
        mCategoriesList = Collections.emptyList();
        mCategoriesAdpater = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Collections.<Category>emptyList());
        mPaymentMethodsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Collections.<PaymentMethod>emptyList());
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.update_receipt, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        if (savedInstanceState == null) {
            if (mReceipt == null) {
                new ChildFragmentNavigationHandler(this).addChild(new OcrInformationalTooltipFragment(), R.id.update_receipt_tooltip);
            }
        }

        this.nameBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_NAME);
        this.priceBox = (EditText) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_PRICE);
        this.taxBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_TAX);
        this.currencySpinner = (Spinner) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CURRENCY);
        this.exchangeRateBox = (NetworkRequestAwareEditText) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXCHANGE_RATE);
        mExchangeRateContainer = (ViewGroup) getFlex().getSubView(getActivity(), rootView, R.id.exchange_rate_container);
        this.dateBox = (DateEditText) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_DATE);
        this.commentBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_COMMENT);
        this.categoriesSpinner = (Spinner) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CATEGORY);
        this.reimbursable = (CheckBox) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXPENSABLE);
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
        this.paymentMethodsSpinner.setOnFocusChangeListener(this);

        // Custom view properties
        exchangeRateBox.setFailedHint(R.string.DIALOG_RECEIPTMENU_HINT_EXCHANGE_RATE_FAILED);

        // Set click listeners
        View.OnTouchListener hideSoftKeyboardOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    SoftKeyboardManager.hideKeyboard(view);
                }
                return false;
            }
        };
        dateBox.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        categoriesSpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        currencySpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        paymentMethodsSpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);

        // Show default dictionary with auto-complete
        nameBox.setKeyListener(TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES));

        // Set-up tax layers
        if (getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.IncludeTaxField)) {
            priceBox.setHint(getFlexString(R.string.DIALOG_RECEIPTMENU_HINT_PRICE_SHORT));
            taxBox.setVisibility(View.VISIBLE);
        }

        // Configure dropdown defaults for currencies
        mCurrenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(mCurrenciesAdapter);

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
                configureExchangeRateField(baseCurrencyCode);
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
            if (getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.IncludeTaxField)) {
                taxBox.setAdapter(new TaxAutoCompleteAdapter(getActivity(), priceBox, taxBox, getPersistenceManager().getPreferenceManager(), getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.DefaultTaxPercentage)));
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
                    if (getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.ReceiptDateDefaultsToReportStartDate)) {
                        dateBox.date = mTrip.getStartDate();
                    } else {
                        dateBox.date = new Date(now.toMillis(false));
                    }
                } else {
                    dateBox.date = mReceiptInputCache.getCachedDate();
                }
                dateBox.setText(DateFormat.getDateFormat(getActivity()).format(dateBox.date));

                final UserPreferenceManager preferences = getPersistenceManager().getPreferenceManager();
                reimbursable.setChecked(preferences.get(UserPreference.Receipts.ReceiptsDefaultAsReimbursable));
                if (preferences.get(UserPreference.Receipts.MatchReceiptCommentToCategory) &&
                        preferences.get(UserPreference.Receipts.MatchReceiptNameToCategory)) {
                    if (mFocusedView == null) {
                        mFocusedView = priceBox;
                    }
                } else if (preferences.get(UserPreference.Receipts.MatchReceiptNameToCategory)) {
                    if (mFocusedView == null) {
                        mFocusedView = priceBox;
                    }
                }

                int idx = mCurrenciesAdapter.getPosition((mTrip != null) ? mTrip.getDefaultCurrencyCode() : preferences.get(UserPreference.General.DefaultCurrency));
                int cachedIdx = (mReceiptInputCache.getCachedCurrency() != null) ? mCurrenciesAdapter.getPosition(mReceiptInputCache.getCachedCurrency()) : -1;
                idx = (cachedIdx >= 0) ? cachedIdx : idx;
                if (idx >= 0) {
                    currencySpinner.setSelection(idx);
                }
                if (!mTrip.getDefaultCurrencyCode().equals(mReceiptInputCache.getCachedCurrency())) {
                    configureExchangeRateField(mReceiptInputCache.getCachedCurrency());
                }
                fullpage.setChecked(preferences.get(UserPreference.Receipts.DefaultToFullPage));

            } else {
                nameBox.setText(mReceipt.getName());
                priceBox.setText(mReceipt.getPrice().getDecimalFormattedPrice());
                dateBox.setText(mReceipt.getFormattedDate(getActivity(), getPersistenceManager().getPreferenceManager().get(UserPreference.General.DateSeparator)));
                dateBox.date = mReceipt.getDate();
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

                reimbursable.setChecked(mReceipt.isReimbursable());
                fullpage.setChecked(mReceipt.isFullPage());

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

        // Configure items that require callbacks
        mCategoryTableEventsListener = new StubTableEventsListener<Category>() {
            @Override
            public void onGetSuccess(@NonNull List<Category> list) {
                if (isAdded()) {
                    mCategoriesList = list;
                    mCategoriesAdpater = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
                    mCategoriesAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categoriesSpinner.setAdapter(mCategoriesAdpater);

                    if (mReceipt == null) {
                        final UserPreferenceManager preferences = getPersistenceManager().getPreferenceManager();
                        if (preferences.get(UserPreference.Receipts.MatchReceiptCommentToCategory) || preferences.get(UserPreference.Receipts.MatchReceiptNameToCategory)) {
                            categoriesSpinner.setOnItemSelectedListener(new SpinnerSelectionListener());
                        }

                        if (preferences.get(UserPreference.Receipts.PredictCategories)) { // Predict Breakfast, Lunch, Dinner by the hour
                            if (mReceiptInputCache.getCachedCategory() == null) {
                                final Time now = new Time();
                                now.setToNow();
                                String nameToIndex = null;
                                if (now.hour >= 4 && now.hour < 11) { // Breakfast hours
                                    nameToIndex = getString(R.string.category_breakfast);
                                } else if (now.hour >= 11 && now.hour < 16) { // Lunch hours
                                    nameToIndex = getString(R.string.category_lunch);
                                } else if (now.hour >= 16 && now.hour < 23) { // Dinner hours
                                    nameToIndex = getString(R.string.category_dinner);
                                }
                                if (nameToIndex != null) {
                                    for (int i = 0; i < mCategoriesAdpater.getCount(); i++) {
                                        if (nameToIndex.equals(mCategoriesAdpater.getItem(i).getName())) {
                                            categoriesSpinner.setSelection(i);
                                            break; // Exit loop now
                                        }
                                    }
                                }
                            } else {
                                int idx = mCategoriesAdpater.getPosition(mReceiptInputCache.getCachedCategory());
                                if (idx > 0) {
                                    categoriesSpinner.setSelection(idx);
                                }
                            }
                        }
                    } else {
                        categoriesSpinner.setSelection(mCategoriesAdpater.getPosition(mReceipt.getCategory()));
                    }
                }
            }
        };
        mPaymentMethodTableEventsListener = new StubTableEventsListener<PaymentMethod>() {
            @Override
            public void onGetSuccess(@NonNull List<PaymentMethod> list) {
                if (isAdded()) {
                    mPaymentMethodsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
                    mPaymentMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    paymentMethodsSpinner.setAdapter(mPaymentMethodsAdapter);
                    if (getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.UsePaymentMethods)) {
                        mPaymentMethodsContainer.setVisibility(View.VISIBLE);
                        if (mReceipt != null) {
                            final PaymentMethod oldPaymentMethod = mReceipt.getPaymentMethod();
                            if (oldPaymentMethod != null) {
                                final int paymentIdx = mPaymentMethodsAdapter.getPosition(oldPaymentMethod);
                                if (paymentIdx > 0) {
                                    paymentMethodsSpinner.setSelection(paymentIdx);
                                }
                            }
                        }
                    }
                }
            }
        };
        getSmartReceiptsApplication().getTableControllerManager().getCategoriesTableController().subscribe(mCategoryTableEventsListener);
        getSmartReceiptsApplication().getTableControllerManager().getPaymentMethodsTableController().subscribe(mPaymentMethodTableEventsListener);
        getSmartReceiptsApplication().getTableControllerManager().getCategoriesTableController().get();
        getSmartReceiptsApplication().getTableControllerManager().getPaymentMethodsTableController().get();
    }

    @Override
    public void onResume() {
        super.onResume();

        final boolean isNewReceipt = mReceipt == null;

        final String title;
        if (isNewReceipt) {
            title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW);
        } else {
            if (getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.ShowReceiptID)) {
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

        if (isNewReceipt && getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.ShowReceiptID)) {
            mIdSubscription = getPersistenceManager().getDatabase().getNextReceiptAutoIncremenetIdHelper()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer integer) {
                            if (isResumed()) {
                                final ActionBar actionBar = getSupportActionBar();
                                if (actionBar != null) {
                                    final String titleWithId = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW_ID), integer);
                                    actionBar.setTitle(titleWithId);
                                }
                            }
                        }
                    });
        }

        if (isNewReceipt) {
            if (getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.EnableAutoCompleteSuggestions)) {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationHandler.navigateToReportInfoFragment(mTrip);
            deleteReceiptFileIfUnused();
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
        if (mReceipt == null && hasFocus) {
            // Only launch if we have focus and it's a new receipt
            SoftKeyboardManager.showKeyboard(v);
        }
    }

    @Override
    public void onUserRetry() {
        if (getPersistenceManager().getSubscriptionCache().getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPlus)) {
            Logger.info(this, "Attempting to retry with valid subscription. Submitting request directly");
            submitExchangeRateRequest((String) currencySpinner.getSelectedItem());
        } else {
            Logger.info(this, "Attempting to retry without valid subscription. Directing user to purchase intent");
            final Activity activity = getActivity();
            if (activity instanceof SmartReceiptsActivity) {
                final SmartReceiptsActivity smartReceiptsActivity = (SmartReceiptsActivity) activity;
                final SubscriptionManager subscriptionManager = smartReceiptsActivity.getSubscriptionManager();
                if (subscriptionManager != null) {
                    subscriptionManager.queryBuyIntent(Subscription.SmartReceiptsPlus, PurchaseSource.ExchangeRate);
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
                for (int i = 0; i < mCategoriesList.size(); i++) {
                    if (category.equals(mCategoriesList.get(i).getName())) {
                        categoriesSpinner.setSelection(mCategoriesList.indexOf(mCategoriesList.get(i)));
                        break;
                    }
                }
            }
        }
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
        if (mIdSubscription != null) {
            mIdSubscription.unsubscribe();
            mIdSubscription = null;
        }

        // Dismiss the soft keyboard
        SoftKeyboardManager.hideKeyboard(mFocusedView);

        exchangeRateBox.setRetryListener(null);
        getPersistenceManager().getDatabase().unregisterReceiptAutoCompleteListener();
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        if (mExchangeRateContainer != null && outState != null) {
            outState.putBoolean(KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE, mExchangeRateContainer.getVisibility() == View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        getSmartReceiptsApplication().getTableControllerManager().getCategoriesTableController().unsubscribe(mCategoryTableEventsListener);
        getSmartReceiptsApplication().getTableControllerManager().getPaymentMethodsTableController().unsubscribe(mPaymentMethodTableEventsListener);
        super.onDestroy();
    }

    private void configureExchangeRateField(@Nullable String baseCurrencyCode) {
        final String exchangeRateCurrencyCode = mTrip.getDefaultCurrencyCode();
        if (exchangeRateCurrencyCode.equals(baseCurrencyCode) || baseCurrencyCode == null) {
            mExchangeRateContainer.setVisibility(View.GONE);
            exchangeRateBox.setText(""); // Clear out if we're hiding the box
        } else {
            mExchangeRateContainer.setVisibility(View.VISIBLE);
            submitExchangeRateRequest(baseCurrencyCode);
        }
    }

    private synchronized void submitExchangeRateRequest(@NonNull String baseCurrencyCode) {
        exchangeRateBox.setText(""); // Clear results to avoid stale data here
        if (getPersistenceManager().getSubscriptionCache().getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPlus)) {
            Logger.info(this, "Submitting exchange rate request");
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.RequestExchangeRate);
            final String exchangeRateCurrencyCode = mTrip.getDefaultCurrencyCode();
            exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Loading);
            if (mLastExchangeRateFetchCallback != null) {
                // Ignore any outstanding results to not confuse ourselves
                mLastExchangeRateFetchCallback.ignoreResult();
            }
            mLastExchangeRateFetchCallback = new MemoryLeakSafeCallback<ExchangeRate, EditText>(exchangeRateBox) {
                @Override
                public void success(EditText editText, Call<ExchangeRate> call, Response<ExchangeRate> response) {
                    final ExchangeRate exchangeRate = response.body();
                    if (exchangeRate != null && exchangeRate.supportsExchangeRateFor(exchangeRateCurrencyCode)) {
                        getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.RequestExchangeRateSuccess);
                        if (TextUtils.isEmpty(editText.getText())) {
                            editText.setText(exchangeRate.getDecimalFormattedExchangeRate(exchangeRateCurrencyCode));
                        } else {
                            Logger.warn(ReceiptCreateEditFragment.this, "User already started typing... Ignoring exchange rate result");
                        }
                        exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Success);
                    } else {
                        Logger.error(ReceiptCreateEditFragment.this, "Received a null exchange rate");
                        getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.RequestExchangeRateFailedWithNull);
                        exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Failure);
                    }
                }

                @Override
                public void failure(EditText editText, Call<ExchangeRate> call, Throwable th) {
                    Logger.error(ReceiptCreateEditFragment.this, th);
                    getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.RequestExchangeRateFailed);
                    exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Failure);
                }
            };
            mExchangeRateServiceManager.getService().getExchangeRate(dateBox.date, getString(R.string.exchange_rate_key), baseCurrencyCode).enqueue(mLastExchangeRateFetchCallback);
        } else {
            exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Ready);
            Logger.info(this, "Ignoring exchange rate request, since there is no subscription for it");
        }
    }

    private void saveReceipt() {
        final String name = TextUtils.isEmpty(nameBox.getText().toString()) ? "" : nameBox.getText().toString();
        final Category category = mCategoriesAdpater.getItem(categoriesSpinner.getSelectedItemPosition());
        final String currency = currencySpinner.getSelectedItem().toString();

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
        builderFactory.setPaymentMethod((PaymentMethod) (getPersistenceManager().getPreferenceManager().get(UserPreference.Receipts.UsePaymentMethods) ? paymentMethodsSpinner.getSelectedItem() : null));
        builderFactory.setIsReimbursable(reimbursable.isChecked());
        builderFactory.setIsFullPage(fullpage.isChecked());
        builderFactory.setExtraEditText1((extra_edittext_box_1 == null) ? null : extra_edittext_box_1.getText().toString());
        builderFactory.setExtraEditText2((extra_edittext_box_2 == null) ? null : extra_edittext_box_2.getText().toString());
        builderFactory.setExtraEditText3((extra_edittext_box_3 == null) ? null : extra_edittext_box_3.getText().toString());


        if (isNewReceipt) {
            builderFactory.setFile(mFile);
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.PersistNewReceipt);
            getSmartReceiptsApplication().getTableControllerManager().getReceiptTableController().insert(builderFactory.build(), new DatabaseOperationMetadata());
            getDateManager().setDateEditTextListenerDialogHolder(null);
        } else {
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.PersistUpdateReceipt);
            getSmartReceiptsApplication().getTableControllerManager().getReceiptTableController().update(mReceipt, builderFactory.build(), new DatabaseOperationMetadata());
            getDateManager().setDateEditTextListenerDialogHolder(null);
        }

        mNavigationHandler.navigateToReportInfoFragment(mTrip);
    }

    private void deleteReceiptFileIfUnused() {
        if (mReceipt == null && mFile != null) {
            if (mFile.delete()) {
                Logger.info(this, "Deleting receipt file as we're not saving it");
            }
        }
    }

    private class SpinnerSelectionListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            final UserPreferenceManager preferences = getPersistenceManager().getPreferenceManager();
            if (preferences.get(UserPreference.Receipts.MatchReceiptNameToCategory)) {
                nameBox.setText(mCategoriesAdpater.getItem(position).getName());
            }
            if (preferences.get(UserPreference.Receipts.MatchReceiptCommentToCategory)) {
                commentBox.setText(mCategoriesAdpater.getItem(position).getName());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}
