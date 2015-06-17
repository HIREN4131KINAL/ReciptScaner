package co.smartreceipts.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.method.TextKeyListener;
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

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.TaxAutoCompleteAdapter;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.widget.HideSoftKeyboardOnTouchListener;
import co.smartreceipts.android.widget.ShowSoftKeyboardOnFocusChangeListener;
import wb.android.autocomplete.AutoCompleteAdapter;

public class ReceiptCreateEditFragment extends WBFragment implements View.OnFocusChangeListener {

    private static final String ARG_FILE = "arg_file";

    // Metadata
    private Trip mTrip;
    private Receipt mReceipt;
    private File mFile;

    // Views
    private AutoCompleteTextView nameBox;
    private EditText priceBox;
    private AutoCompleteTextView taxBox;
    private Spinner currencySpinner;
    private EditText exchangeRateBox;
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
    private NavigationHandler mNavigationHandler;
    private ReceiptInputCache mReceiptInputCache;
    private AutoCompleteAdapter mReceiptsNameAutoCompleteAdapter, mReceiptsCommentAutoCompleteAdapter;
    private Time mNow;

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
        this.nameBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_NAME);
        this.priceBox = (EditText) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_PRICE);
        this.taxBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_TAX);
        this.currencySpinner = (Spinner) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CURRENCY);
        this.exchangeRateBox = (EditText) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXCHANGE_RATE);
        mExchangeRateContainer = (ViewGroup) getFlex().getSubView(getActivity(), rootView, R.id.exchange_rate_container);
        this.dateBox = (DateEditText) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_DATE);
        this.commentBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_COMMENT);
        this.categoriesSpinner = (Spinner) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CATEGORY);
        this.expensable = (CheckBox) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXPENSABLE);
        this.fullpage = (CheckBox) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_FULLPAGE);
        this.paymentMethodsSpinner = (Spinner) getFlex().getSubView(getActivity(), rootView, R.id.dialog_receiptmenu_payment_methods_spinner);
        mPaymentMethodsContainer = (ViewGroup) getFlex().getSubView(getActivity(), rootView, R.id.payment_methods_container);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        // Extras
        final LinearLayout extras = (LinearLayout) getFlex().getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXTRAS);
        this.extra_edittext_box_1 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_1));
        this.extra_edittext_box_2 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_2));
        this.extra_edittext_box_3 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_3));

        // Set each focus listener, so we can track the focus view across resume -> pauses
        this.nameBox.setOnFocusChangeListener(this);
        this.priceBox.setOnFocusChangeListener(this);
        this.taxBox.setOnFocusChangeListener(this);
        this.currencySpinner.setOnFocusChangeListener(this);
        this.dateBox.setOnFocusChangeListener(this);
        this.commentBox.setOnFocusChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        final boolean isNewReceipt = mReceipt == null;

        final String title;
        if (isNewReceipt) {
            if (getPersistenceManager().getPreferences().isShowReceiptID()) {
                title = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW_ID), getPersistenceManager().getDatabase().getNextReceiptAutoIncremenetIdSerial());
            }
            else {
                title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW);
            }
        }
        else {
            if (getPersistenceManager().getPreferences().isShowReceiptID()) {
                title = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT_ID), mReceipt.getId());
            }
            else {
                title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT);
            }
        }

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
            actionBar.setTitle(title);
        }

        if (getPersistenceManager().getPreferences().includeTaxField()) {
            priceBox.setHint(getFlexString(R.string.DIALOG_RECEIPTMENU_HINT_PRICE_SHORT));
            taxBox.setVisibility(View.VISIBLE);
        }

        // Show default dictionary with auto-complete
        nameBox.setKeyListener(TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES));

        final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCurrenciesList());
        currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currenices);

        dateBox.setFocusableInTouchMode(false);
        dateBox.setOnClickListener(getDateManager().getDateEditTextListener());
        final ArrayAdapter<CharSequence> categories = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCategoriesList());
        categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(categories);


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
            if (getPersistenceManager().getPreferences().includeTaxField()) {
                taxBox.setAdapter(new TaxAutoCompleteAdapter(getActivity(), priceBox, taxBox, getPersistenceManager().getPreferences(), getPersistenceManager().getPreferences().getDefaultTaxPercentage()));
            }
            mNow = new Time();
            mNow.setToNow();
            if (mReceiptInputCache.getCachedDate() == null) {
                if (getPersistenceManager().getPreferences().defaultToFirstReportDate()) {
                    dateBox.date = mTrip.getStartDate();
                } else {
                    dateBox.date = new Date(mNow.toMillis(false));
                }
            } else {
                dateBox.date = mReceiptInputCache.getCachedDate();
            }
            dateBox.setText(DateFormat.getDateFormat(getActivity()).format(dateBox.date));
            expensable.setChecked(true);
            Preferences preferences = getPersistenceManager().getPreferences();
            if (preferences.matchCommentToCategory() && preferences.matchNameToCategory()) {
                categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(nameBox, commentBox, categories));
                if (mFocusedView == null) {
                    mFocusedView = priceBox;
                }
            } else if (preferences.matchCommentToCategory()) {
                categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(null, commentBox, categories));
            } else if (preferences.matchNameToCategory()) {
                categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(nameBox, null, categories));
                if (mFocusedView == null) {
                    mFocusedView = priceBox;
                }
            }
            if (preferences.predictCategories()) { // Predict Breakfast, Lunch, Dinner by the hour
                if (mReceiptInputCache.getCachedCategory() == null) {
                    if (mNow.hour >= 4 && mNow.hour < 11) { // Breakfast hours
                        int idx = categories.getPosition(getString(R.string.category_breakfast));
                        if (idx > 0) {
                            categoriesSpinner.setSelection(idx);
                        }
                    } else if (mNow.hour >= 11 && mNow.hour < 16) { // Lunch hours
                        int idx = categories.getPosition(getString(R.string.category_lunch));
                        if (idx > 0) {
                            categoriesSpinner.setSelection(idx);
                        }
                    } else if (mNow.hour >= 16 && mNow.hour < 23) { // Dinner hours
                        int idx = categories.getPosition(getString(R.string.category_dinner));
                        if (idx > 0) {
                            categoriesSpinner.setSelection(idx);
                        }
                    }
                } else {
                    int idx = categories.getPosition(mReceiptInputCache.getCachedCategory());
                    if (idx > 0) {
                        categoriesSpinner.setSelection(idx);
                    }
                }
            }
            int idx = currenices.getPosition((mTrip != null) ? mTrip.getDefaultCurrencyCode() : preferences.getDefaultCurreny());
            int cachedIdx = (mReceiptInputCache.getCachedCurrency() != null) ? currenices.getPosition(mReceiptInputCache.getCachedCurrency()) : -1;
            idx = (cachedIdx > 0) ? cachedIdx : idx;
            if (idx > 0) {
                currencySpinner.setSelection(idx);
            }
            fullpage.setChecked(preferences.shouldDefaultToFullPage());
            if (getPersistenceManager().getPreferences().getUsesPaymentMethods()) {
                mPaymentMethodsContainer.setVisibility(View.VISIBLE);
                final ArrayAdapter<PaymentMethod> paymentMethodsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getPaymentMethods());
                paymentMethodsSpinner.setVisibility(View.VISIBLE);
                paymentMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                paymentMethodsSpinner.setAdapter(paymentMethodsAdapter);
            }
        } else {
            nameBox.setText(mReceipt.getName());
            priceBox.setText(mReceipt.getPrice().getDecimalFormattedPrice());
            dateBox.setText(mReceipt.getFormattedDate(getActivity(), getPersistenceManager().getPreferences().getDateSeparator()));
            dateBox.date = mReceipt.getDate();
            categoriesSpinner.setSelection(categories.getPosition(mReceipt.getCategory()));
            commentBox.setText(mReceipt.getComment());
            taxBox.setText(mReceipt.getTax().getDecimalFormattedPrice());
            final ExchangeRate exchangeRate = mReceipt.getPrice().getExchangeRate();
            if (exchangeRate.supportsExchangeRateFor(mTrip.getDefaultCurrencyCode())) {
                exchangeRateBox.setText(exchangeRate.getDecimalFormattedExchangeRate(mTrip.getDefaultCurrencyCode()));
            }


            int idx = currenices.getPosition(mReceipt.getPrice().getCurrencyCode());
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
                final ArrayAdapter<PaymentMethod> paymentMethodsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getPaymentMethods());
                paymentMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                mPaymentMethodsContainer.setVisibility(View.VISIBLE);
                final PaymentMethod oldPaymentMethod = mReceipt.getPaymentMethod();
                paymentMethodsSpinner.setAdapter(paymentMethodsAdapter);

                if (oldPaymentMethod != null) {
                    final int paymentIdx = paymentMethodsAdapter.getPosition(oldPaymentMethod);
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

        mFocusedView.requestFocus(); // Make sure we're focused on the right view

        dateBox.setOnTouchListener(new HideSoftKeyboardOnTouchListener());
        categoriesSpinner.setOnTouchListener(new HideSoftKeyboardOnTouchListener());
        currencySpinner.setOnTouchListener(new HideSoftKeyboardOnTouchListener());
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currenices.getItem(position).equals(mTrip.getDefaultCurrencyCode())) {
                    mExchangeRateContainer.setVisibility(View.GONE);
                } else {
                    mExchangeRateContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Intentional no-op
            }
        });

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
        if (mFocusedView != null) {
            final InputMethodManager inputMethodManager = (InputMethodManager) mFocusedView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(mFocusedView.getWindowToken(), 0);
            }
        }
        super.onPause();
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
        if (hasFocus && mReceipt == null) {
            // Only launch if we have focus and it's a new receipt
            new ShowSoftKeyboardOnFocusChangeListener().onFocusChange(v, hasFocus);
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
            getPersistenceManager().getDatabase().insertReceiptParallel(mTrip, mFile, name, category, dateBox.date, comment, price, tax, expensable.isChecked(), currency, fullpage.isChecked(), paymentMethod, extra_edittext_1, extra_edittext_2, extra_edittext_3);
            getDateManager().setDateEditTextListenerDialogHolder(null);
        } else {
            getWorkerManager().getLogger().logEvent(ReceiptCreateEditFragment.this, "Update_Receipt");
            getPersistenceManager().getDatabase().updateReceiptParallel(mReceipt, mTrip, name, category, (dateBox.date == null) ? mReceipt.getDate() : dateBox.date, comment, price, tax, expensable.isChecked(), currency, fullpage.isChecked(), paymentMethod, extra_edittext_1, extra_edittext_2, extra_edittext_3);
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
