package co.smartreceipts.android.model.factory;

/**
 * A simple builder factory interface that allows us {@link #build()} new instances of objects of
 * type {@link T}
 */
public interface BuilderFactory<T> {

    T build();
}
