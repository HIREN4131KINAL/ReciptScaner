package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.StubTableActionAlterations;
import co.smartreceipts.android.persistence.database.controllers.alterations.TableActionAlterations;
import co.smartreceipts.android.persistence.database.controllers.results.DeleteResult;
import co.smartreceipts.android.persistence.database.controllers.results.GetResult;
import co.smartreceipts.android.persistence.database.controllers.results.InsertResult;
import co.smartreceipts.android.persistence.database.controllers.results.UpdateResult;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.utils.PreFixedThreadFactory;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;

/**
 * Provides a top-level implementation of the {@link TableController} contract
 *
 * @param <ModelType> the model object type that this will be used to create
 */
abstract class AbstractTableController<ModelType> implements TableController<ModelType> {

    protected final String TAG = getClass().getSimpleName();

    private final Table<ModelType, ?> mTable;
    private final ConcurrentHashMap<TableEventsListener<ModelType>, BridgingTableEventsListener<ModelType>> mBridgingTableEventsListeners = new ConcurrentHashMap<>();
    protected final CopyOnWriteArrayList<TableEventsListener<ModelType>> mTableEventsListeners = new CopyOnWriteArrayList<>();
    protected final TableActionAlterations<ModelType> mTableActionAlterations;
    protected final Analytics mAnalytics;
    protected final Scheduler mSubscribeOnScheduler;
    protected final Scheduler mObserveOnScheduler;

    private final Subject<GetResult<ModelType>, GetResult<ModelType>> getStreamSubject = new SerializedSubject<>(PublishSubject.<GetResult<ModelType>>create());
    private final Subject<InsertResult<ModelType>, InsertResult<ModelType>> insertStreamSubject = new SerializedSubject<>(PublishSubject.<InsertResult<ModelType>>create());
    private final Subject<UpdateResult<ModelType>, UpdateResult<ModelType>> updateStreamSubject = new SerializedSubject<>(PublishSubject.<UpdateResult<ModelType>>create());
    private final Subject<DeleteResult<ModelType>, DeleteResult<ModelType>> deleteStreamSubject = new SerializedSubject<>(PublishSubject.<DeleteResult<ModelType>>create());

    protected CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull Analytics analytics) {
        this(table, new StubTableActionAlterations<ModelType>(), analytics);
    }

    public AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations, @NonNull Analytics analytics) {
        mTable = Preconditions.checkNotNull(table);
        mTableActionAlterations = Preconditions.checkNotNull(tableActionAlterations);
        mAnalytics = Preconditions.checkNotNull(analytics);
        mSubscribeOnScheduler = Schedulers.from(Executors.newSingleThreadExecutor(new PreFixedThreadFactory(getClass().getSimpleName())));
        mObserveOnScheduler = AndroidSchedulers.mainThread();
    }

    AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations,
                            @NonNull Analytics analytics, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        mTable = Preconditions.checkNotNull(table);
        mTableActionAlterations = Preconditions.checkNotNull(tableActionAlterations);
        mAnalytics = Preconditions.checkNotNull(analytics);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
    }

    @Override
    public synchronized void subscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        final BridgingTableEventsListener<ModelType> bridge = new BridgingTableEventsListener<>(this, tableEventsListener, mObserveOnScheduler);
        mBridgingTableEventsListeners.put(tableEventsListener, bridge);
        mTableEventsListeners.add(tableEventsListener);
        bridge.subscribe();
    }

    @Override
    public synchronized void unsubscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        mTableEventsListeners.remove(tableEventsListener);
        final BridgingTableEventsListener<ModelType> bridge = mBridgingTableEventsListeners.remove(tableEventsListener);
        if (bridge != null) {
            bridge.unsubscribe();
        }
    }

    @NonNull
    @Override
    public Observable<List<ModelType>> get() {
        Logger.info(this, "#get");
        final Subject<List<ModelType>, List<ModelType>> getSubject = PublishSubject.create();
        mTableActionAlterations.preGet()
                .subscribeOn(mSubscribeOnScheduler)
                .flatMap(new Func1<Void, Observable<List<ModelType>>>() {
                    @Override
                    public Observable<List<ModelType>> call(Void oVoid) {
                        return mTable.get();
                    }
                })
                .flatMap(new Func1<List<ModelType>, Observable<List<ModelType>>>() {
                    @Override
                    public Observable<List<ModelType>> call(List<ModelType> list) {
                        return mTableActionAlterations.postGet(list);
                    }
                })
                .doOnNext(new Action1<List<ModelType>>() {
                    @Override
                    public void call(List<ModelType> modelTypes) {
                        Logger.debug(AbstractTableController.this, "#onGetSuccess - onNext");
                        getStreamSubject.onNext(new GetResult<>(modelTypes));
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(AbstractTableController.this, "#onGetFailure - onError", throwable);
                        mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                        getStreamSubject.onNext(new GetResult<ModelType>(throwable));
                    }
                })
                .subscribe(getSubject);
        return getSubject.asObservable();
    }

    @NonNull
    @Override
    public Observable<GetResult<ModelType>> getStream() {
        return getStreamSubject.asObservable();
    }

    @NonNull
    @Override
    public Observable<ModelType> insert(@NonNull final ModelType modelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        Logger.info(this, "#insert: {}", modelType);
        final Subject<ModelType, ModelType> insertSubject = PublishSubject.create();
        mTableActionAlterations.preInsert(modelType)
                .subscribeOn(mSubscribeOnScheduler)
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType insertedItem) {
                        return mTable.insert(insertedItem, databaseOperationMetadata);
                    }
                })
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType insertedItem) {
                        return mTableActionAlterations.postInsert(insertedItem);
                    }
                })
                .doOnNext(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType insertedItem) {
                        Logger.debug(AbstractTableController.this, "#onInsertSuccess - onNext");
                        insertStreamSubject.onNext(new InsertResult<>(insertedItem, databaseOperationMetadata));
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(AbstractTableController.this, "#onInsertFailure - onError", throwable);
                        mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                        insertStreamSubject.onNext(new InsertResult<>(modelType, throwable, databaseOperationMetadata));
                    }
                })
                .subscribe(insertSubject);
        return insertSubject.asObservable();
    }

    @NonNull
    @Override
    public Observable<InsertResult<ModelType>> insertStream() {
        return insertStreamSubject.asObservable();
    }

    @NonNull
    @Override
    public Observable<ModelType> update(@NonNull final ModelType oldModelType, @NonNull ModelType newModelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        Logger.info(this, "#update: {}; {}", oldModelType, newModelType);
        final Subject<ModelType, ModelType> updateSubject = PublishSubject.create();
        mTableActionAlterations.preUpdate(oldModelType, newModelType)
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType updatedItem) {
                        return mTable.update(oldModelType, updatedItem, databaseOperationMetadata);
                    }
                })
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType updatedItem) {
                        return mTableActionAlterations.postUpdate(oldModelType, updatedItem);
                    }
                })
                .doOnNext(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType updatedItem) {
                        Logger.debug(AbstractTableController.this, "#onUpdateSuccess - onNext");
                        updateStreamSubject.onNext(new UpdateResult<>(oldModelType, updatedItem, databaseOperationMetadata));
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(AbstractTableController.this, "#onUpdateFailure - onError", throwable);
                        mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                        updateStreamSubject.onNext(new UpdateResult<>(oldModelType, null, throwable, databaseOperationMetadata));
                    }
                })
                .subscribe(updateSubject);
        return updateSubject.asObservable();
    }

    @NonNull
    @Override
    public Observable<UpdateResult<ModelType>> updateStream() {
        return updateStreamSubject.asObservable();
    }

    @NonNull
    @Override
    public synchronized Observable<ModelType> delete(@NonNull final ModelType modelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        Logger.info(this, "#delete: {}", modelType);
        final Subject<ModelType, ModelType> deleteSubject = PublishSubject.create();
        mTableActionAlterations.preDelete(modelType)
                .subscribeOn(mSubscribeOnScheduler)
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType deletedItem) {
                        return mTable.delete(deletedItem, databaseOperationMetadata);
                    }
                })
                .flatMap(new Func1<ModelType, Observable<ModelType>>() {
                    @Override
                    public Observable<ModelType> call(ModelType deletedItem) {
                        return mTableActionAlterations.postDelete(deletedItem);
                    }
                })
                .doOnNext(new Action1<ModelType>() {
                    @Override
                    public void call(ModelType deletedItem) {
                        Logger.debug(AbstractTableController.this, "#onDeleteSuccess - onNext");
                        deleteStreamSubject.onNext(new DeleteResult<>(deletedItem, databaseOperationMetadata));
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(AbstractTableController.this, "#onDeleteFailure - onError", throwable);
                        mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                        deleteStreamSubject.onNext(new DeleteResult<>(modelType, throwable, databaseOperationMetadata));
                    }
                })
                .subscribe(deleteSubject);
        return deleteSubject.asObservable();
    }

    @NonNull
    @Override
    public Observable<DeleteResult<ModelType>> deleteStream() {
        return deleteStreamSubject.asObservable();
    }

    protected void unsubscribeReference(@NonNull AtomicReference<Subscription> subscriptionReference) {
        final Subscription subscription = subscriptionReference.get();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}
