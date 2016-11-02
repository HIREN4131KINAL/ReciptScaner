package co.smartreceipts.android.analytics;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.common.base.Preconditions;

import java.util.List;

import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.Event;

public class GoogleAnalytics implements Analytics {

    private static final String TAG = GoogleAnalytics.class.getSimpleName();

    private final Tracker mTracker;

    public GoogleAnalytics(@NonNull Tracker tracker) {
        mTracker = Preconditions.checkNotNull(tracker);
    }

    @Override
    public synchronized void record(@NonNull Event event) {
        try {
            mTracker.send(new HitBuilders.EventBuilder(event.category().name(), event.name().name()).setLabel(getLabelString(event.getDataPoints())).build());
        } catch (Exception e) {
            Log.e(TAG, "Swallowing GA Exception: " + e.getMessage());
        }
    }

    @NonNull
    private String getLabelString(@NonNull List<DataPoint> dataPoints) {
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
            return "";
        }
    }

}