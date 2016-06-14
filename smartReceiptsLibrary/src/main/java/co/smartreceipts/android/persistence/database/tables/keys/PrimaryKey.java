package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;

/**
 * Provides a definition around the SQL primary key for a particular table
 *
 * @param <ModelType> the model object type that this table will create
 * @param <KeyType> the class type that represents the key (e.g. {@link Integer}, {@link String}).
 */
public interface PrimaryKey<ModelType, KeyType> {

    /**
     * @return the column name that contains the primary key
     */
    @NonNull
    String getPrimaryKeyColumn();

    /**
     * @return the class type that represents the key (e.g. {@link Integer}, {@link String}).
     */
    @NonNull
    Class<KeyType> getPrimaryKeyClass();

    /**
     * Gets the value of the primary key for this {@link ModelType}
     *
     * @param modelType the model type to get the primary key value for
     * @return the {@link KeyType}
     */
    @NonNull
    KeyType getPrimaryKeyValue(@NonNull ModelType modelType);




}
