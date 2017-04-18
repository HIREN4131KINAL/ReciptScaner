package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;


/**
 * A temporary class to bridge our refactoring work and avoid breaking changes while we get this all in place
 */
@Deprecated
public class BridgingTableEventsListener<ModelType> {

    private final TableController<ModelType> tableController;
    private final TableEventsListener<ModelType> listener;
    private final Scheduler observeOnScheduler;
    private CompositeDisposable compositeDisposable;

    public BridgingTableEventsListener(@NonNull TableController<ModelType> tableController, @NonNull TableEventsListener<ModelType> listener,
                                       @NonNull Scheduler observeOnScheduler) {
        this.tableController = Preconditions.checkNotNull(tableController);
        this.listener = Preconditions.checkNotNull(listener);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
    }

    public final void subscribe() {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(this.tableController.getStream()
                .observeOn(observeOnScheduler)
                .subscribe(modelTypeGetResult -> {
                    if (modelTypeGetResult.getThrowable() == null) {
                        listener.onGetSuccess(modelTypeGetResult.get());
                    } else {
                        listener.onGetFailure(modelTypeGetResult.getThrowable());
                    }
                }));

        compositeDisposable.add(this.tableController.insertStream()
                .observeOn(observeOnScheduler)
                .subscribe(modelTypeInsertResult -> {
                    if (modelTypeInsertResult.getThrowable() == null) {
                        listener.onInsertSuccess(modelTypeInsertResult.get(), modelTypeInsertResult.getDatabaseOperationMetadata());
                    } else {
                        listener.onInsertFailure(modelTypeInsertResult.get(), modelTypeInsertResult.getThrowable(), modelTypeInsertResult.getDatabaseOperationMetadata());
                    }
                }));

        compositeDisposable.add(this.tableController.updateStream()
                .observeOn(observeOnScheduler)
                .subscribe(modelTypeUpdateResult -> {
                    if (modelTypeUpdateResult.getThrowable() == null) {
                        listener.onUpdateSuccess(modelTypeUpdateResult.getOld(), modelTypeUpdateResult.getNew(), modelTypeUpdateResult.getDatabaseOperationMetadata());
                    } else {
                        listener.onUpdateFailure(modelTypeUpdateResult.getOld(), modelTypeUpdateResult.getThrowable(), modelTypeUpdateResult.getDatabaseOperationMetadata());
                    }
                }));

        compositeDisposable.add(this.tableController.deleteStream()
                .observeOn(observeOnScheduler)
                .subscribe(modelTypeDeleteResult -> {
                    if (modelTypeDeleteResult.getThrowable() == null) {
                        listener.onDeleteSuccess(modelTypeDeleteResult.get(), modelTypeDeleteResult.getDatabaseOperationMetadata());
                    } else {
                        listener.onDeleteFailure(modelTypeDeleteResult.get(), modelTypeDeleteResult.getThrowable(), modelTypeDeleteResult.getDatabaseOperationMetadata());
                    }
                }));
    }

    public void unsubscribe() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        compositeDisposable = null;
    }
}
