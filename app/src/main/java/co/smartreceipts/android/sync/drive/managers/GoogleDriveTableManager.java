package co.smartreceipts.android.sync.drive.managers;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.impl.CSVTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PDFTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.sync.drive.listeners.DatabaseBackupListener;
import co.smartreceipts.android.sync.drive.listeners.ReceiptBackupListener;
import co.smartreceipts.android.sync.drive.managers.DriveDatabaseManager;
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager;

@ApplicationScope
public class GoogleDriveTableManager {

    @Inject
    TripTableController tripTableController;
    @Inject
    ReceiptTableController receiptTableController;
    @Inject
    CategoriesTableController categoriesTableController;
    @Inject
    CSVTableController csvTableController;
    @Inject
    PDFTableController pdfTableController;
    @Inject
    PaymentMethodsTableController paymentMethodsTableController;
    @Inject
    DistanceTableController distanceTableController;

    private DatabaseBackupListener<Trip> tripDatabaseBackupListener;
    private ReceiptBackupListener receiptDatabaseBackupListener;
    private DatabaseBackupListener<Distance> distanceDatabaseBackupListener;
    private DatabaseBackupListener<PaymentMethod> paymentMethodDatabaseBackupListener;
    private DatabaseBackupListener<Category> categoryDatabaseBackupListener;
    private DatabaseBackupListener<Column<Receipt>> csvColumnDatabaseBackupListener;
    private DatabaseBackupListener<Column<Receipt>> pdfColumnDatabaseBackupListener;

    @Inject
    public GoogleDriveTableManager() {
    }

    public void initBackupListeners(DriveDatabaseManager driveDatabaseManager, DriveReceiptsManager driveReceiptsManager) {
        tripDatabaseBackupListener = new DatabaseBackupListener<>(driveDatabaseManager);
        receiptDatabaseBackupListener = new ReceiptBackupListener(driveDatabaseManager, driveReceiptsManager);
        distanceDatabaseBackupListener = new DatabaseBackupListener<>(driveDatabaseManager);
        paymentMethodDatabaseBackupListener = new DatabaseBackupListener<>(driveDatabaseManager);
        categoryDatabaseBackupListener = new DatabaseBackupListener<>(driveDatabaseManager);
        csvColumnDatabaseBackupListener = new DatabaseBackupListener<>(driveDatabaseManager);
        pdfColumnDatabaseBackupListener = new DatabaseBackupListener<>(driveDatabaseManager);
    }

    public void onConnected() {
        tripTableController.subscribe(tripDatabaseBackupListener);
        receiptTableController.subscribe(receiptDatabaseBackupListener);
        distanceTableController.subscribe(distanceDatabaseBackupListener);
        paymentMethodsTableController.subscribe(paymentMethodDatabaseBackupListener);
        categoriesTableController.subscribe(categoryDatabaseBackupListener);
        csvTableController.subscribe(csvColumnDatabaseBackupListener);
        pdfTableController.subscribe(pdfColumnDatabaseBackupListener);
    }

    public void onConnectionSuspended() {
        tripTableController.unsubscribe(tripDatabaseBackupListener);
        receiptTableController.unsubscribe(receiptDatabaseBackupListener);
        distanceTableController.unsubscribe(distanceDatabaseBackupListener);
        paymentMethodsTableController.unsubscribe(paymentMethodDatabaseBackupListener);
        categoriesTableController.unsubscribe(categoryDatabaseBackupListener);
        csvTableController.unsubscribe(csvColumnDatabaseBackupListener);
        pdfTableController.unsubscribe(pdfColumnDatabaseBackupListener);
    }
}
