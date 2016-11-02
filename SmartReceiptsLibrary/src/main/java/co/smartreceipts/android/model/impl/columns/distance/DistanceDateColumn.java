package co.smartreceipts.android.model.impl.columns.distance;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistanceDateColumn extends AbstractColumnImpl<Distance> {

    private final Context mContext;
    private final Preferences mPreferences;

    public DistanceDateColumn(int id, @NonNull String name, @NonNull SyncState syncState, @NonNull Context context, @NonNull Preferences preferences) {
        super(id, name, syncState);
        mContext = context;
        mPreferences = preferences;
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getFormattedDate(mContext, mPreferences.getDateSeparator());
    }

}
