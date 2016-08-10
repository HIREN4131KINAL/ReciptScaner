package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;

public class ReceiptBackupListener extends StubTableEventsListener<Receipt> {

    @Override
    public void onInsertSuccess(@NonNull Receipt receipt) {

    }

    @Override
    public void onUpdateSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {

    }

    @Override
    public void onDeleteSuccess(@NonNull Receipt receipt) {

    }
}
