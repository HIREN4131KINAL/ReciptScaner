package co.smartreceipts.android;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.AnalyticsManager;

public class ExtraInitializerPlusImpl implements ExtraInitializer {
    
    @Inject
    public ExtraInitializerPlusImpl() {
    }


    @Override
    public void init(AnalyticsManager analyticsManager) {
        /* no-op */
    }
    
}
