package co.smartreceipts.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;

public class LastTripController {

    private static final String PREFERENCES_FILENAME = SharedPreferenceDefinitions.ReceiptFragment_Preferences.toString();
    private static final String PREFERENCE_TRIP_NAME = "tripName";

    private final Context mContext;
    private final DatabaseHelper mDB;

    private Trip mTrip;

    public LastTripController(@NonNull Context context, @NonNull DatabaseHelper db) {
        mContext = context.getApplicationContext();
        mDB = db;
    }

    /**
     * Retrieves the last trip that was saved via {@link #setLastTrip(co.smartreceipts.android.model.Trip)}
     *
     * @return the last {@link co.smartreceipts.android.model.Trip} or {@code null} if none was ever saved
     */
    public synchronized Trip getLastTrip() {
        if (mTrip == null) {
            final SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_FILENAME, 0);
            final String tripName = preferences.getString(PREFERENCE_TRIP_NAME, "");
            mTrip = mDB.getTripByName(tripName);
        }
        return mTrip;
    }

    /**
     * Sets the last trip, which we can retrieve at a later point
     *
     * @param trip the last {@link co.smartreceipts.android.model.Trip} to persist
     */
    public synchronized void setLastTrip(@NonNull Trip trip) {
        final SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_FILENAME, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_TRIP_NAME, trip.getName());
        editor.apply();
        mTrip = trip;
    }

    /**
     * Clears out the last saved trip from this controller
     */
    public synchronized void clear() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_FILENAME, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.clear().apply();
    }

}