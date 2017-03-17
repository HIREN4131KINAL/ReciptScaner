package co.smartreceipts.android.persistence.database.defaults;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import co.smartreceipts.android.persistence.database.tables.CSVTable;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.PDFTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;

public class WhiteLabelFriendlyTableDefaultsCustomizer implements TableDefaultsCustomizer {

    @Inject TableDefaultCustomizerImpl tableDefaultsCustomizer;

    @Inject
    public WhiteLabelFriendlyTableDefaultsCustomizer() {
    }

    // TODO: 15.03.2017 ask!
//    public WhiteLabelFriendlyTableDefaultsCustomizer(/*@NonNull SmartReceiptsApplication smartReceiptsApplication,*/
//                                                     @NonNull TableDefaultsCustomizer tableDefaultsCustomizer) {
//        //Looks like it's always false
//        /*if (smartReceiptsApplication instanceof TableDefaultsCustomizer) {
//            Logger.warn(this, "Using a white labeled set of table defaults");
//            mTableDefaultsCustomizer = (TableDefaultsCustomizer) smartReceiptsApplication;
//        } else {*/
//            this.tableDefaultsCustomizer = tableDefaultsCustomizer;
////        }
//    }

    @Override
    public void insertCSVDefaults(@NonNull CSVTable table) {
        tableDefaultsCustomizer.insertCSVDefaults(table);
    }

    @Override
    public void insertPDFDefaults(@NonNull PDFTable table) {
        tableDefaultsCustomizer.insertPDFDefaults(table);
    }

    @Override
    public void insertCategoryDefaults(@NonNull CategoriesTable table) {
        tableDefaultsCustomizer.insertCategoryDefaults(table);
    }

    @Override
    public void insertPaymentMethodDefaults(@NonNull PaymentMethodsTable table) {
        tableDefaultsCustomizer.insertPaymentMethodDefaults(table);
    }
}
