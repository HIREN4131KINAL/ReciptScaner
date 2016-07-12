package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import rx.Observable;

public class ReceiptTableActionAlterations extends StubTableActionAlterations<Receipt> {

    @NonNull
    @Override
    public Observable<Receipt> preInsert(@NonNull Receipt receipt) {
        return super.preInsert(receipt);
        /** TODO: This part
         *         final int rcptNum = this.getReceiptsSerial(receipt.getTrip()).size() + 1; // Use this to order things more properly

         * final StringBuilder stringBuilder = new StringBuilder(rcptNum + "_");
         * stringBuilder.append(FileUtils.omitIllegalCharactersFromFileName(receipt.getName().trim()));
         File file = receipt.getFile();
         if (file != null) {
         stringBuilder.append('.').append(StorageManager.getExtension(receipt.getFile()));
         final String newName = stringBuilder.toString();
         File renamedFile = mPersistenceManager.getStorageManager().getFile(receipt.getTrip().getDirectory(), newName);
         if (!renamedFile.exists()) { // If this file doesn't exist, let's rename our current one
         Log.e(TAG, "Changing image name from: " + receipt.getFile().getName() + " to: " + newName);
         file = mPersistenceManager.getStorageManager().rename(file, newName); // Returns oldFile on failure
         }
         values.put(ReceiptsTable.COLUMN_PATH, file.getName());
         }
         */
    }
}
