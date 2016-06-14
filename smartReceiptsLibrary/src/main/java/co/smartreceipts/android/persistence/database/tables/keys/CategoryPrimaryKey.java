package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.persistence.database.tables.columns.CategoriesTableColumns;

/**
 * Defines the primary key for the {@link co.smartreceipts.android.persistence.database.tables.CategoriesTable}
 */
public final class CategoryPrimaryKey implements PrimaryKey<Category, String> {

    @Override
    @NonNull
    public String getPrimaryKeyColumn() {
        return CategoriesTableColumns.COLUMN_NAME;
    }

    @Override
    @NonNull
    public Class<String> getPrimaryKeyClass() {
        return String.class;
    }

    @Override
    @NonNull
    public String getPrimaryKeyValue(@NonNull Category category) {
        return category.getName();
    }
}
