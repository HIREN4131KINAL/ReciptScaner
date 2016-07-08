package co.smartreceipts.android.persistence.database.tables.controllers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.model.Trip;

public interface TripForeignKeyTableEventsListener<T> extends TableEventsListener<T> {

    void onGetSuccess(@NonNull List<T> list, @NonNull Trip trip);

    void onGetFailure(@Nullable Throwable e, @NonNull Trip trip);
}
