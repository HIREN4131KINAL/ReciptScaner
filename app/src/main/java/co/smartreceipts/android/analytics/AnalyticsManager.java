package co.smartreceipts.android.analytics;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.events.Event;
import co.smartreceipts.android.di.scopes.ApplicationScope;

@ApplicationScope
public class AnalyticsManager implements Analytics {

    private final CopyOnWriteArrayList<Analytics> mAnalyticsList;
    private final Executor mExecutor;

//    public AnalyticsManager() {
//        this(Collections.<Analytics>emptyList());
//    }

    /**
     *
     * @param analytics is always AnalyticsLogger
     */
    @Inject
    public AnalyticsManager(@NonNull Analytics analytics) {
        this(Collections.singletonList(Preconditions.checkNotNull(analytics)));
    }

    private AnalyticsManager(@NonNull List<Analytics> analytics) {
        mAnalyticsList = new CopyOnWriteArrayList<>(Preconditions.checkNotNull(analytics));
        mExecutor = Executors.newSingleThreadExecutor(); //TODO: Consider intent service
    }

    public void register(@NonNull Analytics analytics) {
        mAnalyticsList.add(analytics);
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
