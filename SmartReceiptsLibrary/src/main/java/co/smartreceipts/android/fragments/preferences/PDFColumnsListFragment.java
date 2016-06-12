package co.smartreceipts.android.fragments.preferences;

import android.support.v7.app.ActionBar;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;

public class PDFColumnsListFragment extends ColumnsListFragment {

    public static String TAG = "PDFColumnsListFragment";

    public static ColumnsListFragment newInstance() {
        return new PDFColumnsListFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.menu_main_pdf);
        }
    }

    @Override
    public List<Column<Receipt>> getColumns() {
        return getPersistenceManager().getDatabase().getPDFTable().get();
    }

    @Override
    public void addColumn() {
        getPersistenceManager().getDatabase().getPDFTable().insertDefaultColumn();
    }

    @Override
    public void deleteLastColumn() {
        getPersistenceManager().getDatabase().getPDFTable().deleteLast();
    }

    @Override
    public void updateColumn(Column<Receipt> oldColumn, Column<Receipt> newColumn) {
        getPersistenceManager().getDatabase().getPDFTable().update(oldColumn, newColumn);
    }

}
