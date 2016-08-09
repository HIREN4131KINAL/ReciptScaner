package co.smartreceipts.android.persistence.database.defaults;

import android.support.annotation.NonNull;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.persistence.database.tables.CSVTable;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.PDFTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;
import wb.android.google.camera.data.Log;

public class WhiteLabelFriendlyTableDefaultsCustomizer implements TableDefaultsCustomizer {

    private static final String TAG = WhiteLabelFriendlyTableDefaultsCustomizer.class.getSimpleName();

    private final TableDefaultsCustomizer mTableDefaultsCustomizer;

    public WhiteLabelFriendlyTableDefaultsCustomizer(@NonNull SmartReceiptsApplication smartReceiptsApplication,
                                                     @NonNull TableDefaultsCustomizer tableDefaultsCustomizer) {
        if (smartReceiptsApplication instanceof TableDefaultsCustomizer) {
            Log.w(TAG, "Using a white labeled set of table defaults");
            mTableDefaultsCustomizer = (TableDefaultsCustomizer) smartReceiptsApplication;
        } else {
            mTableDefaultsCustomizer = tableDefaultsCustomizer;
        }
    }

    @Override
    public void insertCSVDefaults(@NonNull CSVTable table) {
        mTableDefaultsCustomizer.insertCSVDefaults(table);
    }

    @Override
    public void insertPDFDefaults(@NonNull PDFTable table) {
        mTableDefaultsCustomizer.insertPDFDefaults(table);
    }

    @Override
    public void insertCategoryDefaults(@NonNull CategoriesTable table) {
        mTableDefaultsCustomizer.insertCategoryDefaults(table);
    }

    @Override
    public void insertPaymentMethodDefaults(@NonNull PaymentMethodsTable table) {
        mTableDefaultsCustomizer.insertPaymentMethodDefaults(table);
    }
}
