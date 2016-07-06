package co.smartreceipts.android.persistence.database.tables.controllers.impl;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.persistence.PersistenceManager;

public class PaymentMethodsTableController extends AbstractTableController<PaymentMethod> {

    public PaymentMethodsTableController(@NonNull PersistenceManager persistenceManager) {
        super(persistenceManager.getDatabase().getPaymentMethodsTable());
    }
}
