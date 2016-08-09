package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.PersistenceManager;

public class CSVTableController extends ColumnTableController {

    public CSVTableController(@NonNull PersistenceManager persistenceManager, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        super(persistenceManager.getDatabase().getCSVTable(), receiptColumnDefinitions);
    }
}
