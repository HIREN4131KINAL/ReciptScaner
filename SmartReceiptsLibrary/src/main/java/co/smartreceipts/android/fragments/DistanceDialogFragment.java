package co.smartreceipts.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.math.BigDecimal;
import java.sql.Date;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.Preferences;

public class DistanceDialogFragment extends DialogFragment implements OnClickListener {

    public static final String TAG = DistanceDialogFragment.class.getSimpleName();

    private static final String KEY_DISTANCE_OBJECT = "distanceObject";

    private EditText mDistance, mRate, mLocation, mComment;
    private DateEditText mDate;
    private Spinner mCurrency;
    private Trip mTrip;
    private Distance mUpdateableDistance;
    private DatabaseHelper mDB;
    private Preferences mPrefs;

    /**
     * Creates a new instance of a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}, which
     * can be used to enter a new distance item
     *
     * @param trip - the parent {@link co.smartreceipts.android.model.Trip}
     * @return - a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}
     */
    public static DistanceDialogFragment newInstance(final Trip trip) {
        return newInstance(trip, null);
    }


    /**
     * Creates a new instance of a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}, which
     * can be used to update an existing distance item
     *
     * @param trip - the parent {@link co.smartreceipts.android.model.Trip}
     * @param distance - the {@link co.smartreceipts.android.model.Distance} object to update
     * @return - a {@link co.smartreceipts.android.fragments.DistanceDialogFragment}
     */
    public static DistanceDialogFragment newInstance(final Trip trip, final Distance distance) {
        final DistanceDialogFragment dialog = new DistanceDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);
        if (distance != null) {
            args.putParcelable(Distance.PARCEL_KEY, distance);
        }
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SmartReceiptsApplication app = ((SmartReceiptsApplication) getActivity().getApplication());
        mDB = app.getPersistenceManager().getDatabase();
        mPrefs = app.getPersistenceManager().getPreferences();
        mTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
        mUpdateableDistance = getArguments().getParcelable(Distance.PARCEL_KEY);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View rootView = inflater.inflate(R.layout.dialog_mileage, null);

        mDistance = (EditText) rootView.findViewById(R.id.dialog_mileage_distance);
        mRate = (EditText) rootView.findViewById(R.id.dialog_mileage_rate);
        mCurrency = (Spinner) rootView.findViewById(R.id.dialog_mileage_currency);
        mLocation = (EditText) rootView.findViewById(R.id.dialog_mileage_location);
        mComment = (EditText) rootView.findViewById(R.id.dialog_mileage_comment);
        mDate = (DateEditText) rootView.findViewById(R.id.dialog_mileage_date);

        final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, mDB.getCurrenciesList());
        currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCurrency.setAdapter(currenices);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        if (mUpdateableDistance == null) {
            // New Distance
            builder.setTitle(getString(R.string.dialog_mileage_title_create));
            builder.setPositiveButton(getString(R.string.dialog_mileage_positive_create), this);
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
            int idx = currenices.getPosition(mUpdateableDistance.getCurrencyCode());
            if (idx > 0) {
                mCurrency.setSelection(idx);
            }
        }
        builder.setNegativeButton(android.R.string.cancel, this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            final String currency = mCurrency.getSelectedItem().toString();
            final String location = mLocation.getText().toString();
            final String comment = mComment.getText().toString();
            final Date date = mDate.date;

            if (mUpdateableDistance == null) {
                // We're inserting a new one
                final BigDecimal distance = getBigDecimalFromString(mDistance.getText().toString(), new BigDecimal(0));
                final BigDecimal rate = getBigDecimalFromString(mRate.getText().toString(), new BigDecimal(0));
                mDB.insertDistanceParallel(location, distance, date, rate, currency, comment);
            } else {
                // We're updating
                final BigDecimal distance = getBigDecimalFromString(mDistance.getText().toString(), mUpdateableDistance.getDistance());
                final BigDecimal rate = getBigDecimalFromString(mRate.getText().toString(), mUpdateableDistance.getRate());
                mDB.updateDistanceParallel(mUpdateableDistance, location, distance, date, rate, currency, comment);
            }
        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
            // TODO: Show delete warning dialog
        }
        dialog.dismiss();

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
            return new BigDecimal(number);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

}
