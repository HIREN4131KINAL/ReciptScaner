package co.smartreceipts.android.analytics.impl.logger;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.Event;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.log.Logger;

@ApplicationScope
public class AnalyticsLogger implements Analytics {

    @Inject
    public AnalyticsLogger() {
    }

    @Override
    public void record(@NonNull Event event) {
        Logger.info(this, "Logging Event: {} with datapoints: {}", event, getDataPointsString(event.getDataPoints()));
    }

    @NonNull
    private String getDataPointsString(@NonNull List<DataPoint> dataPoints) {
        if (!dataPoints.isEmpty()) {
            final StringBuilder stringBuilder = new StringBuilder("{");
            final String separatorChar = ",";
            String currentSeparator = "";
            for (int i = 0; i < dataPoints.size(); i++) {
                stringBuilder.append(currentSeparator).append(dataPoints.get(i).toString());
                currentSeparator = separatorChar;
            }
            return stringBuilder.append("}").toString();
        } else {
            return "{}";
        }
    }
}
