package co.smartreceipts.android.model;

import android.support.annotation.NonNull;

/**
 * A simple interface for any class that has pricing information via the {@link Price}
 * interface
 *
 * @author williambaumann
 */
public interface Priceable {

    /**
     * Gets the price for this particular item
     *
     * @return the {@link Price}
     */
    @NonNull
    Price getPrice();
}
