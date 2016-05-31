package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.persistence.database.tables.columns.CategoriesTableColumns;
import co.smartreceipts.android.utils.sorting.AlphabeticalCaseInsensitiveCharSequenceComparator;

public final class CategoriesTable extends AbstractSqlTable<Category> {

    private HashMap<String, String> mCategories;
    private ArrayList<CharSequence> mCategoryList;

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
        final String categories = "CREATE TABLE " + CategoriesTableColumns.TABLE_NAME + " ("
                + CategoriesTableColumns.COLUMN_NAME + " TEXT PRIMARY KEY, "
                + CategoriesTableColumns.COLUMN_CODE + " TEXT, "
                + CategoriesTableColumns.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1"
                + ");";
        db.execSQL(categories);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= 2) { 
            final String alterCategories = "ALTER TABLE " + CategoriesTableColumns.TABLE_NAME + " ADD " + CategoriesTableColumns.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1";
            db.execSQL(alterCategories);
        }
    }

    public synchronized ArrayList<CharSequence> getCategoriesList() {
        if (mCategories == null) {
            buildCategories();
            mCategoryList = new ArrayList<CharSequence>(mCategories.keySet());
            Collections.sort(mCategoryList, new AlphabeticalCaseInsensitiveCharSequenceComparator());
        }
        return mCategoryList;
    }

    public synchronized String getCategoryCode(@NonNull CharSequence categoryName) {
        if (mCategories == null || mCategories.size() == 0) {
            buildCategories();
        }
        return mCategories.get(categoryName);
    }

    public synchronized boolean insertCategory(final String name, final String code) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(2);
        values.put(CategoriesTableColumns.COLUMN_NAME, name);
        values.put(CategoriesTableColumns.COLUMN_CODE, code);
        if (db.insertOrThrow(getTableName(), null, values) == -1) {
            return false;
        } else {
            mCategories.put(name, code);
            mCategoryList.add(name);
            Collections.sort(mCategoryList, new AlphabeticalCaseInsensitiveCharSequenceComparator());
            return true;
        }
    }

    public synchronized boolean insertCategoryNoCache(final String name, final String code) throws SQLException {
        final ContentValues values = new ContentValues(2);
        values.put(CategoriesTableColumns.COLUMN_NAME, name);
        values.put(CategoriesTableColumns.COLUMN_CODE, code);

        if (getWritableDatabase().insertOrThrow(getTableName(), null, values) == -1) {
            return false;
        } else {
            return true;
        }
    }

    public synchronized boolean updateCategory(final String oldName, final String newName, final String newCode) {
        final ContentValues values = new ContentValues(2);
        values.put(CategoriesTableColumns.COLUMN_NAME, newName);
        values.put(CategoriesTableColumns.COLUMN_CODE, newCode);

        if (getWritableDatabase().update(getTableName(), values, CategoriesTableColumns.COLUMN_NAME + " = ?", new String[]{oldName}) == 0) {
            return false;
        } else {
            mCategories.remove(oldName);
            mCategoryList.remove(oldName);
            mCategories.put(newName, newCode);
            mCategoryList.add(newName);
            Collections.sort(mCategoryList, new AlphabeticalCaseInsensitiveCharSequenceComparator());
            return true;
        }
    }

    public synchronized boolean deleteCategory(final String name) {
        SQLiteDatabase db = null;
        db = this.getWritableDatabase();
        final boolean success = (db.delete(getTableName(), CategoriesTableColumns.COLUMN_NAME + " = ?", new String[]{name}) > 0);
        if (success) {
            mCategories.remove(name);
            mCategoryList.remove(name);
        }
        return success;
    }

    private void buildCategories() {
        mCategories = new HashMap<>();
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = this.getReadableDatabase();
            c = db.query(getTableName(), null, null, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                final int nameIndex = c.getColumnIndex(CategoriesTableColumns.COLUMN_NAME);
                final int codeIndex = c.getColumnIndex(CategoriesTableColumns.COLUMN_CODE);
                do {
                    final String name = c.getString(nameIndex);
                    final String code = c.getString(codeIndex);
                    mCategories.put(name, code);
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
