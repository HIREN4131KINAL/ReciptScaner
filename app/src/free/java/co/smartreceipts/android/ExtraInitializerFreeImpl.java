package co.smartreceipts.android;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.GoogleAnalytics;

public class ExtraInitializerFreeImpl implements ExtraInitializer{

    @Inject
    Context context;

    @Inject
    public ExtraInitializerFreeImpl() {
    }

    @Override
    public void init(AnalyticsManager analyticsManager) {

        BugSenseHandler.initAndStartSession(context, "01de172a");

        analyticsManager.register(new GoogleAnalytics(context));
    }
}
