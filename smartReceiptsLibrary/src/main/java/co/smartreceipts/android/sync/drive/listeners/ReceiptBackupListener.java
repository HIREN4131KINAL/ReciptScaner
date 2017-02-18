package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.ReceiptTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.sync.drive.managers.DriveDatabaseManager;
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager;

public class ReceiptBackupListener extends StubTableEventsListener<Receipt> implements ReceiptTableEventsListener {

    private final DriveDatabaseManager driveDatabaseManager;
    private final DriveReceiptsManager driveReceiptsManager;

    public ReceiptBackupListener(@NonNull DriveDatabaseManager driveDatabaseManager, @NonNull DriveReceiptsManager driveReceiptsManager) {
        this.driveDatabaseManager = Preconditions.checkNotNull(driveDatabaseManager);
        this.driveReceiptsManager = Preconditions.checkNotNull(driveReceiptsManager);
    }

    @Override
    public void onInsertSuccess(@NonNull Receipt receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            driveDatabaseManager.syncDatabase();
            driveReceiptsManager.handleInsertOrUpdate(receipt);
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            driveDatabaseManager.syncDatabase();
            driveReceiptsManager.handleInsertOrUpdate(newReceipt);
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull Receipt receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            driveDatabaseManager.syncDatabase();
            driveReceiptsManager.handleDelete(receipt);
        }
    }

    @Override
    public void onMoveSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {
        driveDatabaseManager.syncDatabase();
        driveReceiptsManager.handleInsertOrUpdate(newReceipt);
    }

    @Override
    public void onMoveFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e) {

    }

    @Override
    public void onCopySuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {
        driveDatabaseManager.syncDatabase();
        driveReceiptsManager.handleInsertOrUpdate(newReceipt);
    }

    @Override
    public void onCopyFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e) {

    }

    @Override
    public void onSwapSuccess() {
        driveReceiptsManager.initialize();
    }

    @Override
    public void onSwapFailure(@Nullable Throwable e) {

    }

    @Override
    public void onGetSuccess(@NonNull List<Receipt> list, @NonNull Trip trip) {

    }

    @Override
    public void onGetFailure(@Nullable Throwable e, @NonNull Trip trip) {

    }
}
