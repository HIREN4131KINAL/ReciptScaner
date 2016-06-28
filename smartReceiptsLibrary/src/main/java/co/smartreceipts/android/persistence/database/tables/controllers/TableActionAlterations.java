package co.smartreceipts.android.persistence.database.tables.controllers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.persistence.database.tables.Table;
import rx.Observable;

public interface TableActionAlterations<T> {

    /**
     * @return an {@link Observable} that should be performed before we call {@link Table#get()}
     */
    @NonNull
    Observable<Void> preGet();

    /**
     * Performs an action following a call to {@link Table#get()}
     *
     * @param tList the list retrieved via the {@link Table#get()} request
     */
    void postGet(@NonNull List<T> tList) throws Exception;

    /**
     * @return an {@link Observable} that should be performed before we call {@link Table#insert(Object)}}
     */
    @NonNull
    Observable<T> preInsert(@NonNull T t);

    /**
     * Performs an action following a call to {@link Table#insert(Object)}
     *
     * @param t the item that was inserted in {@link Table#insert(Object)} or {@code null} if the insert failed
     */
    void postInsert(@Nullable T t)  throws Exception;

    /**
     * @return an {@link Observable} that should be performed before we call {@link Table#update(Object, Object)}
     */
    @NonNull
    Observable<T> preUpdate(@NonNull T oldT, @NonNull T newT);

    void postUpdate(@NonNull T oldT, @Nullable T newT) throws Exception;

    /**
     * @return an {@link Observable} that should be performed before we call {@link Table#delete(Object)}
     */
    @NonNull
    Observable<T> preDelete(@NonNull T t);

    void postDelete(boolean success, @NonNull T t) throws Exception;
}
