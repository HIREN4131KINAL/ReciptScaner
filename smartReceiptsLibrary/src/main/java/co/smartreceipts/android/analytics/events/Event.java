package co.smartreceipts.android.analytics.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * A simple model-class contract, which can be used to assist with tracking usage analytics in the app
 */
public interface Event {

    /**
     * Defines the category in which this event resides
     */
    interface Category {

        /**
         * @return the {@link String} representation of this
         */
        @NonNull
        String name();
    }

    /**
     * Defines the name of this specific event
     */
    interface Name {

        /**
         * @return the {@link String} representation of this
         */
        @NonNull
        String name();
    }

    /**
     * @return the {@link Event.Category} associated with this event
     */
    @NonNull
    Event.Category category();

    /**
     * @return the {@link Event.Name} associated with this event
     */
    @NonNull
    Event.Name name();

    /**
     * @return a {@link List} of {@link DataPoint} objeccts associated with this event
     */
    @NonNull
    List<DataPoint> getDataPoints();

}
