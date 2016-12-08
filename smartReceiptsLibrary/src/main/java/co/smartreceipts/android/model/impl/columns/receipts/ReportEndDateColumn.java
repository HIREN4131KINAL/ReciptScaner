package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReportEndDateColumn extends AbstractColumnImpl<Receipt> {

    private final Context mContext;
    private final Preferences mPreferences;

    public ReportEndDateColumn(int id, @NonNull String name, @NonNull SyncState syncState, @NonNull Context context, @NonNull Preferences preferences) {
        super(id, name, syncState);
        mContext = context;
        mPreferences = preferences;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getTrip().getFormattedEndDate(mContext, mPreferences.getDateSeparator());
    }
}
