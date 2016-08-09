package co.smartreceipts.android.fragments.preferences;

import android.support.v7.app.ActionBar;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.controllers.impl.ColumnTableController;

public class PDFColumnsListFragment extends ColumnsListFragment {

    public static String TAG = "PDFColumnsListFragment";

    public static ColumnsListFragment newInstance() {
        return new PDFColumnsListFragment();
    }

    @Override
    public ColumnTableController getColumnTableController() {
        return getSmartReceiptsApplication().getTableControllerManager().getPDFTableController();
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.menu_main_pdf);
        }
    }

}
