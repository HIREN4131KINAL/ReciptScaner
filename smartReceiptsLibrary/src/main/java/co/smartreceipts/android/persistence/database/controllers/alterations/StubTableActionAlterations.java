package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import rx.Observable;

/**
 * A stub implementation of the {@link TableActionAlterations} contract that allows us to selectively override methods (if needed)
 *
 * @param <T> the type of object we will be interacting with
 */
public class StubTableActionAlterations<T> implements TableActionAlterations<T> {

    @NonNull
    @Override
    public Observable<Void> preGet() {
        return Observable.just(null);
    }

    @Override
    public void postGet(@NonNull List<T> trips) throws Exception {

    }

    @NonNull
    @Override
    public Observable<T> preInsert(@NonNull T t) {
        return Observable.just(t);
    }

    @Override
    public void postInsert(@Nullable T t) throws Exception {

    }

    @NonNull
    @Override
    public Observable<T> preUpdate(@NonNull T oldT, @NonNull T newT) {
        return Observable.just(newT);
    }

    @Override
    public void postUpdate(@NonNull T oldT, @Nullable T newT) throws Exception {

    }

    @NonNull
    @Override
    public Observable<T> preDelete(@NonNull T t) {
        return Observable.just(t);
    }

    @Override
    public void postDelete(@Nullable T t) throws Exception {

    }
}
