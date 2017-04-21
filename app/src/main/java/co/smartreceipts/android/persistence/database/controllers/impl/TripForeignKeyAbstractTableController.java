package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.TripForeignKeyTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.TableActionAlterations;
import co.smartreceipts.android.persistence.database.tables.TripForeignKeyAbstractSqlTable;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;


/**
 * Provides a top-level implementation of the {@link TableController} contract for {@link TripForeignKeyAbstractSqlTable}
 * instances
 *
 * @param <ModelType> the model object type that this will be used to create
 */
public class TripForeignKeyAbstractTableController<ModelType> extends AbstractTableController<ModelType> {

    private final CopyOnWriteArrayList<TripForeignKeyTableEventsListener<ModelType>> mForeignTableEventsListeners = new CopyOnWriteArrayList<>();
    protected final TripForeignKeyAbstractSqlTable<ModelType, ?> mTripForeignKeyTable;

    public TripForeignKeyAbstractTableController(@NonNull TripForeignKeyAbstractSqlTable<ModelType, ?> table, @NonNull Analytics analytics) {
        super(table, analytics);
        mTripForeignKeyTable = table;
    }

    public TripForeignKeyAbstractTableController(@NonNull TripForeignKeyAbstractSqlTable<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations, @NonNull Analytics analytics) {
        super(table, tableActionAlterations, analytics);
        mTripForeignKeyTable = table;
    }

    TripForeignKeyAbstractTableController(@NonNull TripForeignKeyAbstractSqlTable<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations, @NonNull Analytics analytics, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        super(table, tableActionAlterations, analytics, subscribeOnScheduler, observeOnScheduler);
        mTripForeignKeyTable = table;
    }

    @Override
    public synchronized void subscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        super.subscribe(tableEventsListener);
        if (tableEventsListener instanceof TripForeignKeyTableEventsListener) {
            mForeignTableEventsListeners.add((TripForeignKeyTableEventsListener<ModelType>) tableEventsListener);
        }
    }

    @Override
    public synchronized void unsubscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        if (tableEventsListener instanceof TripForeignKeyTableEventsListener) {
            mForeignTableEventsListeners.remove(tableEventsListener);
        }
        super.unsubscribe(tableEventsListener);
    }

    /**
     * Retrieves list of all objects that are stored within this table for a particular {@link Trip}
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     */
    public synchronized void get(@NonNull Trip trip) {
        get(trip, true);
    }

    /**
     * Retrieves list of all objects that are stored within this table for a particular {@link Trip}
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @param isDescending {@code true} for descending order, {@code false} for ascending
     */
    public synchronized void get(@NonNull final Trip trip, final  boolean isDescending) {
        // TODO: #preGet should really have the foreign key param... Which means all tables should be foreign key friendly...
        Logger.info(this, "#get: {}", trip);
        final AtomicReference<Disposable> subscriptionRef = new AtomicReference<>();
        final Disposable disposable = mTableActionAlterations.preGet()
                .andThen(mTripForeignKeyTable.get(trip, isDescending))
                .subscribeOn(mSubscribeOnScheduler)
                .doOnSuccess(modelTypes -> {
                    try {
                        mTableActionAlterations.postGet(modelTypes);
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .observeOn(mObserveOnScheduler)
                .subscribe(modelTypes -> {
                    if (modelTypes != null) {
                        Logger.debug(TripForeignKeyAbstractTableController.this, "#onGetSuccess - onSuccess");
                        for (final TripForeignKeyTableEventsListener<ModelType> foreignTableEventsListener : mForeignTableEventsListeners) {
                            foreignTableEventsListener.onGetSuccess(modelTypes, trip);
                        }
                    }
                    else {
                        Logger.debug(TripForeignKeyAbstractTableController.this, "#onGetFailure - onSuccess");
                        for (final TripForeignKeyTableEventsListener<ModelType> foreignTableEventsListener : mForeignTableEventsListeners) {
                            foreignTableEventsListener.onGetFailure(null, trip);
                        }
                    }
                    unsubscribeReference(subscriptionRef);
                }, throwable -> {
                    mAnalytics.record(new ErrorEvent(TripForeignKeyAbstractTableController.this, throwable));
                    Logger.error(TripForeignKeyAbstractTableController.this, "#onGetFailure - onError");
                    for (final TripForeignKeyTableEventsListener<ModelType> foreignTableEventsListener : mForeignTableEventsListeners) {
                        foreignTableEventsListener.onGetFailure(throwable, trip);
                    }
                    unsubscribeReference(subscriptionRef);
                });

        subscriptionRef.set(disposable);
        compositeDisposable.add(disposable);
    }
}
