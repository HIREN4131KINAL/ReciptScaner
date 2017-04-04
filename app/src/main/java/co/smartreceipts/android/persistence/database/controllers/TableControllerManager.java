package co.smartreceipts.android.persistence.database.controllers;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.persistence.database.controllers.impl.CSVTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PDFTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;

@ApplicationScope
public class TableControllerManager {

    // TODO: 31.03.2017 We can get rid of all getters here, but I'm not sure if it's better or not
    // (GoogleDriveBackupManager could became more bulky)
    
    private final TripTableController tripTableController;
    private final ReceiptTableController receiptTableController;
    private final CategoriesTableController categoriesTableController;
    private final CSVTableController csvTableController;
    private final PDFTableController pdfTableController;
    private final PaymentMethodsTableController paymentMethodsTableController;
    private final DistanceTableController distanceTableController;

    @Inject
    public TableControllerManager(TripTableController tripTableController,
                                  ReceiptTableController receiptTableController,
                                  CategoriesTableController categoriesTableController,
                                  CSVTableController csvTableController,
                                  PDFTableController pdfTableController,
                                  PaymentMethodsTableController paymentMethodsTableController,
                                  DistanceTableController distanceTableController) {

        this.tripTableController = tripTableController;
        this.receiptTableController = receiptTableController;
        this.categoriesTableController = categoriesTableController;
        this.csvTableController = csvTableController;
        this.pdfTableController = pdfTableController;
        this.paymentMethodsTableController = paymentMethodsTableController;
        this.distanceTableController = distanceTableController;
    }

    @NonNull
    public TripTableController getTripTableController() {
        return tripTableController;
    }

    @NonNull
    public ReceiptTableController getReceiptTableController() {
        return receiptTableController;
    }

    @NonNull
    public DistanceTableController getDistanceTableController() {
        return distanceTableController;
    }

    @NonNull
    public CategoriesTableController getCategoriesTableController() {
        return categoriesTableController;
    }

    @NonNull
    public CSVTableController getCSVTableController() {
        return csvTableController;
    }

    @NonNull
    public PDFTableController getPDFTableController() {
        return pdfTableController;
    }

    @NonNull
    public PaymentMethodsTableController getPaymentMethodsTableController() {
        return paymentMethodsTableController;
    }
}
