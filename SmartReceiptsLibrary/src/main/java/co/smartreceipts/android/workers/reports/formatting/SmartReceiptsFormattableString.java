package co.smartreceipts.android.workers.reports.formatting;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.Preferences;

public class SmartReceiptsFormattableString {

    private static final String REPORT_NAME = "%REPORT_NAME%";
    private static final String USER_ID = "%USER_ID%";
    private static final String REPORT_START = "%REPORT_START%";
    private static final String REPORT_END = "%REPORT_END%";

    private final String mString;
    private final Context mContext;
    private final Trip mTrip;
    private final Preferences mPreferences;

    public SmartReceiptsFormattableString(@NonNull String string, @NonNull Context context, @NonNull Trip trip, @NonNull Preferences preferences) {
        mString = string;
        mContext = context;
        mTrip = trip;
        mPreferences = preferences;
    }

    @Override
    public String toString() {
        return mString.replace(REPORT_NAME, mTrip.getName()).replace(USER_ID, mPreferences.getUserID()).replace(REPORT_START, mTrip.getFormattedStartDate(mContext, mPreferences.getDateSeparator())).replace(REPORT_END, mTrip.getFormattedEndDate(mContext, mPreferences.getDateSeparator()));
    }
}
