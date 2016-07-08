package co.smartreceipts.android.persistence.database.tables.controllers;

import android.support.annotation.NonNull;

/**
 * Provides an asynchronous way for us to easily interact with our database layer
 *
 * @param <ModelType> the model object type that this will be used to create
 */
public interface TableController<ModelType> {

    /**
     * Registers a listener that will receive appropriate callbacks based on table events
     */
    void subscribe(@NonNull TableEventsListener<ModelType> tableEventsListener);

    /**
     * Removes a listener that was previously registered, so it will no longer receive events
     **/
    void unsubscribe();

    /**
     * Retrieves list of all objects that are stored within this table.
     */
    void get();

    /**
     * Inserts a new object of type {@link ModelType} into this table.
     *
     * @param modelType the object to insert
     */
    void insert(@NonNull ModelType modelType);

    /**
     * Updates an existing object of type {@link ModelType} in this table.
     *
     * @param oldModelType the old object that will be replaced
     * @param newModelType the new object that will take the place of the old one
     */
    void update(@NonNull ModelType oldModelType, @NonNull ModelType newModelType);

    /**
     * Removes an existing object of type {@link ModelType} from this table.
     *
     * @param modelType the object to remove
     */
    void delete(@NonNull ModelType modelType);
}
