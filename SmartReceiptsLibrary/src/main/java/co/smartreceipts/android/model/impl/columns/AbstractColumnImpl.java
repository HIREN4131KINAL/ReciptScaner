package co.smartreceipts.android.model.impl.columns;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Column;

/**
 * Provides an abstract implementation of the column contract to cover shared code
 */
public abstract class AbstractColumnImpl<T> implements Column<T>, Comparable<AbstractColumnImpl<T>> {

    private final int mId;
    private final String mName;

    public AbstractColumnImpl(int id, @NonNull String name) {
        mId = id;
        mName = name;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    @NonNull
    public final String getName() {
        return mName;
    }

    @Override
    public String getHeader() {
        return getName();
    }

    @Override
    public String getFooter(@NonNull List<T> rows) {
        return "";
    }

    @Override
    public int compareTo(@NonNull AbstractColumnImpl<T> otherColumn) {
        return getName().compareTo(otherColumn.getName());
    }

    @Override
    public String toString() {
        return getName();
    }

}
