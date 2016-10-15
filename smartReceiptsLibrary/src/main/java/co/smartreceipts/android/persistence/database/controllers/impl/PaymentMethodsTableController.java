package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.persistence.PersistenceManager;

public class PaymentMethodsTableController extends AbstractTableController<PaymentMethod> {

    public PaymentMethodsTableController(@NonNull PersistenceManager persistenceManager, @NonNull Analytics analytics) {
        super(persistenceManager.getDatabase().getPaymentMethodsTable(), analytics);
    }
}
