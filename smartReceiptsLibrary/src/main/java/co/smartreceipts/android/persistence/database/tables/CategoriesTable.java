package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.persistence.database.tables.adapters.CategoryDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.CategoryPrimaryKey;

/**
 * Stores all database operations related to the {@link Category} model object
 */
public final class CategoriesTable extends AbstractSqlTable<Category, String> {

    // SQL Definitions:
    public static final String TABLE_NAME = "categories";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_BREAKDOWN = "breakdown";


    private static final String TAG = CategoriesTable.class.getSimpleName();

    public CategoriesTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        super(sqLiteOpenHelper, TABLE_NAME, new CategoryDatabaseAdapter(), new CategoryPrimaryKey());
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String categories = "CREATE TABLE " + getTableName() + " ("
                + COLUMN_NAME + " TEXT PRIMARY KEY, "
                + COLUMN_CODE + " TEXT, "
                + COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1"
                + ");";

        Log.d(TAG, categories);
        db.execSQL(categories);
        customizer.insertCategoryDefaults(this);
    }

    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= 2) { 
            final String alterCategories = "ALTER TABLE " + getTableName() +
                                           " ADD " + COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1";
            db.execSQL(alterCategories);
        }
    }

    /**
     * Attempts to get a category code for a known category name
     *
     * @param categoryName - the name of the desired {@link Category}
     * @return a category code {@link String} if the name matches a known category or {@code null} if none is found
     */
    @Nullable
    public synchronized String getCategoryCode(@NonNull CharSequence categoryName) {
        final List<Category> categories = new ArrayList<>(get());
        final int size = categories.size();
        for (int i = 0; i < size; i++) {
            final Category category = categories.get(i);
            if (categoryName.equals(category.getName())) {
                return category.getCode();
            }
        }
        return null;
    }

}
