package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.PersistenceManager;

public class PDFTableController extends AbstractTableController<Column<Receipt>> {

    public PDFTableController(@NonNull PersistenceManager persistenceManager) {
        super(persistenceManager.getDatabase().getPDFTable());
    }
}
