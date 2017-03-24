package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;

/**
 * Defines the primary key for the {@link ReceiptsTable}
 */
public final class ReceiptPrimaryKey implements PrimaryKey<Receipt, Integer> {

    @Override
    @NonNull
    public String getPrimaryKeyColumn() {
        return ReceiptsTable.COLUMN_ID;
    }

    @Override
    @NonNull
    public Class<Integer> getPrimaryKeyClass() {
        return Integer.class;
    }

    @Override
    @NonNull
    public Integer getPrimaryKeyValue(@NonNull Receipt receipt) {
        return receipt.getId();
    }
}
