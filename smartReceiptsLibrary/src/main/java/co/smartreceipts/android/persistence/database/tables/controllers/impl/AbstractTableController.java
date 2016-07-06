package co.smartreceipts.android.persistence.database.tables.controllers.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.tables.controllers.TableController;
import co.smartreceipts.android.persistence.database.tables.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.tables.controllers.alterations.TableActionAlterations;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Provides a top-level implementation of the {@link TableController} contract
 *
 * @param <ModelType> the model object type that this will be used to create
 */
abstract class AbstractTableController<ModelType> implements TableController<ModelType> {

    private final Table<ModelType, ?> mTable;
    private final TableActionAlterations<ModelType> mTableActionAlterations;
    private final Scheduler mSubscribeOnScheduler;
    private final Scheduler mObserveOnScheduler;
    private final CopyOnWriteArrayList<TableEventsListener<ModelType>> mTableEventsListeners;

    public AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations) {
        this(table, tableActionAlterations, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    AbstractTableController(@NonNull Table<ModelType, ?> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations,
                            @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        mTable = Preconditions.checkNotNull(table);
        mTableActionAlterations = Preconditions.checkNotNull(tableActionAlterations);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mTableEventsListeners = new CopyOnWriteArrayList<>();
    }



    @Override
    public void registerListener(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        mTableEventsListeners.add(tableEventsListener);
    }

    @Override
    public void unregisterListener(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        mTableEventsListeners.remove(tableEventsListener);
    }

    @Override
    @NonNull
    public Subscription get() {
        return mTableActionAlterations.preGet()
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
                            for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onGetSuccess(modelTypes);
                            }
                        }
                        else {
                            for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onGetFailure(null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                            tableEventsListener.onGetFailure(throwable);
                        }
                    }
                });
    }

    @Override
    @NonNull
    public Subscription insert(@NonNull final ModelType insertModelType) {
        return mTableActionAlterations.preInsert(insertModelType)
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
                            for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onInsertSuccess(modelType);
                            }
                            // TODO: Call get() again
                        } else {
                            for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onInsertFailure(insertModelType, null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                            tableEventsListener.onInsertFailure(insertModelType, throwable);
                        }
                    }
                });
    }

    @Override
    @NonNull
    public Subscription update(@NonNull final ModelType oldModelType, @NonNull ModelType newModelType) {
        return mTableActionAlterations.preUpdate(oldModelType, newModelType)
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
                            for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onUpdateSuccess(oldModelType, modelType);
                            }
                            // TODO: Call get() again
                        } else {
                            for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onUpdateFailure(oldModelType, null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                            tableEventsListener.onUpdateFailure(oldModelType, throwable);
                        }
                    }
                });
    }

    @Override
    @NonNull
    public Subscription delete(@NonNull final ModelType modelType) {
        return mTableActionAlterations.preDelete(modelType)
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
                            for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onDeleteSuccess(modelType);
                            }
                            // TODO: Call get() again
                        } else {
                            for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                                tableEventsListener.onDeleteFailure(modelType, null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        for (TableEventsListener<ModelType> tableEventsListener : mTableEventsListeners) {
                            tableEventsListener.onDeleteFailure(modelType, throwable);
                        }
                    }
                });
    }

}
