package co.smartreceipts.android.analytics;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.common.base.Preconditions;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.Event;
import co.smartreceipts.android.utils.log.Logger;

public class GoogleAnalytics implements Analytics {

    private final Tracker mTracker;

    public GoogleAnalytics(@NonNull Context context) {
        this(com.google.android.gms.analytics.GoogleAnalytics.getInstance(context).newTracker(R.xml.analytics));
    }

    public GoogleAnalytics(@NonNull Tracker tracker) {
        mTracker = Preconditions.checkNotNull(tracker);
    }

    @Override
    public synchronized void record(@NonNull Event event) {
        try {
            mTracker.send(new HitBuilders.EventBuilder(event.category().name(), event.name().name()).setLabel(getLabelString(event.getDataPoints())).build());
        } catch (Exception e) {
            Logger.error(this, "Swallowing GA Exception", e);
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