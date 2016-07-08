package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
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

}
