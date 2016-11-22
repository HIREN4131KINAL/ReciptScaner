package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.StubTableActionAlterations;
import co.smartreceipts.android.persistence.database.controllers.alterations.TableActionAlterations;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.AbstractColumnTable;
import co.smartreceipts.android.utils.ListUtils;
import co.smartreceipts.android.utils.Logger;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ColumnTableController extends AbstractTableController<Column<Receipt>> {

    private final AbstractColumnTable mAbstractColumnTable;
    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;
    private final Scheduler mPreprocessingObserveOnScheduler;

    public ColumnTableController(@NonNull AbstractColumnTable table, @NonNull Analytics analytics, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        this(table, new StubTableActionAlterations<Column<Receipt>>(), analytics, receiptColumnDefinitions);
    }

    public ColumnTableController(@NonNull AbstractColumnTable table, @NonNull TableActionAlterations<Column<Receipt>> tableActionAlterations,
                                 @NonNull Analytics analytics, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        this(table, tableActionAlterations, receiptColumnDefinitions, analytics, Schedulers.io(), AndroidSchedulers.mainThread(), Schedulers.io());
    }

    ColumnTableController(@NonNull AbstractColumnTable table, @NonNull TableActionAlterations<Column<Receipt>> tableActionAlterations, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions,
                          @NonNull Analytics analytics, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler, @NonNull Scheduler preprocessingObserveOnScheduler) {
        super(table, tableActionAlterations, analytics, subscribeOnScheduler, observeOnScheduler);
        mAbstractColumnTable = Preconditions.checkNotNull(table);
        mReceiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
        mPreprocessingObserveOnScheduler = Preconditions.checkNotNull(preprocessingObserveOnScheduler);
    }

    /**
     * Inserts the default column as defined by {@link ColumnDefinitions#getDefaultInsertColumn()}
     */
    public synchronized void insertDefaultColumn() {
        insert(mReceiptColumnDefinitions.getDefaultInsertColumn(), new DatabaseOperationMetadata());
    }

    /**
     * Attempts to delete the last column in the list
     */
    public synchronized void deleteLast(final @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        // TODO: Reduce code overflow and just chain directly with #delete observables via composition

        Logger.info(this, "#deleteLast:");
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        final Subscription subscription = mAbstractColumnTable.get().map(new Func1<List<Column<Receipt>>, Column<Receipt>>() {
            @Override
            public Column<Receipt> call(List<Column<Receipt>> columns) {
                return removeLastColumnIfPresent(columns);
            }
        })
        .observeOn(mSubscribeOnScheduler)
        .subscribeOn(mPreprocessingObserveOnScheduler)
        .subscribe(new Action1<Column<Receipt>>() {
            @Override
            public void call(Column<Receipt> column) {
                if (column != null) {
                    for (final TableEventsListener<Column<Receipt>> tableEventsListener : mTableEventsListeners) {
                        tableEventsListener.onDeleteSuccess(column, databaseOperationMetadata);
                    }
                } else {
                    for (final TableEventsListener<Column<Receipt>> tableEventsListener : mTableEventsListeners) {
                        // TODO: Link this stuff better to fix improper null here :/
                        tableEventsListener.onDeleteFailure(column, null, databaseOperationMetadata);
                    }
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mAnalytics.record(new ErrorEvent(ColumnTableController.this, throwable));
                Logger.debug(this, "#onDeleteLastFailure - onError");
                unsubscribeReference(subscriptionRef);
            }
        }, new Action0() {
            @Override
            public void call() {
                Logger.debug(this, "#deleteLast - onComplete");
                unsubscribeReference(subscriptionRef);
            }
        });
        subscriptionRef.set(subscription);
        mCompositeSubscription.add(subscription);
    }

    @Nullable
    private Column<Receipt> removeLastColumnIfPresent(@NonNull List<Column<Receipt>> columns) {
        final Column<Receipt> lastColumn = ListUtils.removeLast(columns);
        if (lastColumn != null) {
            delete(lastColumn, new DatabaseOperationMetadata());
            return lastColumn;
        } else {
            return null;
        }
    }
}
