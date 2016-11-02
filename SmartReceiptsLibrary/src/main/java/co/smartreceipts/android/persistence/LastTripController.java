package co.smartreceipts.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.model.Trip;

public class LastTripController {

    private static final String PREFERENCES_FILENAME = SharedPreferenceDefinitions.ReceiptFragment_Preferences.toString();
    private static final String PREFERENCE_TRIP_NAME = "tripName";

    private final Context mContext;

    private Trip mTrip;

    public LastTripController(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Retrieves the last trip that was saved via {@link #setLastTrip(co.smartreceipts.android.model.Trip)}
     * from a list of known database entries
     *
     * @return the last {@link co.smartreceipts.android.model.Trip} or {@code null} if none was ever saved
     */
    @Nullable
    public synchronized Trip getLastTrip(@NonNull List<Trip> trips) {
        if (mTrip == null) {
            final SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_FILENAME, 0);
            final String tripName = preferences.getString(PREFERENCE_TRIP_NAME, "");
            for (final Trip trip : trips) {
                if (tripName.equals(trip.getName())) {
                    mTrip = trip;
                    return mTrip;
                }
            }
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