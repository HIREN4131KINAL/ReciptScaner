package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Provides a column that returns a constant value for all fields
 */
public final class ConstantColumn<T> extends AbstractColumnImpl<T> {

    public ConstantColumn(int id, @NonNull String name) {
        super(id, name);
    }

    @Override
    public String getValue(@NonNull T rowItem) {
        return getName();
    }

    @Override
    public String getFooter(@NonNull List<T> rows) {
        return getName();
    }
}

