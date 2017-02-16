package co.smartreceipts.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.settings.catalog.UserPreference;
import rx.Observable;

public class UserPreferenceManager {

    private final Context context;
    private final SharedPreferences preferences;

    public UserPreferenceManager(@NonNull Context context) {
        this.context = Preconditions.checkNotNull(context);
        this.preferences = context.getSharedPreferences(Preferences.SMART_PREFS, 0);
    }

    public void todo() {
        // TODO: Custom initiailization
        // All floats b/c no default_value in settings options
        // Minimum Receipt Price set as minimum price
        // initDefaultDateSeparator
        // initDefaultCurrency
    }

    @NonNull
    public <T> T get(UserPreference<T> preference) {
        return null;
    }

    @NonNull
    public <T> Observable<T> getObservable(UserPreference<T> preference) {
        return Observable.empty();
    }

}
