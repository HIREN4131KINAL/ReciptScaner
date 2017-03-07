package co.smartreceipts.android.trips.editor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import wb.android.autocomplete.AutoCompleteAdapter;

public class TripCreateEditFragment extends WBFragment implements View.OnFocusChangeListener {

    private AutoCompleteTextView mNameBox;
    private DateEditText mStartBox;
    private Spinner mCurrencySpinner;
    private EditText mCommentBox;
    private AutoCompleteTextView mCostCenterBox;
    private DateEditText mEndBox;

    private View mFocusedView;

    private NavigationHandler mNavigationHandler;
    private AutoCompleteAdapter mNameAutoCompleteAdapter, mCostCenterAutoCompleteAdapter;
    private ArrayAdapter<CharSequence> mCurrencies;

    private TripCreateEditFragmentPresenter mPresenter;

    public static TripCreateEditFragment newInstance() {
        return new TripCreateEditFragment();
    }

    public static TripCreateEditFragment newInstance(Trip trip) {
        TripCreateEditFragment fragment = new TripCreateEditFragment();

        Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new TripCreateEditFragmentPresenter(this);
        mNavigationHandler = new NavigationHandler(getActivity(), new FragmentProvider());

        setHasOptionsMenu(true);
    }

    public Trip getTrip() {
        if (getArguments() != null) {
            return getArguments().getParcelable(Trip.PARCEL_KEY);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.update_trip, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillFields();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationHandler.navigateToHomeTripsFragment();
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            saveTripChanges();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
            actionBar.setTitle((getTrip() == null) ? getFlexString(R.string.DIALOG_TRIPMENU_TITLE_NEW) : getFlexString(R.string.DIALOG_TRIPMENU_TITLE_EDIT));
            actionBar.setSubtitle("");
        }

        if (mFocusedView != null) {
            mFocusedView.requestFocus(); // Make sure we're focused on the right view
        }
    }

    @Override
    public void onPause() {
        if (mNameAutoCompleteAdapter != null) {
            mNameAutoCompleteAdapter.onPause();
        }
        if (mCostCenterAutoCompleteAdapter != null) {
            mCostCenterAutoCompleteAdapter.onPause();
        }

        // Dismiss the soft keyboard
        SoftKeyboardManager.hideKeyboard(mFocusedView);

        super.onPause();
    }

    private void initViews(View rootView) {
//        final View rootView = getFlex().getView(getActivity(), R.layout.update_trip);
        mNameBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), rootView, R.id.dialog_tripmenu_name);
        mStartBox = (DateEditText) getFlex().getSubView(getActivity(), rootView, R.id.dialog_tripmenu_start);
        mEndBox = (DateEditText) getFlex().getSubView(getActivity(), rootView, R.id.dialog_tripmenu_end);
        mCurrencySpinner = (Spinner) getFlex().getSubView(getActivity(), rootView, R.id.dialog_tripmenu_currency);
        mCommentBox = (EditText) getFlex().getSubView(getActivity(), rootView, R.id.dialog_tripmenu_comment);

        mCostCenterBox = (AutoCompleteTextView) rootView.findViewById(R.id.dialog_tripmenu_cost_center);
        mCostCenterBox.setVisibility(getPersistenceManager().getPreferenceManager().get(UserPreference.General.IncludeCostCenter) ? View.VISIBLE : View.GONE);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (mNavigationHandler.isDualPane()) {
            toolbar.setVisibility(View.GONE);
        } else {
            setSupportActionBar(toolbar);
        }

        ArrayList<CharSequence> currenciesList = getPersistenceManager().getDatabase().getCurrenciesList();
        mCurrencies = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, currenciesList);
        mCurrencies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCurrencySpinner.setAdapter(mCurrencies);

        // Show default dictionary with auto-complete
        TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
        mNameBox.setKeyListener(input);

        setKeyboardRelatedListeners();
    }

    private void fillFields() {
        int currencySpinnerPosition;

        if (getTrip() == null) { // new trip

            PersistenceManager persistenceManager = getPersistenceManager();
            if (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.EnableAutoCompleteSuggestions)) {
                DatabaseHelper db = getPersistenceManager().getDatabase();
                mNameAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_TRIPS_NAME, db);
                mCostCenterAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_TRIPS_COST_CENTER, db);

                mNameBox.setAdapter(mNameAutoCompleteAdapter);
                mCostCenterBox.setAdapter(mCostCenterAutoCompleteAdapter);
            }

            mStartBox.setOnClickListener(getDateManager().getDurationDateEditTextListener(mEndBox));

            currencySpinnerPosition = mCurrencies.getPosition(getPersistenceManager().getPreferenceManager().get(UserPreference.General.DefaultCurrency));
        } else { // edit trip
            mNameBox.setText(getTrip().getName());

            mStartBox.setText(getTrip().getFormattedStartDate(getActivity(), getPersistenceManager().getPreferenceManager().get(UserPreference.General.DateSeparator)));
            mStartBox.date = getTrip().getStartDate();

            mEndBox.setText(getTrip().getFormattedEndDate(getActivity(), getPersistenceManager().getPreferenceManager().get(UserPreference.General.DateSeparator)));
            mEndBox.date = getTrip().getEndDate();

            mCommentBox.setText(getTrip().getComment());

            currencySpinnerPosition = mCurrencies.getPosition(getTrip().getDefaultCurrencyCode());

            mStartBox.setOnClickListener(getDateManager().getDateEditTextListener());
            mCostCenterBox.setText(getTrip().getCostCenter());

            mCurrencySpinner.setOnItemSelectedListener(new CurrencySpinnerSelectionListener());

        }

        // Focused View
        if (mFocusedView == null) {
            mFocusedView = mNameBox;
        }
        // set currency
        if (currencySpinnerPosition > 0) {
            mCurrencySpinner.setSelection(currencySpinnerPosition);
        }

        mStartBox.setFocusableInTouchMode(false);

        mEndBox.setFocusableInTouchMode(false);
        mEndBox.setOnClickListener(getDateManager().getDateEditTextListener());
        mNameBox.setSelection(mNameBox.getText().length()); // Put the cursor at the end
    }

    private void setKeyboardRelatedListeners() {
        // Set each focus listener, so we can track the focus view across resume -> pauses
        mNameBox.setOnFocusChangeListener(this);
        mStartBox.setOnFocusChangeListener(this);
        mEndBox.setOnFocusChangeListener(this);
        mCurrencySpinner.setOnFocusChangeListener(this);
        mCommentBox.setOnFocusChangeListener(this);
        mCostCenterBox.setOnFocusChangeListener(this);

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
        mStartBox.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        mEndBox.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        mCurrencySpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);
    }

    private void saveTripChanges() {
        String name = mNameBox.getText().toString().trim();
        final String startDateText = mStartBox.getText().toString();
        final String endDateText = mEndBox.getText().toString();
        final String defaultCurrencyCode = mCurrencySpinner.getSelectedItem().toString();
        final String comment = mCommentBox.getText().toString();
        final String costCenter = mCostCenterBox.getText().toString();

        if (mPresenter.checkTrip(name, startDateText, mStartBox.date, endDateText, mEndBox.date)) {
            Trip updatedTrip = mPresenter.saveTrip(getPersistenceManager().getStorageManager().getFile(name),
                    mStartBox.date, mEndBox.date, defaultCurrencyCode, comment, costCenter);
            // open created/edited trip info
            mNavigationHandler.navigateToReportInfoFragment(updatedTrip);
        }
    }

    public void showError(TripEditorErrors error) {
        switch (error) {
            case MISSING_FIELD:
                Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_TRIPMENU_TOAST_MISSING_FIELD), Toast.LENGTH_LONG).show();
                break;
            case CALENDAR_ERROR:
                Toast.makeText(getActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_LONG).show();
                break;
            case DURATION_ERROR:
                Toast.makeText(getActivity(), getFlexString(R.string.DURATION_ERROR), Toast.LENGTH_LONG).show();
                break;
            case SPACE_ERROR:
                Toast.makeText(getActivity(), getFlexString(R.string.SPACE_ERROR), Toast.LENGTH_LONG).show();
                break;
            case ILLEGAL_CHAR_ERROR:
                Toast.makeText(getActivity(), getFlexString(R.string.ILLEGAL_CHAR_ERROR), Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        mFocusedView = hasFocus ? view : null;
        if (getTrip() == null && hasFocus) {
            SoftKeyboardManager.showKeyboard(view);
        }
    }

    private class CurrencySpinnerSelectionListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final String newCurrencyCode = mCurrencies.getItem(position).toString();

            if (!getTrip().getDefaultCurrencyCode().equals(newCurrencyCode)) {
                Toast.makeText(view.getContext(), R.string.toast_warning_reset_exchange_rate, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Intentional no-op
        }
    }
}
