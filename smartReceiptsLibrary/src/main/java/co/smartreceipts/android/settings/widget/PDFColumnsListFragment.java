package co.smartreceipts.android.settings.widget;

import android.support.v7.app.ActionBar;

import co.smartreceipts.android.R;
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
