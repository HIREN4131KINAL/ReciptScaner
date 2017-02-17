package co.smartreceipts.android.model.impl.columns.distance;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistanceDateColumn extends AbstractColumnImpl<Distance> {

    private final Context mContext;
    private final UserPreferenceManager mPreferences;

    public DistanceDateColumn(int id, @NonNull String name, @NonNull SyncState syncState, @NonNull Context context, @NonNull UserPreferenceManager preferences) {
        super(id, name, syncState);
        mContext = context;
        mPreferences = preferences;
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getFormattedDate(mContext, mPreferences.get(UserPreference.General.DateSeparator));
    }

}
