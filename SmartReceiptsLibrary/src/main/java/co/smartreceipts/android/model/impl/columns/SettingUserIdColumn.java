package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns blank values for everything but the header
 */
public final class SettingUserIdColumn<T> extends AbstractColumnImpl<T> {

    private final Preferences mPreferences;

    public SettingUserIdColumn(int id, @NonNull String name, @NonNull SyncState syncState, @NonNull Preferences preferences) {
        super(id, name, syncState);
        mPreferences = preferences;
    }

    @Override
    public String getValue(@NonNull T rowItem) {
        return mPreferences.getUserID();
    }

}
