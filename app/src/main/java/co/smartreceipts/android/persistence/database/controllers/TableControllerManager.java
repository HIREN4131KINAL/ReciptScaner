package co.smartreceipts.android.persistence.database.controllers;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.di.qualifiers.ReceiptColumnDefinitions;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.impl.CSVTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PDFTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;

@ApplicationScope
public class TableControllerManager {

    // TODO: 29.03.2017 dagger for fields. tcm is often used just for one get method
    // tripTableController, distanceTableController, categoriesTC, CSVTableController, PDFTableController, RTC
    
    private final TripTableController mTripTableController;
    private final ReceiptTableController mReceiptTableController;
    private final DistanceTableController mDistanceTableController;
    private final CategoriesTableController mCategoriesTableController;
    private final CSVTableController mCSVTableController;
    private final PDFTableController mPDFTableController;
    private final PaymentMethodsTableController mPaymentMethodsTableController;

    @Inject
    public TableControllerManager(PersistenceManager persistenceManager,
                                  Analytics analytics,
                                  @ReceiptColumnDefinitions ColumnDefinitions<Receipt> receiptColumnDefinitions) {

        mTripTableController = new TripTableController(persistenceManager, analytics);
        mReceiptTableController = new ReceiptTableController(persistenceManager, analytics, mTripTableController);
        mDistanceTableController = new DistanceTableController(persistenceManager, analytics, mTripTableController);
        mCategoriesTableController = new CategoriesTableController(persistenceManager, analytics);
        mCSVTableController = new CSVTableController(persistenceManager, analytics, receiptColumnDefinitions);
        mPDFTableController = new PDFTableController(persistenceManager, analytics, receiptColumnDefinitions);
        mPaymentMethodsTableController = new PaymentMethodsTableController(persistenceManager, analytics);
    }

    @NonNull
    public TripTableController getTripTableController() {
        return mTripTableController;
    }

    @NonNull
    public ReceiptTableController getReceiptTableController() {
        return mReceiptTableController;
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
