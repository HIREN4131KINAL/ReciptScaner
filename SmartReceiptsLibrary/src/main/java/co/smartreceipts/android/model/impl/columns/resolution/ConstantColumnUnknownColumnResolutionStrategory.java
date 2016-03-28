package co.smartreceipts.android.model.impl.columns.resolution;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.UnknownColumnResolutionStrategory;
import co.smartreceipts.android.model.impl.columns.ConstantColumn;

/**
 * Provides a default implementation of the {@link co.smartreceipts.android.model.UnknownColumnResolutionStrategory}
 * that always returns a {@link co.smartreceipts.android.model.impl.columns.ConstantColumn}
 */
public final class ConstantColumnUnknownColumnResolutionStrategory<T> implements UnknownColumnResolutionStrategory<T> {

    @Override
    public Column<T> resolve(int id, @NonNull String columnName) {
        return new ConstantColumn<T>(id, columnName);
    }
}

