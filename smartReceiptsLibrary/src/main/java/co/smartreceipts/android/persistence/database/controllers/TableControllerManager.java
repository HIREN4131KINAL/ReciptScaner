package co.smartreceipts.android.persistence.database.controllers;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.impl.CSVTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PDFTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;

public class TableControllerManager {

    private final TripTableController mTripTableController;
    private final DistanceTableController mDistanceTableController;
    private final CategoriesTableController mCategoriesTableController;
    private final CSVTableController mCSVTableController;
    private final PDFTableController mPDFTableController;
    private final PaymentMethodsTableController mPaymentMethodsTableController;

    public TableControllerManager(@NonNull PersistenceManager persistenceManager) {
        Preconditions.checkNotNull(persistenceManager);
        mTripTableController = new TripTableController(persistenceManager);
        mDistanceTableController = new DistanceTableController(persistenceManager, mTripTableController);
        mCategoriesTableController = new CategoriesTableController(persistenceManager);
        mCSVTableController = new CSVTableController(persistenceManager);
        mPDFTableController = new PDFTableController(persistenceManager);
        mPaymentMethodsTableController = new PaymentMethodsTableController(persistenceManager);
    }

    @NonNull
    public TripTableController getTripTableController() {
        return mTripTableController;
    }

    @NonNull
    public DistanceTableController getDistanceTableController() {
        return mDistanceTableController;
    }

    @NonNull
    public CategoriesTableController getCategoriesTableController() {
        return mCategoriesTableController;
    }

    @NonNull
    public CSVTableController getCSVTableController() {
        return mCSVTableController;
    }

    @NonNull
    public PDFTableController getPDFTableController() {
        return mPDFTableController;
    }

    @NonNull
    public PaymentMethodsTableController getPaymentMethodsTableController() {
        return mPaymentMethodsTableController;
    }
}
