package co.smartreceipts.android.fragments;

import java.math.BigDecimal;
import java.sql.Date;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.persistence.DatabaseHelper;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DistanceDialogFragment extends DialogFragment implements OnClickListener {
	
	public static final String TAG = DistanceDialogFragment.class.getSimpleName();
	
	private static final String KEY_DISTANCE_OBJECT = "distanceObject";
	
	private EditText mDistance, mRate, mLocation, mComment;
	private DateEditText mDate;
	private Object mUpdateableDistance;
	private DatabaseHelper mDB;
	
	public static final DistanceDialogFragment newInstance() {
		return newInstance(null);
	}
	
	public static final DistanceDialogFragment newInstance(Parcelable distance) {
		final DistanceDialogFragment dialog = new DistanceDialogFragment();
		if (distance != null) {
			final Bundle args = new Bundle();
			args.putParcelable(KEY_DISTANCE_OBJECT, distance);
		}
		return dialog;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(KEY_DISTANCE_OBJECT)) {
			mUpdateableDistance = savedInstanceState.get(KEY_DISTANCE_OBJECT);
		}
		mDB = ((SmartReceiptsApplication)getActivity().getApplication()).getPersistenceManager().getDatabase();
	}
		
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View rootView = inflater.inflate(R.layout.dialog_mileage, null);
		
		mDistance = (EditText) rootView.findViewById(R.id.dialog_mileage_distance);
		mRate = (EditText) rootView.findViewById(R.id.dialog_mileage_rate);
		mLocation = (EditText) rootView.findViewById(R.id.dialog_mileage_location);
		mComment = (EditText) rootView.findViewById(R.id.dialog_mileage_comment);
		mDate = (DateEditText) rootView.findViewById(R.id.dialog_mileage_date);
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(rootView);
		if (mUpdateableDistance == null) {
			// New Distance
			builder.setTitle(getString(R.string.dialog_mileage_title_create));
			builder.setPositiveButton(getString(R.string.dialog_mileage_positive_create), this);
		}
		else {
			// Update
			// ************
			// TODO: Replace views with existing values
			// ************
			builder.setTitle(getString(R.string.dialog_mileage_title_update));
			builder.setPositiveButton(getString(R.string.dialog_mileage_positive_update), this);
			builder.setNeutralButton(getString(R.string.dialog_mileage_neutral_delete), this);
		}
		builder.setNegativeButton(android.R.string.cancel, this);
		
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			final String location = mLocation.getText().toString();
			final String comment = mComment.getText().toString();
			final Date date = mDate.date;
			
			if (mUpdateableDistance == null) {
				// We're inserting a new one
				final BigDecimal distance = getBigDecimalFromString(mDistance.getText().toString(), new BigDecimal(0));
				final BigDecimal rate = getBigDecimalFromString(mRate.getText().toString(), new BigDecimal(0));
			}
			else {
				// We're updating an existing one
				// ************
				// TODO: Replace fallbacks with existing values
				// ************
				final BigDecimal distance = getBigDecimalFromString(mDistance.getText().toString(), new BigDecimal(0));
				final BigDecimal rate = getBigDecimalFromString(mRate.getText().toString(), new BigDecimal(0));
			}
		}
		else if (which == DialogInterface.BUTTON_NEUTRAL) {
			// TODO: Show delete warning dialog
		}
		dialog.dismiss();

	}
	
	/**
	 * @param number - a string containing a number
	 * @param fallback - the {@link BigDecimal} to return if the string is NaN
	 * @return a {@link BigDecimal} or the fallback param if not
	 */
	private BigDecimal getBigDecimalFromString(String number, BigDecimal fallback) {
		try {
			return new BigDecimal(number);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

}
