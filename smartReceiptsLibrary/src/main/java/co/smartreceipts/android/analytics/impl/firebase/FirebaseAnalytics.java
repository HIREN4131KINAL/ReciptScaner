package co.smartreceipts.android.analytics.impl.firebase;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.crash.FirebaseCrash;

import java.util.List;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.analytics.events.Event;

public class FirebaseAnalytics implements Analytics {


    private final com.google.firebase.analytics.FirebaseAnalytics mFirebaseAnalytics;

    public FirebaseAnalytics(@NonNull Context context) {
        mFirebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(
                context.getApplicationContext());
    }

    @Override
    public void record(@NonNull Event event) {
        if (event instanceof ErrorEvent) {
            FirebaseCrash.report(((ErrorEvent) event).getThrowable());
        } else {
            Bundle b = new Bundle();
            List<DataPoint> dataPoints = event.getDataPoints();
            for (DataPoint dataPoint : dataPoints) {
                b.putString(dataPoint.getName(), dataPoint.getValue());
            }
            mFirebaseAnalytics.logEvent(event.name().name(), b);
        }
    }
}
