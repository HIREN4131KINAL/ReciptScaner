package co.smartreceipts.android.persistence.database.tables.controllers;

import android.support.annotation.NonNull;

import rx.Subscription;

/**
 * Provides an asynchronous way for us to easily interact with our database layer
 *
 * @param <ModelType> the model object type that this will be used to create
 */
public interface TableController<ModelType> {

    /**
     * Registers a listener that will receive appropriate callbacks based on table events
     *
     * @param tableEventsListener the {@link TableEventsListener} that can react to table actions
     */
    void registerListener(@NonNull TableEventsListener<ModelType> tableEventsListener);

    /**
     * Removes a listener that was previously registered, so it will no longer receive events
     *
     * @param tableEventsListener the {@link TableEventsListener} to be unregistered
     */
    void unregisterListener(@NonNull TableEventsListener<ModelType> tableEventsListener);

    /**
     * Retrieves list of all objects that are stored within this table.
     */
    @NonNull
    Subscription get();

    /**
     * Inserts a new object of type {@link ModelType} into this table.
     *
     * @param modelType the object to insert
     */
    @NonNull
    Subscription insert(@NonNull ModelType modelType);

    /**
     * Updates an existing object of type {@link ModelType} in this table.
     *
     * @param oldModelType the old object that will be replaced
     * @param newModelType the new object that will take the place of the old one
     */
    @NonNull
    Subscription update(@NonNull ModelType oldModelType, @NonNull ModelType newModelType);

    /**
     * Removes an existing object of type {@link ModelType} from this table.
     *
     * @param modelType the object to remove
     */
    @NonNull
    Subscription delete(@NonNull ModelType modelType);
}
