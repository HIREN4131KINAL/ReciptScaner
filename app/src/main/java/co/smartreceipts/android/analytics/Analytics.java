package co.smartreceipts.android.analytics;

import android.support.annotation.NonNull;

import co.smartreceipts.android.analytics.events.Event;

/**
 * A default contract which can be used for logging events
 */
public interface Analytics {

    /**
     * Records a specific event
     *
     * @param event the {@link Event} to record
     */
    void record(@NonNull Event event);
}
