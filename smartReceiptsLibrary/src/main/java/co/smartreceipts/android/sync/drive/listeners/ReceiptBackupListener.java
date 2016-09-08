package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.HashSet;
import java.util.Set;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.sync.drive.GoogleDriveTaskManager;
import co.smartreceipts.android.sync.drive.rx.RxDriveStreams;
import co.smartreceipts.android.sync.model.SyncState;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ReceiptBackupListener extends StubTableEventsListener<Receipt> {

    private final GoogleDriveTaskManager mDriveTaskManager;
    private final ReceiptTableController mReceiptTableController;
    private final Set<Receipt> mReceiptsToIgnoreUpdates = new HashSet<>();

    public ReceiptBackupListener(@NonNull GoogleDriveTaskManager driveTaskManager, @NonNull ReceiptTableController receiptTableController) {
        mDriveTaskManager = Preconditions.checkNotNull(driveTaskManager);
        mReceiptTableController = Preconditions.checkNotNull(receiptTableController);
    }

    @Override
    public void onInsertSuccess(@NonNull final Receipt receipt) {
        if (receipt.getFile() != null) {
            mDriveTaskManager.insert(receipt.getSyncState(), receipt.getFile())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<SyncState>() {
                    @Override
                    public void call(SyncState syncState) {
                        mReceiptsToIgnoreUpdates.add(receipt);
                        mReceiptTableController.update(receipt, new ReceiptBuilderFactory(receipt).setSyncState(syncState).build());
                    }
                });
        }
        mDriveTaskManager.updateDatabase();
    }

    @Override
    public void onUpdateSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {
        if (!mReceiptsToIgnoreUpdates.remove(oldReceipt)) {
            // TODO: Something if we aren't ignoring
        }
        mDriveTaskManager.updateDatabase();
    }

    @Override
    public void onUpdateFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e) {
        mReceiptsToIgnoreUpdates.remove(oldReceipt);
    }

    @Override
    public void onDeleteSuccess(@NonNull final Receipt receipt) {
        if (receipt.getFile() != null) {
            mDriveTaskManager.delete(receipt.getSyncState(), receipt.getFile())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new Action1<SyncState>() {
                        @Override
                        public void call(SyncState syncState) {
                            // TODO: Handle mark for deletion vs full delete
                            mReceiptTableController.delete(receipt);
                        }
                    });
        }
        mDriveTaskManager.updateDatabase();
    }
}
