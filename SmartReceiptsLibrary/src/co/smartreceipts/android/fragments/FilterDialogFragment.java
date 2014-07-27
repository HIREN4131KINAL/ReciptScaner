package co.smartreceipts.android.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class FilterDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setPositiveButton(getString(R.string.dialog_filter_positive_button), this);
		builder.setNegativeButton(getString(R.string.dialog_filter_positive_button), this);
		
		return builder.create();
	}
	
	
	private View buildViewsFromFilters(List<Filter<ReceiptRow>> filters) {
		
		return null;
	}
}
