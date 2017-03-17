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
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.adapters.TaxAutoCompleteAdapter;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.apis.ExchangeRateServiceManager;
import co.smartreceipts.android.apis.MemoryLeakSafeCallback;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.date.DateManager;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.info.tooltip.OcrInformationalTooltipFragment;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.NetworkRequestAwareEditText;
import co.smartreceipts.android.widget.UserSelectionTrackingOnItemSelectedListener;
import dagger.android.support.AndroidSupportInjection;
import retrofit2.Call;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.flex.Flex;

public class ReceiptCreateEditFragment extends WBFragment implements View.OnFocusChangeListener, NetworkRequestAwareEditText.RetryListener, DatabaseHelper.ReceiptAutoCompleteListener {

    private static final String ARG_FILE = "arg_file";
    private static final String ARG_OCR = "arg_ocr";
    private static final String KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE = "key_is_exchange_rate_visible";

    @Inject
    Flex flex;

    @Inject
    DateManager dateManager;

    @Inject
    PersistenceManager persistenceManager;

    // Metadata
    private Trip trip;
    private Receipt receipt;
    private File file;
    private OcrResponse ocrResponse;

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
    private ViewGroup paymentMethodsContainer;
    private ViewGroup exchangeRateContainer;
    private Toolbar toolbar;
    private View focusedView;

    // Rx
    private rx.Subscription idSubscription;
    private TableEventsListener<Category> categoryTableEventsListener;
    private TableEventsListener<PaymentMethod> paymentMethodTableEventsListener;

    // Misc
    private MemoryLeakSafeCallback<ExchangeRate, EditText> lastExchangeRateFetchCallback;
    private NavigationHandler navigationHandler;
    private ExchangeRateServiceManager exchangeRateServiceManager;
    private ReceiptInputCache receiptInputCache;
    private AutoCompleteAdapter receiptsNameAutoCompleteAdapter, receiptsCommentAutoCompleteAdapter;
    private ArrayAdapter<CharSequence> currenciesAdapter;
    private List<Category> categoriesList;
    private ArrayAdapter<Category> categoriesAdpater;
    private ArrayAdapter<PaymentMethod> paymentMethodsAdapter;

    /**
     * Creates a new instance of this fragment for a new receipt
     *
     * @param trip - the parent trip of this receipt
     * @param file - the file associated with this receipt or null if we do not have one
     * @return the new instance of this fragment
     */
    public static ReceiptCreateEditFragment newInstance(@NonNull Trip trip, @Nullable File file, @Nullable OcrResponse ocrResponse) {
        return newInstance(trip, null, file, ocrResponse);
    }

    /**
     * Creates a new instance of this fragment to edit an existing receipt
     *
     * @param trip          - the parent trip of this receipt
     * @param receiptToEdit - the receipt to edit
     * @return the new instance of this fragment
     */
    public static ReceiptCreateEditFragment newInstance(@NonNull Trip trip, @NonNull Receipt receiptToEdit) {
        return newInstance(trip, receiptToEdit, null, null);
    }

    private static ReceiptCreateEditFragment newInstance(@NonNull Trip trip, @Nullable Receipt receiptToEdit, @Nullable File file, @Nullable OcrResponse ocrResponse) {
        final ReceiptCreateEditFragment fragment = new ReceiptCreateEditFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);
        args.putParcelable(Receipt.PARCEL_KEY, receiptToEdit);
        args.putSerializable(ARG_FILE, file);
        args.putSerializable(ARG_OCR, ocrResponse);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trip = getArguments().getParcelable(Trip.PARCEL_KEY);
        receipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        file = (File) getArguments().getSerializable(ARG_FILE);
        ocrResponse = (OcrResponse) getArguments().getSerializable(ARG_OCR);
        receiptInputCache = new ReceiptInputCache(getFragmentManager());
        navigationHandler = new NavigationHandler(getActivity(), new FragmentProvider());
        exchangeRateServiceManager = new ExchangeRateServiceManager(getFragmentManager());
        currenciesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                persistenceManager.getDatabase().getCurrenciesList());
        categoriesList = Collections.emptyList();
        categoriesAdpater = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Collections.<Category>emptyList());
        paymentMethodsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Collections.<PaymentMethod>emptyList());
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
            if (receipt == null) {
                new ChildFragmentNavigationHandler(this).addChild(new OcrInformationalTooltipFragment(), R.id.update_receipt_tooltip);
            }
        }

        this.nameBox = (AutoCompleteTextView) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_NAME);
        this.priceBox = (EditText) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_PRICE);
        this.taxBox = (AutoCompleteTextView) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_TAX);
        this.currencySpinner = (Spinner) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CURRENCY);
        this.exchangeRateBox = (NetworkRequestAwareEditText) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXCHANGE_RATE);
        exchangeRateContainer = (ViewGroup) flex.getSubView(getActivity(), rootView, R.id.exchange_rate_container);
        this.dateBox = (DateEditText) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_DATE);
        this.commentBox = (AutoCompleteTextView) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_COMMENT);
        this.categoriesSpinner = (Spinner) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CATEGORY);
        this.reimbursable = (CheckBox) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXPENSABLE);
        this.fullpage = (CheckBox) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_FULLPAGE);
        this.paymentMethodsSpinner = (Spinner) flex.getSubView(getActivity(), rootView, R.id.dialog_receiptmenu_payment_methods_spinner);
        paymentMethodsContainer = (ViewGroup) flex.getSubView(getActivity(), rootView, R.id.payment_methods_container);

        // Extras
        final LinearLayout extras = (LinearLayout) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXTRAS);
        this.extra_edittext_box_1 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_1));
        this.extra_edittext_box_2 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_2));
        this.extra_edittext_box_3 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_3));

        // Toolbar stuff
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (navigationHandler.isDualPane()) {
            toolbar.setVisibility(View.GONE);
        } else {
            setSupportActionBar(toolbar);
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
        if (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.IncludeTaxField)) {
            priceBox.setHint(getFlexString(R.string.DIALOG_RECEIPTMENU_HINT_PRICE_SHORT));
            taxBox.setVisibility(View.VISIBLE);
        }

        // Configure dropdown defaults for currencies
        currenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currenciesAdapter);

        // And the exchange rate processing for our currencies
        final boolean exchangeRateIsVisible = savedInstanceState != null && savedInstanceState.getBoolean(KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE);
        if (exchangeRateIsVisible) {
            // Note: the restoration of selected spinner items (in the currency spinner) is delayed so we use this state tracker to restore immediately
            exchangeRateContainer.setVisibility(View.VISIBLE);
        }
        currencySpinner.setOnItemSelectedListener(new UserSelectionTrackingOnItemSelectedListener() {

            @Override
            public void onUserSelectedNewItem(AdapterView<?> parent, View view, int position, long id, int previousPosition) {
                // Then determine if we should show/hide the box
                final String baseCurrencyCode = currenciesAdapter.getItem(position).toString();
                configureExchangeRateField(baseCurrencyCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Intentional no-op
            }
        });

        // Outline date defaults
        dateBox.setFocusableInTouchMode(false);
        dateBox.setOnClickListener(dateManager.getDateEditTextListener());

        // Lastly, preset adapters for "new" receipts
        final boolean isNewReceipt = receipt == null;
        if (isNewReceipt) {
            if (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.IncludeTaxField)) {
                taxBox.setAdapter(new TaxAutoCompleteAdapter(getActivity(), priceBox, taxBox,
                        persistenceManager.getPreferenceManager(),
                        persistenceManager.getPreferenceManager().get(UserPreference.Receipts.DefaultTaxPercentage)));
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Configure things if it's not a restored fragment
        if (savedInstanceState == null) {
            final boolean isNewReceipt = receipt == null;
            if (isNewReceipt) {

                final Time now = new Time();
                now.setToNow();
                if (receiptInputCache.getCachedDate() == null) {
                    if (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.ReceiptDateDefaultsToReportStartDate)) {
                        dateBox.date = trip.getStartDate();
                    } else {
                        dateBox.date = new Date(now.toMillis(false));
                    }
                } else {
                    dateBox.date = receiptInputCache.getCachedDate();
                }
                dateBox.setText(DateFormat.getDateFormat(getActivity()).format(dateBox.date));

                final UserPreferenceManager preferences = persistenceManager.getPreferenceManager();
                reimbursable.setChecked(preferences.get(UserPreference.Receipts.ReceiptsDefaultAsReimbursable));
                if (preferences.get(UserPreference.Receipts.MatchReceiptCommentToCategory) &&
                        preferences.get(UserPreference.Receipts.MatchReceiptNameToCategory)) {
                    if (focusedView == null) {
                        focusedView = priceBox;
                    }
                } else if (preferences.get(UserPreference.Receipts.MatchReceiptNameToCategory)) {
                    if (focusedView == null) {
                        focusedView = priceBox;
                    }
                }

                int idx = currenciesAdapter.getPosition((trip != null) ? trip.getDefaultCurrencyCode() : preferences.get(UserPreference.General.DefaultCurrency));
                int cachedIdx = (receiptInputCache.getCachedCurrency() != null) ? currenciesAdapter.getPosition(receiptInputCache.getCachedCurrency()) : -1;
                idx = (cachedIdx >= 0) ? cachedIdx : idx;
                if (idx >= 0) {
                    currencySpinner.setSelection(idx);
                }
                if (!trip.getDefaultCurrencyCode().equals(receiptInputCache.getCachedCurrency())) {
                    configureExchangeRateField(receiptInputCache.getCachedCurrency());
                }
                fullpage.setChecked(preferences.get(UserPreference.Receipts.DefaultToFullPage));

                if (ocrResponse != null) {
                    if (ocrResponse.getMerchant() != null && ocrResponse.getMerchant().getName() != null) {
                        nameBox.setText(ocrResponse.getMerchant().getName());
                    }
                    if (ocrResponse.getTotalAmount() != null && ocrResponse.getTotalAmount().getData() != null) {
                        priceBox.setText(ModelUtils.getDecimalFormattedValue(new BigDecimal(ocrResponse.getTotalAmount().getData())));
                    }
                    if (ocrResponse.getTaxAmount() != null && ocrResponse.getTaxAmount().getData() != null) {
                        taxBox.setText(ModelUtils.getDecimalFormattedValue(new BigDecimal(ocrResponse.getTaxAmount().getData())));
                    }
                    if (ocrResponse.getDate() != null && ocrResponse.getDate().getData() != null) {
                        try {
                            TimeZone utc = TimeZone.getTimeZone("UTC");
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                            f.setTimeZone(utc);
                            GregorianCalendar cal = new GregorianCalendar(utc);
                            cal.setTime(f.parse(ocrResponse.getDate().getData()));
                            dateBox.date = new Date(cal.getTime().getTime());
                            dateBox.setText(DateFormat.getDateFormat(getActivity()).format(dateBox.date));
                        } catch (ParseException e) {
                            Logger.error(this, "Failed to parse OCR Date.", e);
                        }
                    }
                }

            } else {
                nameBox.setText(receipt.getName());
                priceBox.setText(receipt.getPrice().getDecimalFormattedPrice());
                dateBox.setText(receipt.getFormattedDate(getActivity(), persistenceManager.getPreferenceManager().get(UserPreference.General.DateSeparator)));
                dateBox.date = receipt.getDate();
                commentBox.setText(receipt.getComment());
                taxBox.setText(receipt.getTax().getDecimalFormattedPrice());

                final ExchangeRate exchangeRate = receipt.getPrice().getExchangeRate();
                if (exchangeRate.supportsExchangeRateFor(trip.getDefaultCurrencyCode())) {
                    exchangeRateBox.setText(exchangeRate.getDecimalFormattedExchangeRate(trip.getDefaultCurrencyCode()));
                }

                int idx = currenciesAdapter.getPosition(receipt.getPrice().getCurrencyCode());
                if (idx > 0) {
                    currencySpinner.setSelection(idx);
                }

                if (receipt.getPrice().getCurrency().equals(trip.getPrice().getCurrency())) {
                    exchangeRateContainer.setVisibility(View.GONE);
                } else {
                    exchangeRateContainer.setVisibility(View.VISIBLE);
                }

                reimbursable.setChecked(receipt.isReimbursable());
                fullpage.setChecked(receipt.isFullPage());

                if (extra_edittext_box_1 != null && receipt.hasExtraEditText1()) {
                    extra_edittext_box_1.setText(receipt.getExtraEditText1());
                }
                if (extra_edittext_box_2 != null && receipt.hasExtraEditText2()) {
                    extra_edittext_box_2.setText(receipt.getExtraEditText2());
                }
                if (extra_edittext_box_3 != null && receipt.hasExtraEditText3()) {
                    extra_edittext_box_3.setText(receipt.getExtraEditText3());
                }
            }

            // Focused View
            if (focusedView == null) {
                focusedView = nameBox;
            }

        }

        // Configure items that require callbacks
        categoryTableEventsListener = new StubTableEventsListener<Category>() {
            @Override
            public void onGetSuccess(@NonNull List<Category> list) {
                if (isAdded()) {
                    categoriesList = list;
                    categoriesAdpater = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
                    categoriesAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categoriesSpinner.setAdapter(categoriesAdpater);

                    if (receipt == null) {
                        final UserPreferenceManager preferences = persistenceManager.getPreferenceManager();
                        if (preferences.get(UserPreference.Receipts.MatchReceiptCommentToCategory) || preferences.get(UserPreference.Receipts.MatchReceiptNameToCategory)) {
                            categoriesSpinner.setOnItemSelectedListener(new SpinnerSelectionListener());
                        }

                        if (preferences.get(UserPreference.Receipts.PredictCategories)) { // Predict Breakfast, Lunch, Dinner by the hour
                            if (receiptInputCache.getCachedCategory() == null) {
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
                                    for (int i = 0; i < categoriesAdpater.getCount(); i++) {
                                        if (nameToIndex.equals(categoriesAdpater.getItem(i).getName())) {
                                            categoriesSpinner.setSelection(i);
                                            break; // Exit loop now
                                        }
                                    }
                                }
                            } else {
                                int idx = categoriesAdpater.getPosition(receiptInputCache.getCachedCategory());
                                if (idx > 0) {
                                    categoriesSpinner.setSelection(idx);
                                }
                            }
                        }
                    } else {
                        categoriesSpinner.setSelection(categoriesAdpater.getPosition(receipt.getCategory()));
                    }
                }
            }
        };
        paymentMethodTableEventsListener = new StubTableEventsListener<PaymentMethod>() {
            @Override
            public void onGetSuccess(@NonNull List<PaymentMethod> list) {
                if (isAdded()) {
                    paymentMethodsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
                    paymentMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    paymentMethodsSpinner.setAdapter(paymentMethodsAdapter);
                    if (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.UsePaymentMethods)) {
                        paymentMethodsContainer.setVisibility(View.VISIBLE);
                        if (receipt != null) {
                            final PaymentMethod oldPaymentMethod = receipt.getPaymentMethod();
                            if (oldPaymentMethod != null) {
                                final int paymentIdx = paymentMethodsAdapter.getPosition(oldPaymentMethod);
                                if (paymentIdx > 0) {
                                    paymentMethodsSpinner.setSelection(paymentIdx);
                                }
                            }
                        }
                    }
                }
            }
        };
        getSmartReceiptsApplication().getTableControllerManager().getCategoriesTableController().subscribe(categoryTableEventsListener);
        getSmartReceiptsApplication().getTableControllerManager().getPaymentMethodsTableController().subscribe(paymentMethodTableEventsListener);
        getSmartReceiptsApplication().getTableControllerManager().getCategoriesTableController().get();
        getSmartReceiptsApplication().getTableControllerManager().getPaymentMethodsTableController().get();
    }

    @Override
    public void onResume() {
        super.onResume();

        final boolean isNewReceipt = receipt == null;

        final String title;
        if (isNewReceipt) {
            title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW);
        } else {
            if (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.ShowReceiptID)) {
                title = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT_ID), receipt.getId());
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

        if (isNewReceipt && persistenceManager.getPreferenceManager().get(UserPreference.Receipts.ShowReceiptID)) {
            idSubscription = persistenceManager.getDatabase().getNextReceiptAutoIncremenetIdHelper()
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
            if (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.EnableAutoCompleteSuggestions)) {
                final DatabaseHelper db = persistenceManager.getDatabase();
                if (receiptsNameAutoCompleteAdapter == null) {
                    receiptsNameAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_RECEIPTS_NAME, db, db);
                } else {
                    receiptsNameAutoCompleteAdapter.reset();
                }
                if (receiptsCommentAutoCompleteAdapter == null) {
                    receiptsCommentAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_RECEIPTS_COMMENT, db);
                } else {
                    receiptsCommentAutoCompleteAdapter.reset();
                }
                nameBox.setAdapter(receiptsNameAutoCompleteAdapter);
                commentBox.setAdapter(receiptsCommentAutoCompleteAdapter);
            }
        }

        if (focusedView != null) {
            focusedView.requestFocus(); // Make sure we're focused on the right view
        }

        exchangeRateBox.setRetryListener(this);
        persistenceManager.getDatabase().registerReceiptAutoCompleteListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigationHandler.navigateToReportInfoFragment(trip);
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
        focusedView = hasFocus ? v : null;
        if (receipt == null && hasFocus) {
            // Only launch if we have focus and it's a new receipt
            SoftKeyboardManager.showKeyboard(v);
        }
    }

    @Override
    public void onUserRetry() {
        if (getSmartReceiptsApplication().getPurchaseWallet().hasSubscription(Subscription.SmartReceiptsPlus)) {
            Logger.info(this, "Attempting to retry with valid subscription. Submitting request directly");
            submitExchangeRateRequest((String) currencySpinner.getSelectedItem());
        } else {
            Logger.info(this, "Attempting to retry without valid subscription. Directing user to purchase intent");
            final Activity activity = getActivity();
            if (activity instanceof SmartReceiptsActivity) {
                final SmartReceiptsActivity smartReceiptsActivity = (SmartReceiptsActivity) activity;
                final PurchaseManager purchaseManager = smartReceiptsActivity.getSubscriptionManager();
                if (purchaseManager != null) {
                    purchaseManager.queryBuyIntent(Subscription.SmartReceiptsPlus, PurchaseSource.ExchangeRate);
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
                for (int i = 0; i < categoriesList.size(); i++) {
                    if (category.equals(categoriesList.get(i).getName())) {
                        categoriesSpinner.setSelection(categoriesList.indexOf(categoriesList.get(i)));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        // Notify the downstream adapters
        if (receiptsNameAutoCompleteAdapter != null) {
            receiptsNameAutoCompleteAdapter.onPause();
        }
        if (receiptsCommentAutoCompleteAdapter != null) {
            receiptsCommentAutoCompleteAdapter.onPause();
        }
        if (idSubscription != null) {
            idSubscription.unsubscribe();
            idSubscription = null;
        }

        // Dismiss the soft keyboard
        SoftKeyboardManager.hideKeyboard(focusedView);

        exchangeRateBox.setRetryListener(null);
        persistenceManager.getDatabase().unregisterReceiptAutoCompleteListener();
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        if (exchangeRateContainer != null && outState != null) {
            outState.putBoolean(KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE, exchangeRateContainer.getVisibility() == View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        getSmartReceiptsApplication().getTableControllerManager().getCategoriesTableController().unsubscribe(categoryTableEventsListener);
        getSmartReceiptsApplication().getTableControllerManager().getPaymentMethodsTableController().unsubscribe(paymentMethodTableEventsListener);
        super.onDestroy();
    }

    private void configureExchangeRateField(@Nullable String baseCurrencyCode) {
        final String exchangeRateCurrencyCode = trip.getDefaultCurrencyCode();
        if (exchangeRateCurrencyCode.equals(baseCurrencyCode) || baseCurrencyCode == null) {
            exchangeRateContainer.setVisibility(View.GONE);
            exchangeRateBox.setText(""); // Clear out if we're hiding the box
        } else {
            exchangeRateContainer.setVisibility(View.VISIBLE);
            submitExchangeRateRequest(baseCurrencyCode);
        }
    }

    private synchronized void submitExchangeRateRequest(@NonNull String baseCurrencyCode) {
        exchangeRateBox.setText(""); // Clear results to avoid stale data here
        if (getSmartReceiptsApplication().getPurchaseWallet().hasSubscription(Subscription.SmartReceiptsPlus)) {
            Logger.info(this, "Submitting exchange rate request");
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.RequestExchangeRate);
            final String exchangeRateCurrencyCode = trip.getDefaultCurrencyCode();
            exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Loading);
            if (lastExchangeRateFetchCallback != null) {
                // Ignore any outstanding results to not confuse ourselves
                lastExchangeRateFetchCallback.ignoreResult();
            }
            lastExchangeRateFetchCallback = new MemoryLeakSafeCallback<ExchangeRate, EditText>(exchangeRateBox) {
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
            exchangeRateServiceManager.getService().getExchangeRate(dateBox.date, getString(R.string.exchange_rate_key), baseCurrencyCode).enqueue(lastExchangeRateFetchCallback);
        } else {
            exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Ready);
            Logger.info(this, "Ignoring exchange rate request, since there is no subscription for it");
        }
    }

    private void saveReceipt() {
        final String name = TextUtils.isEmpty(nameBox.getText().toString()) ? "" : nameBox.getText().toString();
        final Category category = categoriesAdpater.getItem(categoriesSpinner.getSelectedItemPosition());
        final String currency = currencySpinner.getSelectedItem().toString();

        if (dateBox.date == null) {
            Toast.makeText(getActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
            return;
        } else {
            receiptInputCache.setCachedDate((Date) dateBox.date.clone());
        }

        receiptInputCache.setCachedCategory(category);
        receiptInputCache.setCachedCurrency(currency);

        if (!trip.isDateInsideTripBounds(dateBox.date)) {
            if (isAdded()) {
                Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
            }
        }

        final boolean isNewReceipt = receipt == null;

        final ReceiptBuilderFactory builderFactory = (isNewReceipt) ? new ReceiptBuilderFactory(-1) : new ReceiptBuilderFactory(receipt);
        builderFactory.setName(name);
        builderFactory.setTrip(trip);
        builderFactory.setDate((Date) dateBox.date.clone());
        builderFactory.setPrice(priceBox.getText().toString());
        builderFactory.setTax(taxBox.getText().toString());
        builderFactory.setExchangeRate(new ExchangeRateBuilderFactory().setBaseCurrency(currency).setRate(trip.getTripCurrency(), exchangeRateBox.getText().toString()).build());
        builderFactory.setCategory(category);
        builderFactory.setCurrency(currency);
        builderFactory.setComment(commentBox.getText().toString());
        builderFactory.setPaymentMethod((PaymentMethod) (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.UsePaymentMethods) ? paymentMethodsSpinner.getSelectedItem() : null));
        builderFactory.setIsReimbursable(reimbursable.isChecked());
        builderFactory.setIsFullPage(fullpage.isChecked());
        builderFactory.setExtraEditText1((extra_edittext_box_1 == null) ? null : extra_edittext_box_1.getText().toString());
        builderFactory.setExtraEditText2((extra_edittext_box_2 == null) ? null : extra_edittext_box_2.getText().toString());
        builderFactory.setExtraEditText3((extra_edittext_box_3 == null) ? null : extra_edittext_box_3.getText().toString());


        if (isNewReceipt) {
            builderFactory.setFile(file);
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.PersistNewReceipt);
            getSmartReceiptsApplication().getTableControllerManager().getReceiptTableController().insert(builderFactory.build(), new DatabaseOperationMetadata());
            dateManager.setDateEditTextListenerDialogHolder(null);
        } else {
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.PersistUpdateReceipt);
            getSmartReceiptsApplication().getTableControllerManager().getReceiptTableController().update(receipt, builderFactory.build(), new DatabaseOperationMetadata());
            dateManager.setDateEditTextListenerDialogHolder(null);
        }

        navigationHandler.navigateToReportInfoFragment(trip);
    }

    private void deleteReceiptFileIfUnused() {
        if (receipt == null && file != null) {
            if (file.delete()) {
                Logger.info(this, "Deleting receipt file as we're not saving it");
            }
        }
    }

    private class SpinnerSelectionListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            final UserPreferenceManager preferences = persistenceManager.getPreferenceManager();
            if (preferences.get(UserPreference.Receipts.MatchReceiptNameToCategory)) {
                nameBox.setText(categoriesAdpater.getItem(position).getName());
            }
            if (preferences.get(UserPreference.Receipts.MatchReceiptCommentToCategory)) {
                commentBox.setText(categoriesAdpater.getItem(position).getName());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private String getFlexString(int id) {
        return getFlexString(flex, id);
    }

}
