package co.smartreceipts.android.model;

import android.support.annotation.NonNull;

/**
 * A simple interface for any class that has pricing information via the {@link co.smartreceipts.android.model.Price}
 * interface
 *
 * @author williambaumann
 */
public interface Priceable {

    /**
     * Gets the price for this particular item
     *
     * @return the {@link co.smartreceipts.android.model.Price}
     */
    @NonNull
    Price getPrice();
}
