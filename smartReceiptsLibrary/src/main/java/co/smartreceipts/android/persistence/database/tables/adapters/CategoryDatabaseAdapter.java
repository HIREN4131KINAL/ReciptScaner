package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link co.smartreceipts.android.persistence.database.tables.CategoriesTable}
 */
public final class CategoryDatabaseAdapter implements DatabaseAdapter<Category, PrimaryKey<Category, String>> {

    @Override
    @NonNull
    public Category read(@NonNull Cursor cursor) {
        final int nameIndex = cursor.getColumnIndex(CategoriesTable.COLUMN_NAME);
        final int codeIndex = cursor.getColumnIndex(CategoriesTable.COLUMN_CODE);

        final String name = cursor.getString(nameIndex);
        final String code = cursor.getString(codeIndex);
        return new CategoryBuilderFactory().setName(name).setCode(code).build();
    }

    @Override
    @NonNull
    public ContentValues write(@NonNull Category category) {
        final ContentValues values = new ContentValues();
        values.put(CategoriesTable.COLUMN_NAME, category.getName());
        values.put(CategoriesTable.COLUMN_CODE, category.getCode());
        return values;
    }

    @Override
    @NonNull
    public Category build(@NonNull Category category, @NonNull PrimaryKey<Category, String> primaryKey) {
        return new CategoryBuilderFactory().setName(primaryKey.getPrimaryKeyValue(category)).setCode(category.getCode()).build();
    }

}
