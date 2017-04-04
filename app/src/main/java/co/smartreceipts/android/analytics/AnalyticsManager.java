package co.smartreceipts.android.analytics;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import co.smartreceipts.android.analytics.events.Event;

public class AnalyticsManager implements Analytics {

    private final CopyOnWriteArrayList<Analytics> mAnalyticsList;
    private final Executor mExecutor;

    public AnalyticsManager(@NonNull List<Analytics> analytics) {
        mAnalyticsList = new CopyOnWriteArrayList<>(Preconditions.checkNotNull(analytics));
        mExecutor = Executors.newSingleThreadExecutor(); //TODO: Consider intent service
    }

    @Override
    public void record(@NonNull final Event event) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (final Analytics analytics : mAnalyticsList) {
                    analytics.record(event);
                }
            }
        });
    }
}
