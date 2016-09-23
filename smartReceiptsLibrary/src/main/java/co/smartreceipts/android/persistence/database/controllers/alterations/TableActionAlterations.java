package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
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
     * @return an {@link Observable} that should be performed before we call {@link Table#insert(T, DatabaseOperationMetadata)}}
     */
    @NonNull
    Observable<T> preInsert(@NonNull T t);

    /**
     * Performs an action following a call to {@link Table#insert(T, DatabaseOperationMetadata)}
     *
     * @param t the item that was inserted in {@link Table#insert(T, DatabaseOperationMetadata)} or {@code null} if the insert failed
     */
    void postInsert(@Nullable T t)  throws Exception;

    /**
     * @return an {@link Observable} that should be performed before we call {@link Table#update(T, T, DatabaseOperationMetadata)}
     */
    @NonNull
    Observable<T> preUpdate(@NonNull T oldT, @NonNull T newT);

    /**
     * Performs an action following a call to {@link Table#update(T, T, DatabaseOperationMetadata)}
     */
    void postUpdate(@NonNull T oldT, @Nullable T newT) throws Exception;

    /**
     * @return an {@link Observable} that should be performed before we call {@link Table#delete(T, DatabaseOperationMetadata)}
     */
    @NonNull
    Observable<T> preDelete(@NonNull T t);

    /**
     * Performs an action following a call to {@link Table#delete(T, DatabaseOperationMetadata)}
     *
     * @param t the item that was inserted in {@link Table#delete(T, DatabaseOperationMetadata)} or {@code null} if the delete failed
     */
    void postDelete(@Nullable T t) throws Exception;
}
