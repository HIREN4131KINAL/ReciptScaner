package co.smartreceipts.android.fragments.preferences;

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
        getSupportActionBar().setTitle(R.string.menu_main_pdf);
    }

    @Override
    public List<Column<Receipt>> getColumns() {
        return getPersistenceManager().getDatabase().getPDFColumns();
    }

    @Override
    public void addColumn() {
        getPersistenceManager().getDatabase().insertPDFColumn();
    }

    @Override
    public void deleteLastColumn() {
        getPersistenceManager().getDatabase().deletePDFColumn();
    }

    @Override
    public void updateColumn(Column<Receipt> oldColumn, Column<Receipt> newColumn) {
        getPersistenceManager().getDatabase().updatePDFColumn(oldColumn, newColumn);
    }

}
