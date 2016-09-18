package co.smartreceipts.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import java.math.BigDecimal;
import java.sql.Date;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.date.DateManager;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import wb.android.autocomplete.AutoCompleteAdapter;

public class DistanceDialogFragment extends DialogFragment implements OnClickListener {

    public static final String TAG = DistanceDialogFragment.class.getSimpleName();
    private static final String ARG_SUGGESTED_DATE = "arg_suggested_date";

    private EditText mDistance, mRate, mComment;
    private AutoCompleteTextView mLocation;
    private DateEditText mDate;
    private Spinner mCurrency;
    private Trip mTrip;
    private Distance mUpdateableDistance;
    private DatabaseHelper mDB;
    private Preferences mPrefs;
    private DateManager mDateManager;
    private AutoCompleteAdapter mLocationAutoCompleteAdapter;
    private Date mSuggestedDate;
    private DistanceTableController mDistanceTableController;

    /**
     * Creates a new instance of a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}, which
     * can be used to enter a new distance item
     *
     * @param trip - the parent {@link co.smartreceipts.android.model.Trip}
     * @return - a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}
     */
    public static DistanceDialogFragment newInstance(final Trip trip) {
        return newInstance(trip, null, null);
    }


    /**
     * Creates a new instance of a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}, which
     * can be used to enter a new distance item
     *
     * @param trip          - the parent {@link co.smartreceipts.android.model.Trip}
     * @param suggestedDate - the suggested {@link java.sql.Date} to display to the user when creating a new distance item
     * @return - a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}
     */
    public static DistanceDialogFragment newInstance(final Trip trip, final Date suggestedDate) {
        return newInstance(trip, null, suggestedDate);
    }


    /**
     * Creates a new instance of a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}, which
     * can be used to update an existing distance item
     *
     * @param trip     - the parent {@link co.smartreceipts.android.model.Trip}
     * @param distance - the {@link co.smartreceipts.android.model.Distance} object to update
     * @return - a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}
     */
    public static DistanceDialogFragment newInstance(final Trip trip, final Distance distance) {
        return newInstance(trip, distance, null);
    }


    private static DistanceDialogFragment newInstance(final Trip trip, final Distance distance, final Date suggestedDate) {
        final DistanceDialogFragment dialog = new DistanceDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);
        if (distance != null) {
            args.putParcelable(Distance.PARCEL_KEY, distance);
        }
        if (suggestedDate != null) {
            args.putLong(ARG_SUGGESTED_DATE, suggestedDate.getTime() + 1);
        }
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SmartReceiptsApplication app = ((SmartReceiptsApplication) getActivity().getApplication());
        mDB = app.getPersistenceManager().getDatabase();
        mDistanceTableController = app.getTableControllerManager().getDistanceTableController();
        mPrefs = app.getPersistenceManager().getPreferences();
        mTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
        mUpdateableDistance = getArguments().getParcelable(Distance.PARCEL_KEY);
        final Time now = new Time();
        now.setToNow();
        // Default to "now" if not suggested date was set
        mSuggestedDate = new Date(getArguments().getLong(ARG_SUGGESTED_DATE, now.toMillis(false)));
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View rootView = inflater.inflate(R.layout.dialog_mileage, null);

        mDistance = (EditText) rootView.findViewById(R.id.dialog_mileage_distance);
        mRate = (EditText) rootView.findViewById(R.id.dialog_mileage_rate);
        mCurrency = (Spinner) rootView.findViewById(R.id.dialog_mileage_currency);
        mLocation = (AutoCompleteTextView) rootView.findViewById(R.id.dialog_mileage_location);
        mComment = (EditText) rootView.findViewById(R.id.dialog_mileage_comment);
        mDate = (DateEditText) rootView.findViewById(R.id.dialog_mileage_date);

        final ArrayAdapter<CharSequence> currencies = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, mDB.getCurrenciesList());
        currencies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCurrency.setAdapter(currencies);
        mDate.setOnClickListener(getDateManager().getDateEditTextListener());
        mDate.setFocusable(false);
        mDate.setFocusableInTouchMode(false);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        if (mUpdateableDistance == null) {
            // New Distance
            builder.setTitle(getString(R.string.dialog_mileage_title_create));
            builder.setPositiveButton(getString(R.string.dialog_mileage_positive_create), this);
            mDate.date = mSuggestedDate;
            mDate.setText(DateFormat.getDateFormat(getActivity()).format(mDate.date));
            int idx = currencies.getPosition(mTrip.getPrice().getCurrencyCode());
            if (idx > 0) {
                mCurrency.setSelection(idx);
            }
            if (mPrefs.hasDefaultDistanceRate()) {
                mRate.setText(ModelUtils.getDecimalFormattedValue(new BigDecimal(mPrefs.getDefaultDistanceRate()), Distance.RATE_PRECISION));
            }
            if (mLocationAutoCompleteAdapter == null) {
                mLocationAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_DISTANCE_LOCATION, mDB);
            } else {
                mLocationAutoCompleteAdapter.reset();
            }
            mLocation.setAdapter(mLocationAutoCompleteAdapter);
            mDistance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus && getActivity() != null && getDialog() != null) {
                        if (getActivity().getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
                            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                }
            });
        } else {
            // Update distance
            builder.setTitle(getString(R.string.dialog_mileage_title_update));
            builder.setPositiveButton(getString(R.string.dialog_mileage_positive_update), this);
            builder.setNeutralButton(getString(R.string.dialog_mileage_neutral_delete), this);
            mDistance.setText(mUpdateableDistance.getDecimalFormattedDistance());
            mRate.setText(mUpdateableDistance.getDecimalFormattedRate());
            mLocation.setText(mUpdateableDistance.getLocation());
            mComment.setText(mUpdateableDistance.getComment());
            mDate.setText(mUpdateableDistance.getFormattedDate(getActivity(), mPrefs.getDateSeparator()));
            mDate.date = mUpdateableDistance.getDate();
            int idx = currencies.getPosition(mUpdateableDistance.getPrice().getCurrencyCode());
            if (idx > 0) {
                mCurrency.setSelection(idx);
            }
        }
        builder.setNegativeButton(android.R.string.cancel, this);

        final Dialog dialog = builder.create();
        getDateManager().setDateEditTextListenerDialogHolder(dialog);
        return dialog;
    }

    @Override
    public void onPause() {
        if (mLocationAutoCompleteAdapter != null) {
            mLocationAutoCompleteAdapter.onPause();
        }
        super.onPause();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Create/Edit
            final String currency = mCurrency.getSelectedItem().toString();
            final String location = mLocation.getText().toString();
            final String comment = mComment.getText().toString();
            final Date date = mDate.date;

            if (mUpdateableDistance == null) {
                // We're inserting a new one
                final BigDecimal distance = getBigDecimalFromString(mDistance.getText().toString(), new BigDecimal(0));
                final BigDecimal rate = getBigDecimalFromString(mRate.getText().toString(), new BigDecimal(0));
                final DistanceBuilderFactory builder = new DistanceBuilderFactory();
                builder.setTrip(mTrip);
                builder.setLocation(location);
                builder.setDistance(distance);
                builder.setDate(date);
                builder.setRate(rate);
                builder.setCurrency(currency);
                builder.setComment(comment);
                ((SmartReceiptsApplication)getActivity().getApplication()).getAnalyticsManager().record(Events.Distance.PersistNewDistance);
                mDistanceTableController.insert(builder.build(), new DatabaseOperationMetadata());
            } else {
                // We're updating
                final BigDecimal distance = getBigDecimalFromString(mDistance.getText().toString(), mUpdateableDistance.getDistance());
                final BigDecimal rate = getBigDecimalFromString(mRate.getText().toString(), mUpdateableDistance.getRate());
                final DistanceBuilderFactory builder = new DistanceBuilderFactory(mUpdateableDistance);
                builder.setLocation(location);
                builder.setDistance(distance);
                builder.setDate(date);
                builder.setRate(rate);
                builder.setCurrency(currency);
                builder.setComment(comment);
                ((SmartReceiptsApplication)getActivity().getApplication()).getAnalyticsManager().record(Events.Distance.PersistUpdateDistance);
                mDistanceTableController.update(mUpdateableDistance, builder.build(), new DatabaseOperationMetadata());
            }
        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
            // Delete
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.delete_item, mUpdateableDistance.getLocation()));
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.delete, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDistanceTableController.delete(mUpdateableDistance, new DatabaseOperationMetadata());
                    dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });
            builder.show();
        }
        dismiss();
    }

    /**
     * @param number   - a string containing a number
     * @param fallback - the {@link BigDecimal} to return if the string is NaN
     * @return a {@link BigDecimal} or the fallback param if not
     */
    private BigDecimal getBigDecimalFromString(String number, BigDecimal fallback) {
        if (TextUtils.isEmpty(number)) {
            return fallback;
        }
        try {
            return new BigDecimal(number.replace(",", "."));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }


    private DateManager getDateManager() {
        if (mDateManager == null) {
            mDateManager = new DateManager(getActivity(), ((SmartReceiptsApplication) getActivity().getApplication()).getPersistenceManager().getPreferences());
        }
        return mDateManager;
    }

}
