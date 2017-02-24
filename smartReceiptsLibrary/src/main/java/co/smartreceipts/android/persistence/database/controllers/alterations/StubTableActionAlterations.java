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

    @NonNull
    @Override
    public Observable<List<T>> postGet(@NonNull List<T> list) {
        return Observable.just(list);
    }

    @NonNull
    @Override
    public Observable<T> preInsert(@NonNull T t) {
        return Observable.just(t);
    }

    @NonNull
    @Override
    public Observable<T> postInsert(@Nullable T t) {
        if (t != null) {
            return Observable.just(t);
        } else {
            return Observable.error(new Exception("Post insert failed due to a null value"));
        }
    }

    @NonNull
    @Override
    public Observable<T> preUpdate(@NonNull T oldT, @NonNull T newT) {
        return Observable.just(newT);
    }

    @NonNull
    @Override
    public Observable<T> postUpdate(@NonNull T oldT, @Nullable T newT) {
        if (newT != null) {
            return Observable.just(newT);
        } else {
            return Observable.error(new Exception("Post update failed due to a null value"));
        }
    }

    @NonNull
    @Override
    public Observable<T> preDelete(@NonNull T t) {
        return Observable.just(t);
    }

    @NonNull
    @Override
    public Observable<T> postDelete(@Nullable T t) {
        if (t != null) {
            return Observable.just(t);
        } else {
            return Observable.error(new Exception("Post delete failed due to a null value"));
        }
    }
}
