package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.columns.CategoriesTableColumns;
import co.smartreceipts.android.utils.sorting.CategoryNameComparator;

public final class CategoriesTable extends AbstractSqlTable<Category> {

    private HashMap<String, Category> mCategories;
    private ArrayList<Category> mCategoryList;

    public CategoriesTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        super(sqLiteOpenHelper);
    }

    @Override
    public String getTableName() {
        return CategoriesTableColumns.TABLE_NAME;
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String categories = "CREATE TABLE " + getTableName() + " ("
                + CategoriesTableColumns.COLUMN_NAME + " TEXT PRIMARY KEY, "
                + CategoriesTableColumns.COLUMN_CODE + " TEXT, "
                + CategoriesTableColumns.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1"
                + ");";
        db.execSQL(categories);
        customizer.insertCategoryDefaults(this);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= 2) { 
            final String alterCategories = "ALTER TABLE " + getTableName() +
                                           " ADD " + CategoriesTableColumns.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1";
            db.execSQL(alterCategories);
        }
    }

    public synchronized List<Category> get() {
        if (mCategories == null) {
            buildCategories();
            mCategoryList = new ArrayList<>();
            for (final Map.Entry<String, Category> entry : mCategories.entrySet()) {
                mCategoryList.add(entry.getValue());
            }
            Collections.sort(mCategoryList, new CategoryNameComparator());
        }
        return new ArrayList<>(mCategoryList);
    }

    public synchronized String getCategoryCode(@NonNull CharSequence categoryName) {
        if (mCategories == null) {
            buildCategories();
        }
        return mCategories.get(categoryName).getCode();
    }

    public synchronized boolean insert(@NonNull Category category) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(2);
        values.put(CategoriesTableColumns.COLUMN_NAME, category.getName());
        values.put(CategoriesTableColumns.COLUMN_CODE, category.getCode());
        if (db.insertOrThrow(getTableName(), null, values) == -1) {
            return false;
        } else {
            if (mCategoryList != null && mCategories != null) {
                mCategories.put(category.getName(), category);
                mCategoryList.add(category);
                Collections.sort(mCategoryList, new CategoryNameComparator());
            }
            return true;
        }
    }

    public synchronized boolean update(@NonNull Category oldCategory, @NonNull Category newCategory) {
        final ContentValues values = new ContentValues(2);
        values.put(CategoriesTableColumns.COLUMN_NAME, newCategory.getName());
        values.put(CategoriesTableColumns.COLUMN_CODE, newCategory.getCode());

        if (getWritableDatabase().update(getTableName(), values, CategoriesTableColumns.COLUMN_NAME + " = ?", new String[]{oldCategory.getName()}) == 0) {
            return false;
        } else {
            mCategoryList.remove(oldCategory);
            mCategories.put(newCategory.getName(), newCategory);
            mCategoryList.add(newCategory);
            Collections.sort(mCategoryList, new CategoryNameComparator());
            return true;
        }
    }

    public synchronized boolean delete(@NonNull Category category) {
        final boolean success = (getWritableDatabase().delete(getTableName(), CategoriesTableColumns.COLUMN_NAME + " = ?", new String[]{category.getName()}) > 0);
        if (success) {
            final Category oldCategory = mCategories.remove(category.getName());
            mCategoryList.remove(oldCategory);
        }
        return success;
    }

    private void buildCategories() {
        mCategories = new HashMap<>();
        Cursor c = null;
        try {
            final SQLiteDatabase db = this.getReadableDatabase();
            c = db.query(getTableName(), null, null, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                final int nameIndex = c.getColumnIndex(CategoriesTableColumns.COLUMN_NAME);
                final int codeIndex = c.getColumnIndex(CategoriesTableColumns.COLUMN_CODE);
                do {
                    final String name = c.getString(nameIndex);
                    final String code = c.getString(codeIndex);
                    mCategories.put(name, new CategoryBuilderFactory().setName(name).setCode(code).build());
                }
                while (c.moveToNext());
            }
        } finally { // Close the cursor and db to avoid memory leaks
            if (c != null) {
                c.close();
            }
        }
    }

}
