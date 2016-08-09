package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import rx.Observable;

/**
 * Acts as a standard contract for the methods that all database tables should support, such as CRUD operations
 * and create/upgrade scripts
 *
 * @param <ModelType> the model object that this table will interact with
 * @param <PrimaryKeyType> the primary key type (e.g. Integer, String) that is used by the primary key column
 */
public interface Table<ModelType, PrimaryKeyType> {

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
     * @return an {@link Observable} with: a {@link List} of all objects of type {@link ModelType} that are stored within this table
     */
    @NonNull
    Observable<List<ModelType>> get();

    /**
     * Attempts to look up an object based on it's {@link PrimaryKeyType} value for it's primary key column
     *
     * @param primaryKeyType the primary key for this object
     * @return an {@link Observable} with: the {@link ModelType} object or {@code null} if none was found
     */
    @NonNull
    Observable<ModelType> findByPrimaryKey(@NonNull PrimaryKeyType primaryKeyType);

    /**
     * Inserts a new object of type {@link ModelType} into this table. Please note that this is a blocking operation
     *
     * @param modelType the object to insert
     * @return an {@link Observable} with: the inserted object of type {@link ModelType} or {@code null} if the insert failed
     */
    @NonNull
    Observable<ModelType> insert(@NonNull ModelType modelType);

    /**
     * Updates an existing object of type {@link ModelType} in this table. Please note that this is a blocking operation
     *
     * @param oldModelType the old object that will be replaced
     * @param newModelType the new object that will take the place of the old one
     * @return an {@link Observable} with: the updated object of type {@link ModelType} or {@code null} if the update failed
     */
    @NonNull
    Observable<ModelType> update(@NonNull ModelType oldModelType, @NonNull ModelType newModelType);

    /**
     * Removes an existing object of type {@link ModelType} from this table. Please note that this is a blocking operation
     * @param modelType the object to remove
     * @return an {@link Observable} with: {@code true} if we successfully deleted this item. {@code false} otherwise
     */
    @NonNull
    Observable<Boolean> delete(@NonNull ModelType modelType);
}
