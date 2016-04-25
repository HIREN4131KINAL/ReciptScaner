package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;

/**
 * A simple marker interface that we can use to help identify personally identifiable information
 */
public interface PIIMarker<T> {

    /**
     * @return an object of type {@link T}
     */
    T get();
}
