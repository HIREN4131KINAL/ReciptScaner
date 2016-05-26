package co.smartreceipts.android.model;

import android.support.annotation.NonNull;

public interface Category {

    /**
     * @return the full-name representation of this category
     */
    @NonNull
    String getName();

    /**
     * @return the "code" associated with this category
     */
    @NonNull
    String getCode();
}
