package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.File;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.BuilderFactory1;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactoryFactory;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.utils.FileUtils;
import rx.Observable;
import rx.functions.Func0;
import wb.android.storage.StorageManager;

public class ReceiptTableActionAlterations extends StubTableActionAlterations<Receipt> {

    private static final String TAG = ReceiptTableActionAlterations.class.getSimpleName();

    private final ReceiptsTable mReceiptsTable;
    private final StorageManager mStorageManager;
    private final BuilderFactory1<Receipt, ReceiptBuilderFactory> mReceiptBuilderFactoryFactory;

    public ReceiptTableActionAlterations(@NonNull ReceiptsTable receiptsTable, @NonNull StorageManager storageManager) {
        mReceiptsTable = Preconditions.checkNotNull(receiptsTable);
        mStorageManager = Preconditions.checkNotNull(storageManager);
        mReceiptBuilderFactoryFactory = new ReceiptBuilderFactoryFactory();
    }

    ReceiptTableActionAlterations(@NonNull ReceiptsTable receiptsTable, @NonNull StorageManager storageManager, @Nullable BuilderFactory1<Receipt, ReceiptBuilderFactory> receiptBuilderFactoryFactory) {
        mReceiptsTable = Preconditions.checkNotNull(receiptsTable);
        mStorageManager = Preconditions.checkNotNull(storageManager);
        mReceiptBuilderFactoryFactory = Preconditions.checkNotNull(receiptBuilderFactoryFactory);
    }

    @NonNull
    @Override
    public Observable<Receipt> preInsert(@NonNull final Receipt receipt) {
        return Observable.defer(new Func0<Observable<Receipt>>() {
            @Override
            public Observable<Receipt> call() {
                return Observable.just(preInsertBlocking(receipt));
            }
        });
    }

    @Override
    public void postUpdate(@NonNull Receipt oldReceipt, @Nullable Receipt newReceipt) throws Exception {
        if (newReceipt != null) { // i.e. - the update succeeded
            if (oldReceipt.hasFile()) {
                mStorageManager.delete(oldReceipt.getFile());
            }
        }
    }

    @Override
    public void postDelete(boolean success, @NonNull Receipt receipt) throws Exception {
        if (success && receipt.hasFile()) {
            mStorageManager.delete(receipt.getFile());
        }
    }

    @NonNull
    private Receipt preInsertBlocking(@NonNull Receipt receipt) {
        final ReceiptBuilderFactory builder = mReceiptBuilderFactoryFactory.build(receipt);

        final int rcptNum = mReceiptsTable.get(receipt.getTrip()).toBlocking().first().size() + 1;
        final StringBuilder stringBuilder = new StringBuilder(rcptNum + "_");
        stringBuilder.append(FileUtils.omitIllegalCharactersFromFileName(receipt.getName().trim()));
        final File file = receipt.getFile();
        if (file != null) {
            stringBuilder.append('.').append(StorageManager.getExtension(receipt.getFile()));
            final String newName = stringBuilder.toString();
            final File renamedFile = mStorageManager.getFile(receipt.getTrip().getDirectory(), newName);
            if (!renamedFile.exists()) {
                Log.i(TAG, "Changing image name from: " + file.getName() + " to: " + newName);
                builder.setFile(mStorageManager.rename(file, newName)); // Returns oldFile on failure
            }
        }

        return builder.build();
    }
}
