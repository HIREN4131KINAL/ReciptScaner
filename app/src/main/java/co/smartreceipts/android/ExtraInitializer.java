package co.smartreceipts.android;

import co.smartreceipts.android.analytics.AnalyticsManager;

/**
 * For additional version-dependent initialization
 */
public interface ExtraInitializer {

    void init(AnalyticsManager analyticsManager);

}
