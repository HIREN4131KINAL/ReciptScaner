package co.smartreceipts.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.TypedValue;

import com.google.common.base.Preconditions;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class UserPreferenceManager {

    private final Context context;
    private final SharedPreferences preferences;
    private final Scheduler initializationScheduler;

    public UserPreferenceManager(@NonNull Context context) {
        this(context.getApplicationContext(), context.getSharedPreferences(Preferences.SMART_PREFS, 0), Schedulers.io());
    }

    @VisibleForTesting
    UserPreferenceManager(@NonNull Context context, @NonNull SharedPreferences preferences, @NonNull Scheduler initializationScheduler) {
        this.context = Preconditions.checkNotNull(context);
        this.preferences = Preconditions.checkNotNull(preferences);
        this.initializationScheduler = Preconditions.checkNotNull(initializationScheduler);
    }

    public void initialize() {
        Observable.just(UserPreference.values())
                .subscribeOn(this.initializationScheduler)
                .subscribe(new Action1<List<UserPreference<?>>>() {
                    @Override
                    public void call(List<UserPreference<?>> userPreferences) {
                        for (final UserPreference<?> userPreference : userPreferences) {
                            final String preferenceName = context.getString(userPreference.getName());
                            if (!preferences.contains(preferenceName)) {
                                // In here - we assign values that don't allow for preference_defaults.xml definitions (e.g. Locale Based Setings)
                                // Additionally, we set all float fields, which don't don't allow for 'android:defaultValue' settings
                                if (UserPreference.General.DateSeparator.equals(userPreference)) {
                                    final String assignedDateSeparator = context.getString(UserPreference.General.DateSeparator.getDefaultValue());
                                    if (TextUtils.isEmpty(assignedDateSeparator)) {
                                        final String localeDefaultDateSeparator = DateUtils.getDateSeparator(context);
                                        preferences.edit().putString(preferenceName, localeDefaultDateSeparator).apply();
                                        Logger.debug(UserPreferenceManager.this, "Assigned locale default date separator {}", localeDefaultDateSeparator);
                                    }
                                } else if (UserPreference.General.DefaultCurrency.equals(userPreference)) {
                                    final String assignedCurrencyCode = context.getString(UserPreference.General.DefaultCurrency.getDefaultValue());
                                    if (TextUtils.isEmpty(assignedCurrencyCode)) {
                                        final String currencyCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
                                        preferences.edit().putString(preferenceName, currencyCode).apply();
                                        Logger.debug(UserPreferenceManager.this, "Assigned locale default currency code {}", currencyCode);
                                    }
                                } else if (UserPreference.Receipts.MinimumReceiptPrice.equals(userPreference)) {
                                    final TypedValue typedValue = new TypedValue();
                                    context.getResources().getValue(userPreference.getDefaultValue(), typedValue, true);
                                    if (typedValue.getFloat() < 0) {
                                        final float defaultMinimumReceiptPrice = -Float.MAX_VALUE;
                                        preferences.edit().putFloat(preferenceName, defaultMinimumReceiptPrice).apply();
                                        Logger.debug(UserPreferenceManager.this, "Assigned default float value for {} as {}", preferenceName, defaultMinimumReceiptPrice);
                                    }
                                } else if (UserPreference.Camera.UseNativeCamera.equals(userPreference)) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        preferences.edit().putBoolean(preferenceName, true).apply();
                                        Logger.debug(UserPreferenceManager.this, "We always use the native camera for M+");
                                    }
                                } else if (Float.class.equals(userPreference.getType())) {
                                    final TypedValue typedValue = new TypedValue();
                                    context.getResources().getValue(userPreference.getDefaultValue(), typedValue, true);
                                    preferences.edit().putFloat(preferenceName, typedValue.getFloat()).apply();
                                    Logger.debug(UserPreferenceManager.this, "Assigned default float value for {} as {}", preferenceName, typedValue.getFloat());
                                }
                            }
                        }
                        Logger.debug(UserPreferenceManager.this, "Completed user preference initialization");
                    }
                });
    }

    @NonNull
    public SharedPreferences getSharedPreferences() {
        return preferences;
    }

    @NonNull
    public <T> Observable<T> getObservable(final UserPreference<T> preference) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                subscriber.onNext(get(preference));
                subscriber.onCompleted();
            }
        });
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> T get(UserPreference<T> preference) {
        final String name = context.getString(preference.getName());
        if (Boolean.class.equals(preference.getType())) {
            return (T) Boolean.valueOf(preferences.getBoolean(name, context.getResources().getBoolean(preference.getDefaultValue())));
        } else if (String.class.equals(preference.getType())) {
            return (T) preferences.getString(name, context.getString(preference.getDefaultValue()));
        } else if (Float.class.equals(preference.getType())) {
            final TypedValue typedValue = new TypedValue();
            context.getResources().getValue(preference.getDefaultValue(), typedValue, true);
            return (T) Float.valueOf(preferences.getFloat(name, typedValue.getFloat()));
        } else if (Integer.class.equals(preference.getType())) {
            return (T) Integer.valueOf(preferences.getInt(name, context.getResources().getInteger(preference.getDefaultValue())));
        }  else {
            throw new IllegalArgumentException("Unsupported preference type: " + preference.getType());
        }
    }

}
