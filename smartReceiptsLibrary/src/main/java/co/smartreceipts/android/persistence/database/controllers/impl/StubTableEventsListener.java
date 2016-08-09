package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;

public class StubTableEventsListener<T> implements TableEventsListener<T> {

    @Override
    public void onGetSuccess(@NonNull List<T> list) {

    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {

    }

    @Override
    public void onInsertSuccess(@NonNull T t) {

    }

    @Override
    public void onInsertFailure(@NonNull T t, @Nullable Throwable e) {

    }

    @Override
    public void onUpdateSuccess(@NonNull T oldT, @NonNull T newT) {

    }

    @Override
    public void onUpdateFailure(@NonNull T oldT, @Nullable Throwable e) {

    }

    @Override
    public void onDeleteSuccess(@NonNull T t) {

    }

    @Override
    public void onDeleteFailure(@NonNull T t, @Nullable Throwable e) {

    }
}
