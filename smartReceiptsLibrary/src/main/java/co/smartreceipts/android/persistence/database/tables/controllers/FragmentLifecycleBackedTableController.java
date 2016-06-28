package co.smartreceipts.android.persistence.database.tables.controllers;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Provides a life-cycle safe implementation of the {@link TableController} contract for {@link android.support.v4.app.Fragment}
 * objects
 *
 * @param <ModelType> the model object type that this will be used to create
 */
public class FragmentLifecycleBackedTableController<ModelType> implements TableController<ModelType> {

    private final TableController<ModelType> mTableController;
    private CompositeSubscription mCompositeSubscription;

    public FragmentLifecycleBackedTableController(@NonNull TableController<ModelType> tableController) {
        mTableController = Preconditions.checkNotNull(tableController);
    }

    @Override
    public void registerListener(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        mTableController.registerListener(tableEventsListener);
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeSubscription();
        }
    }

    /**
     * Un-subscribes all current subscriptions (if no and unregisters the listener
     *
     * @param tableEventsListener the {@link TableEventsListener} to be unregistered
     */
    @Override
    public void unregisterListener(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        mTableController.unregisterListener(tableEventsListener);
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = null;
    }

    @NonNull
    @Override
    public Subscription get() {
        final Subscription subscription = mTableController.get();
        mCompositeSubscription.add(subscription);
        return subscription;
    }

    @NonNull
    @Override
    public Subscription insert(@NonNull ModelType modelType) {
        final Subscription subscription = mTableController.insert(modelType);
        mCompositeSubscription.add(subscription);
        return subscription;
    }

    @NonNull
    @Override
    public Subscription update(@NonNull ModelType oldModelType, @NonNull ModelType newModelType) {
        final Subscription subscription = mTableController.update(oldModelType, newModelType);
        mCompositeSubscription.add(subscription);
        return subscription;
    }

    @NonNull
    @Override
    public Subscription delete(@NonNull ModelType modelType) {
        final Subscription subscription = mTableController.delete(modelType);
        mCompositeSubscription.add(subscription);
        return subscription;
    }
}
