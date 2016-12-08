package co.smartreceipts.android.fragments.preferences;

import android.support.v7.app.ActionBar;

import co.smartreceipts.android.R;
import co.smartreceipts.android.persistence.database.controllers.impl.ColumnTableController;

public class CSVColumnsListFragment extends ColumnsListFragment {

	public static String TAG = "CSVColumnsListFragment";

	public static ColumnsListFragment newInstance() {
		return new CSVColumnsListFragment();
	}

    @Override
    public ColumnTableController getColumnTableController() {
        return getSmartReceiptsApplication().getTableControllerManager().getCSVTableController();
    }

    @Override
	public void onResume() {
		super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_main_csv);
        }
	}

}
