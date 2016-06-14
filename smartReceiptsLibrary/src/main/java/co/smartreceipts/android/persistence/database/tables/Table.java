package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.persistence.DatabaseHelper;

/**
 * Acts as a standard contract for the methods that all database tables should support, such as CRUD operations
 * and create/upgrade scripts
 *
 * @param <T> the model object that this table will interact with
 */
public interface Table<T> {

    /**
     * @return the table name for SQL operations
     */
    @NonNull
    String getTableName();

    /**
     * Called when this table is first created to allow us to build it out
     *
     * @param db the {@link SQLiteDatabase} that database operations can be performed on
     * @param customizer a {@link TableDefaultsCustomizer} to allow us to insert the reasonable defaults
     */
    void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer);

    /**
     * Called whenever we upgrade our underlying database version
     *
     * @param db the {@link SQLiteDatabase} that database operations can be performed on
     * @param oldVersion the old database version
     * @param newVersion the new database version
     * @param customizer a {@link TableDefaultsCustomizer} to allow us to insert the reasonable defaults
     */
    void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer);

    /**
     * Called after {@link #onCreate(SQLiteDatabase, TableDefaultsCustomizer)} and {@link #onUpgrade(SQLiteDatabase, int, int, TableDefaultsCustomizer)}
     * complete (if either was required) to allow us to perform clean-up operations
     */
    void onPostCreateUpgrade();

    /**
     * Retrieves list of all objects that are stored within this table. Please note that this is a blocking operation
     *
     * @return a {@link List} of all objects of type {@link T} that are stored within this table
     */
    @NonNull
    List<T> get();

    /**
     * Inserts a new object of type {@link T} into this table. Please note that this is a blocking operation
     *
     * @param t the object to insert
     * @return the inserted object of type {@link T} or {@code null} if the insert failed
     */
    @Nullable
    T insert(@NonNull T t);

    /**
     * Updates an existing object of type {@link T} in this table. Please note that this is a blocking operation
     *
     * @param oldT the old object that will be replaced
     * @param newT the new object that will take the place of the old one
     * @return the updated object of type {@link T} or {@code null} if the update failed
     */
    @Nullable
    T update(@NonNull T oldT, @NonNull T newT);

    /**
     * Removes an existing object of type {@link T} from this table. Please note that this is a blocking operation
     * @param t the object to remove
     * @return {@code true} if we successfully deleted this item. {@code false} otherwise
     */
    boolean delete(@NonNull T t);
}
