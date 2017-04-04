package co.smartreceipts.android.push.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import rx.Observable;
import rx.Subscriber;

public class PushDataStore {

    private static final String KEY_BOOL_REMOTE_REFRESH_REQUIRED = "key_bool_remote_refresh_required";

    private final SharedPreferences sharedPreferences;

    public PushDataStore(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public PushDataStore(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = Preconditions.checkNotNull(sharedPreferences);
    }

    /**
     * @return an {@link Observable} that will emit {@code true} if a refresh is required and {@code false}
     * if one is not
     */
    @NonNull
    public Observable<Boolean> isRemoteRefreshRequiredObservable() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onNext(isRemoteRefreshRequired());
                subscriber.onCompleted();
            }
        });
    }

    public boolean isRemoteRefreshRequired() {
        return sharedPreferences.getBoolean(KEY_BOOL_REMOTE_REFRESH_REQUIRED, true);
    }

    public void setRemoteRefreshRequired(boolean refreshRequired) {
        sharedPreferences.edit().putBoolean(KEY_BOOL_REMOTE_REFRESH_REQUIRED, refreshRequired).apply();
    }
}
