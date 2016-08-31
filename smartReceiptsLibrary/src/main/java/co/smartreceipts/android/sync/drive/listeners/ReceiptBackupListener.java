package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

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
                        // TODO: How do we handle the indefinite loop that we'll see here in the onUpdateSuccess?
                        mReceiptTableController.update(receipt, new ReceiptBuilderFactory(receipt).setSyncState(syncState).build());
                    }
                });
        } else {
            mDriveTaskManager.updateDatabase();
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {

    }

    @Override
    public void onDeleteSuccess(@NonNull Receipt receipt) {

    }
}
