package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.StubTableActionAlterations;
import co.smartreceipts.android.persistence.database.controllers.alterations.TableActionAlterations;
import co.smartreceipts.android.utils.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Provides a top-level implementation of the {@link TableController} contract
 *
 * @param <ModelType> the model object type that this will be used to create
 */
abstract class AbstractTableController<ModelType> implements TableController<ModelType> {

    protected final String TAG = getClass().getSimpleName();

    private final Table<ModelType, ?> mTable;
    protected final CopyOnWriteArrayList<TableEventsListener<ModelType>> mTableEventsListeners;
    protected final TableActionAlterations<ModelType> mTableActionAlterations;
    protected final Analytics mAnalytics;
    protected final Scheduler mSubscribeOnScheduler;
    protected final Scheduler mObserveOnScheduler;

    protected CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull Analytics analytics) {
        this(table, new StubTableActionAlterations<ModelType>(), analytics);
    }

    public AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations, @NonNull Analytics analytics) {
        this(table, tableActionAlterations, analytics, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations,
                            @NonNull Analytics analytics, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        mTable = Preconditions.checkNotNull(table);
        mTableActionAlterations = Preconditions.checkNotNull(tableActionAlterations);
        mAnalytics = Preconditions.checkNotNull(analytics);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mTableEventsListeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public synchronized void subscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        if (mTableEventsListeners.isEmpty()) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mTableEventsListeners.add(tableEventsListener);
    }

    @Override
    public synchronized void unsubscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        mTableEventsListeners.remove(tableEventsListener);
        if (mTableEventsListeners.isEmpty()) {
            mCompositeSubscription.unsubscribe();
        }
    }

    @Override
    public synchronized void get() {
        Logger.info(this, "#get");
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTableActionAlterations.preGet()
                .flatMap(new Func1<Void, Observable<List<ModelType>>>() {
                    @Override
                    public Observable<List<ModelType>> call(Void oVoid) {
                        return mTable.get();
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
                            Logger.debug(AbstractTableController.this, "#onGetSuccess - onNext");
                            for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onGetSuccess(modelTypes);
                            }
                        } else {
                            Logger.debug(AbstractTableController.this, "#onGetFailure - onNext");
                            for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onGetFailure(null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                        Logger.error(AbstractTableController.this, "#onGetFailure - onError", throwable);
                        for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                            tableEventsListener.onGetFailure(throwable);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(AbstractTableController.this, "#get - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    @Override
    public synchronized void insert(@NonNull final ModelType insertModelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        Logger.info(this, "#insert: {}", insertModelType);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTableActionAlterations.preInsert(insertModelType)
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType modelType) {
                        return mTable.insert(modelType, databaseOperationMetadata);
                    }
                })
                .doOnNext(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType modelType) {
                        try {
                            mTableActionAlterations.postInsert(modelType);
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType modelType) {
                        if (modelType != null) {
                            Logger.debug(AbstractTableController.this, "#onInsertSuccess - onNext");
                            for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onInsertSuccess(modelType, databaseOperationMetadata);
                            }
                        } else {
                            Logger.debug(AbstractTableController.this, "#onInsertFailure - onNext");
                            for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onInsertFailure(insertModelType, null, databaseOperationMetadata);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                        Logger.error(AbstractTableController.this, "#onInsertFailure - onError", throwable);
                        for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                            tableEventsListener.onInsertFailure(insertModelType, throwable, databaseOperationMetadata);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(AbstractTableController.this, "#insert - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    @Override
    public synchronized void update(@NonNull final ModelType oldModelType, @NonNull ModelType newModelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        Logger.info(this, "#update: {}; {}", oldModelType, newModelType);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTableActionAlterations.preUpdate(oldModelType, newModelType)
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType modelType) {
                        return mTable.update(oldModelType, modelType, databaseOperationMetadata);
                    }
                })
                .doOnNext(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType modelType) {
                        try {
                            mTableActionAlterations.postUpdate(oldModelType, modelType);
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType modelType) {
                        if (modelType != null) {
                            Logger.debug(AbstractTableController.this, "#onUpdateSuccess - onNext");
                            for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onUpdateSuccess(oldModelType, modelType, databaseOperationMetadata);
                            }
                        } else {
                            Logger.debug(AbstractTableController.this, "#onUpdateFailure - onNext");
                            for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onUpdateFailure(oldModelType, null, databaseOperationMetadata);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                        Logger.error(AbstractTableController.this, "#onUpdateFailure - onError", throwable);
                        for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                            tableEventsListener.onUpdateFailure(oldModelType, throwable, databaseOperationMetadata);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(AbstractTableController.this, "#update - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    @Override
    public synchronized void delete(@NonNull final ModelType modelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        Logger.info(this, "#delete: {}", modelType);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTableActionAlterations.preDelete(modelType)
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType modelType) {
                        return mTable.delete(modelType, databaseOperationMetadata);
                    }
                })
                .doOnNext(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType deletedItem) {
                        try {
                            mTableActionAlterations.postDelete(deletedItem);
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType deleteItem) {
                        if (deleteItem != null) {
                            Logger.debug(AbstractTableController.this, "#onDeleteSuccess - onNext");
                            for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onDeleteSuccess(deleteItem, databaseOperationMetadata);
                            }
                        } else {
                            Logger.debug(AbstractTableController.this, "#onDeleteFailure - onNext");
                            for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onDeleteFailure(modelType, null, databaseOperationMetadata);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                        Logger.error(AbstractTableController.this, "#onDeleteFailure - onError", throwable);
                        for (final TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                            tableEventsListener.onDeleteFailure(modelType, throwable, databaseOperationMetadata);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(AbstractTableController.this, "#delete - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    protected void unsubscribeReference(@NonNull AtomicReference<Subscription> subscriptionReference) {
        final Subscription subscription = subscriptionReference.get();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}
