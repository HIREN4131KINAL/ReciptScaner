package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Provides a column that returns blank values for everything but the header
 */
public final class BlankColumn<T> extends AbstractColumnImpl<T> {

    public BlankColumn(int id, @NonNull String name) {
        super(id, name);
    }

    @Override
    public String getValue(@NonNull T rowItem) {
        return "";
    }

    @Override
    public String getFooter(@NonNull List<T> rows) {
        return "";
    }
}
