package co.smartreceipts.android.fragments.preferences;

import android.support.v7.app.ActionBar;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;

public class CSVColumnsListFragment extends ColumnsListFragment {

	public static String TAG = "CSVColumnsListFragment";

	public static ColumnsListFragment newInstance() {
		return new CSVColumnsListFragment();
	}

	@Override
	public void onResume() {
		super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_main_csv);
        }
	}

	@Override
	public List<Column<Receipt>> getColumns() {
		return getPersistenceManager().getDatabase().getCSVTable().getColumns();
	}

	@Override
	public void addColumn() {
		getPersistenceManager().getDatabase().getCSVTable().insertDefaultColumn();
	}

	@Override
	public void deleteLastColumn() {
		getPersistenceManager().getDatabase().getCSVTable().deleteColumn();
	}

	@Override
	public void updateColumn(Column<Receipt> oldColumn, Column<Receipt> newColumn) {
		getPersistenceManager().getDatabase().getCSVTable().updateColumn(oldColumn, newColumn);
	}

}
