package co.smartreceipts.android.persistence.database.tables.controllers.impl;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.PersistenceManager;

public class CSVTableController extends AbstractTableController<Column<Receipt>> {

    public CSVTableController(@NonNull PersistenceManager persistenceManager) {
        super(persistenceManager.getDatabase().getCSVTable());
    }
}
