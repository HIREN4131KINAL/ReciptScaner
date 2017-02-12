package co.smartreceipts.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.settings.catalog.UserPreference;

public class UserPreferenceManager {

    private final Context context;
    private final SharedPreferences preferences;

    public UserPreferenceManager(@NonNull Context context) {
        this.context = Preconditions.checkNotNull(context);
        this.preferences = context.getSharedPreferences(Preferences.SMART_PREFS, 0);
    }

    public void todo() {
        // TODO: Custom initiailization
        // Minimum Receipt Price set as minimum price
        // initDefaultDateSeparator
        // initDefaultCurrency
    }

    @NonNull
    public <T> T get(UserPreference<T> preference) {
        return null;
    }

}
