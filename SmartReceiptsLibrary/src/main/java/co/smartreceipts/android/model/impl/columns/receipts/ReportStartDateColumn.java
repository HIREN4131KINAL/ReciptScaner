package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.persistence.Preferences;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReportStartDateColumn extends AbstractColumnImpl<Receipt> {

    private final Context mContext;
    private final Preferences mPreferences;

    public ReportStartDateColumn(int id, @NonNull String name, @NonNull Context context, @NonNull Preferences preferences) {
        super(id, name);
        mContext = context;
        mPreferences = preferences;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getTrip().getFormattedStartDate(mContext, mPreferences.getDateSeparator());
    }
}
