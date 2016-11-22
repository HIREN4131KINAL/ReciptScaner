package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.tables.TripForeignKeyAbstractSqlTable;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.TripForeignKeyTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.TableActionAlterations;
import co.smartreceipts.android.utils.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

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
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTableActionAlterations.preGet()
                .flatMap(new Func1<Void, Observable<List<ModelType>>>() {
                    @Override
                    public Observable<List<ModelType>> call(Void oVoid) {
P
                        return mTripForeignKeyTable.get(trip, isDescending);
                    }
                })
                .doOnNext(new Action1<List<ModelType>>() {
                    @Override
                    public void call(List<ModelType> modelTypes) {
                        try {
                            mTableActionAlterations.postGet(modelTypes);
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(new Action1<List<ModelType>>() {
                    @Override
                    public void call(List<ModelType> modelTypes) {
                        if (modelTypes != null) {
                            Logger.debug(TripForeignKeyAbstractTableController.this, "#onGetSuccess - onNext");
                            for (final TripForeignKeyTableEventsListener<ModelType> foreignTableEventsListener : mForeignTableEventsListeners) {
                                foreignTableEventsListener.onGetSuccess(modelTypes, trip);
                            }
                        }
                        else {
                            Logger.debug(TripForeignKeyAbstractTableController.this, "#onGetFailure - onNext");
                            for (final TripForeignKeyTableEventsListener<ModelType> foreignTableEventsListener : mForeignTableEventsListeners) {
                                foreignTableEventsListener.onGetFailure(null, trip);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(TripForeignKeyAbstractTableController.this, throwable));
                        Logger.error(TripForeignKeyAbstractTableController.this, "#onGetFailure - onError");
                        for (final TripForeignKeyTableEventsListener<ModelType> foreignTableEventsListener : mForeignTableEventsListeners) {
                            foreignTableEventsListener.onGetFailure(throwable, trip);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(TripForeignKeyAbstractTableController.this, "#get - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }
}
