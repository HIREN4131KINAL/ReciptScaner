package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.ReceiptTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.ReceiptTableActionAlterations;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
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

    public ReceiptTableController(@NonNull PersistenceManager persistenceManager, @NonNull TripTableController tripTableController) {
        this(Preconditions.checkNotNull(persistenceManager.getDatabase().getReceiptsTable()), Preconditions.checkNotNull(persistenceManager.getStorageManager()), tripTableController);
    }

    public ReceiptTableController(@NonNull ReceiptsTable receiptsTable, @NonNull StorageManager storageManager, @NonNull TripTableController tripTableController) {
        this(receiptsTable, new ReceiptTableActionAlterations(receiptsTable, storageManager), tripTableController);
    }

    public ReceiptTableController(@NonNull ReceiptsTable receiptsTable, @NonNull ReceiptTableActionAlterations receiptTableActionAlterations, @NonNull TripTableController tripTableController) {
        super(receiptsTable, receiptTableActionAlterations);
        mReceiptTableActionAlterations = Preconditions.checkNotNull(receiptTableActionAlterations);
        subscribe(new ReceiptRefreshTripPricesListener(Preconditions.checkNotNull(tripTableController)));
    }

    ReceiptTableController(@NonNull ReceiptsTable receiptsTable, @NonNull ReceiptTableActionAlterations receiptTableActionAlterations, @NonNull TripTableController tripTableController,
                           @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        super(receiptsTable, receiptTableActionAlterations, subscribeOnScheduler, observeOnScheduler);
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
        Log.i(TAG, "#move: " + receiptToMove + "; " + toTrip);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mReceiptTableActionAlterations.preMove(receiptToMove, toTrip)
                .flatMap(new Func1<Receipt, Observable<Receipt>>() {
                    @Override
                    public Observable<Receipt> call(Receipt receipt) {
                        return mTripForeignKeyTable.insert(receipt);
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
                            Log.d(TAG, "#onMoveSuccess - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onMoveSuccess(receiptToMove, newReceipt);
                            }
                        } else {
                            Log.d(TAG, "#onMoveFailure - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onMoveFailure(receiptToMove, null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "#onMoveFailure - onError");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onMoveFailure(receiptToMove, throwable);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "#move - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    public synchronized void copy(@NonNull final Receipt receiptToCopy, @NonNull Trip toTrip) {
        Log.i(TAG, "#move: " + receiptToCopy + "; " + toTrip);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mReceiptTableActionAlterations.preCopy(receiptToCopy, toTrip)
                .flatMap(new Func1<Receipt, Observable<Receipt>>() {
                    @Override
                    public Observable<Receipt> call(Receipt receipt) {
                        return mTripForeignKeyTable.insert(receipt);
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
                            Log.d(TAG, "#onCopySuccess - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onCopySuccess(receiptToCopy, newReceipt);
                            }
                        } else {
                            Log.d(TAG, "#onCopyFailure - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onCopyFailure(receiptToCopy, null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "#onCopyFailure - onError");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onCopyFailure(receiptToCopy, throwable);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "#move - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    public synchronized void swapUp(@NonNull final Receipt receiptToSwapUp) {
        Log.i(TAG, "#swapUp: " + receiptToSwapUp);
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
                            success &= mTripForeignKeyTable.update(entry.getKey(), entry.getValue()).toBlocking().first() != null;
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
                            Log.d(TAG, "#onSwapUpSuccess - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onSwapSuccess();
                            }
                        } else {
                            Log.d(TAG, "#onSwapUpFailure - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onSwapFailure(null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "#onSwapUpFailure - onError");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onSwapFailure(throwable);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "#swapUp - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    public synchronized void swapDown(@NonNull final Receipt receiptToSwapDown) {
        Log.i(TAG, "#swapDown: " + receiptToSwapDown);
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
                            success &= mTripForeignKeyTable.update(entry.getKey(), entry.getValue()).toBlocking().first() != null;
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
                            Log.d(TAG, "#onSwapDownSuccess - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onSwapSuccess();
                            }
                        } else {
                            Log.d(TAG, "#onSwapDownFailure - onNext");
                            for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                                tableEventsListener.onSwapFailure(null);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "#onSwapDownFailure - onError");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onSwapFailure(throwable);
                        }
                        unsubscribeReference(subscriptionRef);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "#swapDown - onComplete");
                        unsubscribeReference(subscriptionRef);
                    }
                });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }
}
