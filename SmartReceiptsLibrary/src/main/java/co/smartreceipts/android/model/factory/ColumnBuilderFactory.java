package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.UnknownColumnResolutionStrategory;
import co.smartreceipts.android.model.impl.columns.resolution.ConstantColumnUnknownColumnResolutionStrategory;

/**
 * A {@link co.smartreceipts.android.model.Column} {@link co.smartreceipts.android.model.factory.BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Column} objects
 */
public final class ColumnBuilderFactory<T> implements BuilderFactory<Column<T>> {

    private final ColumnDefinitions<T> mColumnDefinitions;
    private final UnknownColumnResolutionStrategory<T> mUnknownColumnResolutionStrategory;
    private int mId;
    private String mColumnName;

    public ColumnBuilderFactory(@NonNull ColumnDefinitions<T> columnDefinitions) {
        this(columnDefinitions, new ConstantColumnUnknownColumnResolutionStrategory<T>());
    }

    public ColumnBuilderFactory(@NonNull ColumnDefinitions<T> columnDefinitions, @NonNull UnknownColumnResolutionStrategory<T> unknownColumnResolutionStrategory) {
        mColumnDefinitions = columnDefinitions;
        mUnknownColumnResolutionStrategory = unknownColumnResolutionStrategory;
        mId = Column.UNKNOWN_ID;
        mColumnName = "";
    }

    public ColumnBuilderFactory<T> setColumnId(int id) {
        mId = id;
        return this;
    }

    public ColumnBuilderFactory<T> setColumnName(Column<T> column) {
        mColumnName = (column != null) ? column.getName() : "";
        return this;
    }

    public ColumnBuilderFactory<T> setColumnName(String name) {
        mColumnName = (name != null) ? name : "";
        return this;
    }


    @NonNull
    @Override
    public Column<T> build() {
        final Column<T> column = mColumnDefinitions.getColumn(mId, mColumnName);
        return (column != null) ? column : mUnknownColumnResolutionStrategory.resolve(mId, mColumnName);
    }

}
