package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.StubTableActionAlterations;
import co.smartreceipts.android.persistence.database.controllers.alterations.TableActionAlterations;
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

    private final Table<ModelType, ?> mTable;
    protected final TableActionAlterations<ModelType> mTableActionAlterations;
    protected final Scheduler mSubscribeOnScheduler;
    protected final Scheduler mObserveOnScheduler;

    private TableEventsListener<ModelType> mTableEventsListener;
    protected CompositeSubscription mCompositeSubscription;

    public AbstractTableController(@NonNull Table<ModelType, ?> table) {
        this(table, new StubTableActionAlterations<ModelType>());
    }

    public AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations) {
        this(table, tableActionAlterations, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations,
                            @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        mTable = Preconditions.checkNotNull(table);
        mTableActionAlterations = Preconditions.checkNotNull(tableActionAlterations);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public synchronized void subscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        Preconditions.checkState(mTableEventsListener == null, "You must unsubscribe any existing listeners");
        mCompositeSubscription = new CompositeSubscription();
        mTableEventsListener = tableEventsListener;
    }

    @Override
    public synchronized void unsubscribe() {
        mTableEventsListener = null;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public synchronized void get() {
        final TableEventsListener<ModelType> tableEventsListener = mTableEventsListener;
        Preconditions.checkNotNull(tableEventsListener, "You must subscribe a table events listener");

        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTableActionAlterations.preGet()
                .flatMap(new Func1<Void, Observable<List<ModelType>>>() {
                    @Override
                    public Observable<List<ModelType>> call(Void oVoid) {
                        return mTable.get();
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
                            tableEventsListener.onGetSuccess(modelTypes);
                        } else {
                            tableEventsListener.onGetFailure(null);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        tableEventsListener.onGetFailure(throwable);
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    @Override
    public synchronized void insert(@NonNull final ModelType insertModelType) {
        final TableEventsListener<ModelType> tableEventsListener = mTableEventsListener;
        Preconditions.checkNotNull(tableEventsListener, "You must subscribe a table events listener");

        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTableActionAlterations.preInsert(insertModelType)
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType modelType) {
                        return mTable.insert(insertModelType);
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
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
                .subscribe(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType modelType) {
                        if (modelType != null) {
                            tableEventsListener.onInsertSuccess(modelType);
                        } else {
                            tableEventsListener.onInsertFailure(insertModelType, null);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        tableEventsListener.onInsertFailure(insertModelType, throwable);
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    @Override
    public synchronized void update(@NonNull final ModelType oldModelType, @NonNull ModelType newModelType) {
        final TableEventsListener<ModelType> tableEventsListener = mTableEventsListener;
        Preconditions.checkNotNull(tableEventsListener, "You must subscribe a table events listener");

        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTableActionAlterations.preUpdate(oldModelType, newModelType)
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType modelType) {
                        return mTable.update(oldModelType, modelType);
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
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
                .subscribe(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType modelType) {
                        if (modelType != null) {
                            tableEventsListener.onUpdateSuccess(oldModelType, modelType);
                        } else {
                            tableEventsListener.onUpdateFailure(oldModelType, null);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        tableEventsListener.onUpdateFailure(oldModelType, throwable);
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    @Override
    public synchronized void delete(@NonNull final ModelType modelType) {
        final TableEventsListener<ModelType> tableEventsListener = mTableEventsListener;
        Preconditions.checkNotNull(tableEventsListener, "You must subscribe a table events listener");

        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTableActionAlterations.preDelete(modelType)
                .flatMap(new Func1<ModelType, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(ModelType modelType) {
                        return mTable.delete(modelType);
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean success) {
                        try {
                            mTableActionAlterations.postDelete(success, modelType);
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean success) {
                        if (success) {
                            tableEventsListener.onDeleteSuccess(modelType);
                        } else {
                            tableEventsListener.onDeleteFailure(modelType, null);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        tableEventsListener.onDeleteFailure(modelType, throwable);
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
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
