package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.results.DeleteResult;
import co.smartreceipts.android.persistence.database.controllers.results.GetResult;
import co.smartreceipts.android.persistence.database.controllers.results.InsertResult;
import co.smartreceipts.android.persistence.database.controllers.results.UpdateResult;
import rx.Scheduler;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * A temporary class to bridge our refactoring work and avoid breaking changes while we get this all in place
 */
@Deprecated
public class BridgingTableEventsListener<ModelType> {

    private final TableController<ModelType> tableController;
    private final TableEventsListener<ModelType> listener;
    private final Scheduler observeOnScheduler;
    private CompositeSubscription compositeSubscription;

    public BridgingTableEventsListener(@NonNull TableController<ModelType> tableController, @NonNull TableEventsListener<ModelType> listener,
                                       @NonNull Scheduler observeOnScheduler) {
        this.tableController = Preconditions.checkNotNull(tableController);
        this.listener = Preconditions.checkNotNull(listener);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
    }

    public final void subscribe() {
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(this.tableController.getStream()
                .subscribeOn(observeOnScheduler)
                .subscribe(new Action1<GetResult<ModelType>>() {
                    @Override
                    public void call(GetResult<ModelType> getResult) {
                        if (getResult.getThrowable() == null) {
                            listener.onGetSuccess(getResult.get());
                        } else {
                            listener.onGetFailure(getResult.getThrowable());
                        }
                    }
                }));
        compositeSubscription.add(this.tableController.insertStream()
                .subscribeOn(observeOnScheduler)
                .subscribe(new Action1<InsertResult<ModelType>>() {
                    @Override
                    public void call(InsertResult<ModelType> insertResult) {
                        if (insertResult.getThrowable() == null) {
                            listener.onInsertSuccess(insertResult.get(), insertResult.getDatabaseOperationMetadata());
                        } else {
                            listener.onInsertFailure(insertResult.get(), insertResult.getThrowable(), insertResult.getDatabaseOperationMetadata());
                        }
                    }
                }));
        compositeSubscription.add(this.tableController.updateStream()
                .subscribeOn(observeOnScheduler)
                .subscribe(new Action1<UpdateResult<ModelType>>() {
                    @Override
                    public void call(UpdateResult<ModelType> updateResult) {
                        if (updateResult.getThrowable() == null) {
                            listener.onUpdateSuccess(updateResult.getOld(), updateResult.getNew(), updateResult.getDatabaseOperationMetadata());
                        } else {
                            listener.onUpdateFailure(updateResult.getOld(), updateResult.getThrowable(), updateResult.getDatabaseOperationMetadata());
                        }
                    }
                }));
        compositeSubscription.add(this.tableController.deleteStream()
                .subscribeOn(observeOnScheduler)
                .subscribe(new Action1<DeleteResult<ModelType>>() {
                    @Override
                    public void call(DeleteResult<ModelType> deleteResult) {
                        if (deleteResult.getThrowable() == null) {
                            listener.onDeleteSuccess(deleteResult.get(), deleteResult.getDatabaseOperationMetadata());
                        } else {
                            listener.onDeleteFailure(deleteResult.get(), deleteResult.getThrowable(), deleteResult.getDatabaseOperationMetadata());
                        }
                    }
                }));
    }

    public void unsubscribe() {
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }
        compositeSubscription = null;
    }
}
