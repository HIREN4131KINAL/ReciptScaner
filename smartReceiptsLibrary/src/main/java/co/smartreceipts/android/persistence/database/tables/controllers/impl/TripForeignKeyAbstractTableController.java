package co.smartreceipts.android.persistence.database.tables.controllers.impl;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.tables.controllers.TableController;
import co.smartreceipts.android.persistence.database.tables.TripForeignKeyAbstractSqlTable;
import co.smartreceipts.android.persistence.database.tables.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.tables.controllers.TripForeignKeyTableEventsListener;
import co.smartreceipts.android.persistence.database.tables.controllers.alterations.TableActionAlterations;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides a top-level implementation of the {@link TableController} contract for {@link TripForeignKeyAbstractSqlTable}
 * instances
 *
 * @param <ModelType> the model object type that this will be used to create
 */
public class TripForeignKeyAbstractTableController<ModelType> extends AbstractTableController<ModelType> {

    private final TripForeignKeyAbstractSqlTable<ModelType, ?> mTripForeignKeyTable;
    private final CopyOnWriteArrayList<TripForeignKeyTableEventsListener<ModelType>> mForeignTableEventsListeners = new CopyOnWriteArrayList<>();

    public TripForeignKeyAbstractTableController(@NonNull TripForeignKeyAbstractSqlTable<ModelType, ?> table) {
        super(table);
        mTripForeignKeyTable = table;
    }

    public TripForeignKeyAbstractTableController(@NonNull TripForeignKeyAbstractSqlTable<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations) {
        super(table, tableActionAlterations);
        mTripForeignKeyTable = table;
    }

    TripForeignKeyAbstractTableController(@NonNull TripForeignKeyAbstractSqlTable<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        super(table, tableActionAlterations, subscribeOnScheduler, observeOnScheduler);
        mTripForeignKeyTable = table;
    }

    @Override
    public void registerListener(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        super.registerListener(tableEventsListener);
        if (tableEventsListener instanceof TripForeignKeyTableEventsListener) {
            mForeignTableEventsListeners.add((TripForeignKeyTableEventsListener<ModelType>)tableEventsListener);
        }
    }

    @Override
    public void unregisterListener(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        super.unregisterListener(tableEventsListener);
        if (tableEventsListener instanceof TripForeignKeyTableEventsListener) {
            mForeignTableEventsListeners.remove(tableEventsListener);
        }
    }

    /**
     * Retrieves list of all objects that are stored within this table for a particular {@link Trip}
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     */
    @NonNull
    public Subscription get(@NonNull Trip trip) {
        return get(trip, true);
    }

    /**
     * Retrieves list of all objects that are stored within this table for a particular {@link Trip}
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @param isDescending {@code true} for descending order, {@code false} for ascending
     */
    @NonNull
    public Subscription get(@NonNull final Trip trip, final  boolean isDescending) {
        // TODO: #preGet should really have the foreign key param... Which means all tables should be foreign key friendly...
        return mTableActionAlterations.preGet()
                .flatMap(new Func1<Void, Observable<List<ModelType>>>() {
                    @Override
                    public Observable<List<ModelType>> call(Void oVoid) {
                        return mTripForeignKeyTable.get(trip, isDescending);
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
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
                .subscribe(new Action1<List<ModelType>>() {
                    @Override
                    public void call(List<ModelType> modelTypes) {
                        if (modelTypes != null) {
                            for (TripForeignKeyTableEventsListener<ModelType> tableEventsListener : mForeignTableEventsListeners) {
                                tableEventsListener.onGetSuccess(modelTypes, trip);
                            }
                        }
                        else {
                            for (TripForeignKeyTableEventsListener<ModelType> tableEventsListener : mForeignTableEventsListeners) {
                                tableEventsListener.onGetFailure(null, trip);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        for (TripForeignKeyTableEventsListener<ModelType> tableEventsListener : mForeignTableEventsListeners) {
                            tableEventsListener.onGetFailure(throwable, trip);
                        }
                    }
                });
    }
}
