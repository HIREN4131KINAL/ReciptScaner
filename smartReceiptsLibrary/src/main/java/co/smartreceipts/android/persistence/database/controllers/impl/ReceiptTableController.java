package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.ReceiptTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.ReceiptTableActionAlterations;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.utils.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import wb.android.storage.StorageManager;

public class ReceiptTableController extends TripForeignKeyAbstractTableController<Receipt> {

    private final ReceiptTableActionAlterations mReceiptTableActionAlterations;
    private final CopyOnWriteArrayList<ReceiptTableEventsListener> mReceiptTableEventsListeners = new CopyOnWriteArrayList<>();

    public ReceiptTableController(@NonNull PersistenceManager persistenceManager, @NonNull Analytics analytics, @NonNull TripTableController tripTableController) {
        this(Preconditions.checkNotNull(persistenceManager.getDatabase().getReceiptsTable()), Preconditions.checkNotNull(persistenceManager.getStorageManager()), analytics, tripTableController);
    }

    public ReceiptTableController(@NonNull ReceiptsTable receiptsTable, @NonNull StorageManager storageManager, @NonNull Analytics analytics, @NonNull TripTableController tripTableController) {
        this(receiptsTable, new ReceiptTableActionAlterations(receiptsTable, storageManager), analytics, tripTableController);
    }

    public ReceiptTableController(@NonNull ReceiptsTable receiptsTable, @NonNull ReceiptTableActionAlterations receiptTableActionAlterations, @NonNull Analytics analytics, @NonNull TripTableController tripTableController) {
        super(receiptsTable, receiptTableActionAlterations, analytics);
        mReceiptTableActionAlterations = Preconditions.checkNotNull(receiptTableActionAlterations);
        subscribe(new ReceiptRefreshTripPricesListener(Preconditions.checkNotNull(tripTableController)));
    }

    ReceiptTableController(@NonNull ReceiptsTable receiptsTable, @NonNull ReceiptTableActionAlterations receiptTableActionAlterations, @NonNull Analytics analytics,
                           @NonNull TripTableController tripTableController, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        super(receiptsTable, receiptTableActionAlterations, analytics, subscribeOnScheduler, observeOnScheduler);
        mReceiptTableActionAlterations = Preconditions.checkNotNull(receiptTableActionAlterations);
        subscribe(new ReceiptRefreshTripPricesListener(Preconditions.checkNotNull(tripTableController)));
    }

    @Override
    public synchronized void subscribe(@NonNull TableEventsListener<Receipt> tableEventsListener) {
        super.subscribe(tableEventsListener);
        if (tableEventsListener instanceof ReceiptTableEventsListener) {
            mReceiptTableEventsListeners.add((ReceiptTableEventsListener) tableEventsListener);
        }
    }

    @Override
    public synchronized void unsubscribe(@NonNull TableEventsListener<Receipt> tableEventsListener) {
        if (tableEventsListener instanceof ReceiptTableEventsListener) {
            mReceiptTableEventsListeners.remove(tableEventsListener);
        }
        super.unsubscribe(tableEventsListener);
    }

    public synchronized void move(@NonNull final Receipt receiptToMove, @NonNull Trip toTrip) {
        Logger.info(this, "#move: {}; {}", receiptToMove, toTrip);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mReceiptTableActionAlterations.preMove(receiptToMove, toTrip)
                .flatMap(new Func1<Receipt, Observable<Receipt>>() {
                    @Override
                    public Observable<Receipt> call(Receipt receipt) {
                        return mTripForeignKeyTable.insert(receipt, new DatabaseOperationMetadata());
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .doOnNext(new Action1<Receipt>() {
                    @Override
                    public void call(Receipt newReceipt) {
                        try {
                            mReceiptTableActionAlterations.postMove(receiptToMove, newReceipt);
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .subscribe(new Action1<Receipt>() {
                    @Override
                    public void call(Receipt newReceipt) {
                        if (newReceipt != null) {
                            Logger.debug(this, "#onMoveSuccess - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onMoveSuccess(receiptToMove, newReceipt);
                            }
                        } else {
                            Logger.debug(this, "#onMoveFailure - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onMoveFailure(receiptToMove, null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(ReceiptTableController.this, throwable));
                        Logger.debug(this, "#onMoveFailure - onError");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onMoveFailure(receiptToMove, throwable);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(this, "#move - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    public synchronized void copy(@NonNull final Receipt receiptToCopy, @NonNull Trip toTrip) {
        Logger.info(this, "#move: {}; {}", receiptToCopy, toTrip);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mReceiptTableActionAlterations.preCopy(receiptToCopy, toTrip)
                .flatMap(new Func1<Receipt, Observable<Receipt>>() {
                    @Override
                    public Observable<Receipt> call(Receipt receipt) {
                        return mTripForeignKeyTable.insert(receipt, new DatabaseOperationMetadata());
                    }
                })
                .doOnNext(new Action1<Receipt>() {
                    @Override
                    public void call(Receipt newReceipt) {
                        try {
                            mReceiptTableActionAlterations.postCopy(receiptToCopy, newReceipt);
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(new Action1<Receipt>() {
                    @Override
                    public void call(Receipt newReceipt) {
                        if (newReceipt != null) {
                            Logger.debug(this, "#onCopySuccess - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onCopySuccess(receiptToCopy, newReceipt);
                            }
                        } else {
                            Logger.debug(this, "#onCopyFailure - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onCopyFailure(receiptToCopy, null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(ReceiptTableController.this, throwable));
                        Logger.debug(this, "#onCopyFailure - onError");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onCopyFailure(receiptToCopy, throwable);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(this, "#move - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    public synchronized void swapUp(@NonNull final Receipt receiptToSwapUp) {
        Logger.info(TAG, "#swapUp: {}", receiptToSwapUp);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTripForeignKeyTable.get(receiptToSwapUp.getTrip())
                .flatMap(new Func1<List<Receipt>, Observable<List<? extends Map.Entry<Receipt, Receipt>>>>() {
                    @Override
                    public Observable<List<? extends Map.Entry<Receipt, Receipt>>> call(List<Receipt> receipts) {
                        return mReceiptTableActionAlterations.getReceiptsToSwapUp(receiptToSwapUp, receipts);
                    }
                })
                .flatMap(new Func1<List<? extends Map.Entry<Receipt, Receipt>>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(List<? extends Map.Entry<Receipt, Receipt>> entries) {
                        boolean success = true;
                        for (final Map.Entry<Receipt, Receipt> entry : entries) {
                            success &= mTripForeignKeyTable.update(entry.getKey(), entry.getValue(), new DatabaseOperationMetadata()).toBlocking().first() != null;
                        }
                        return Observable.just(success);
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean success) {
                        if (success) {
                            Logger.debug(this, "#onSwapUpSuccess - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onSwapSuccess();
                            }
                        } else {
                            Logger.debug(this, "#onSwapUpFailure - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onSwapFailure(null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(ReceiptTableController.this, throwable));
                        Logger.debug(this, "#onSwapUpFailure - onError");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onSwapFailure(throwable);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(this, "#swapUp - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    public synchronized void swapDown(@NonNull final Receipt receiptToSwapDown) {
        Logger.info(this, "#swapDown: {}", receiptToSwapDown);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mTripForeignKeyTable.get(receiptToSwapDown.getTrip())
                .flatMap(new Func1<List<Receipt>, Observable<List<? extends Map.Entry<Receipt, Receipt>>>>() {
                    @Override
                    public Observable<List<? extends Map.Entry<Receipt, Receipt>>> call(List<Receipt> receipts) {
                        return mReceiptTableActionAlterations.getReceiptsToSwapDown(receiptToSwapDown, receipts);
                    }
                })
                .flatMap(new Func1<List<? extends Map.Entry<Receipt, Receipt>>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(List<? extends Map.Entry<Receipt, Receipt>> entries) {
                        boolean success = true;
                        for (final Map.Entry<Receipt, Receipt> entry : entries) {
                            success &= mTripForeignKeyTable.update(entry.getKey(), entry.getValue(), new DatabaseOperationMetadata()).toBlocking().first() != null;
                        }
                        return Observable.just(success);
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean success) {
                        if (success) {
                            Logger.debug(this, "#onSwapDownSuccess - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onSwapSuccess();
                            }
                        } else {
                            Logger.debug(this, "#onSwapDownFailure - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onSwapFailure(null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(ReceiptTableController.this, throwable));
                        Logger.debug(this, "#onSwapDownFailure - onError");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onSwapFailure(throwable);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(this, "#swapDown - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }
}
