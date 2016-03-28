package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import co.smartreceipts.android.persistence.Preferences;

/**
 * Provides a column that returns blank values for everything but the header
 */
public final class SettingUserIdColumn<T> extends AbstractColumnImpl<T> {

    private final Preferences mPreferences;

    public SettingUserIdColumn(int id, @NonNull String name, @NonNull Preferences preferences) {
        super(id, name);
        mPreferences = preferences;
    }

    @Override
    public String getValue(@NonNull T rowItem) {
        return mPreferences.getUserID();
    }

}
