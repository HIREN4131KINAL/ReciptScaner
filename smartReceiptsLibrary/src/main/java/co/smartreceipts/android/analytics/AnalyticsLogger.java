package co.smartreceipts.android.analytics;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.Event;
import co.smartreceipts.android.utils.Logger;

public class AnalyticsLogger implements Analytics {

    private static final String TAG = AnalyticsLogger.class.getSimpleName();

    @Override
    public void record(@NonNull Event event) {
        final StringBuilder builder = new StringBuilder("Logging Event: ");
        builder.append(event);
        builder.append(" with datapoints: ");
        builder.append(getDataPointsString(event.getDataPoints()));
        Logger.info(this, builder.toString());
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
