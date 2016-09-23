package co.smartreceipts.android.sync.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

public class SyncProviderStore {

    private static final String KEY_SYNC_PROVIDER = "key_sync_provider_1";

    private final SharedPreferences mSharedPreferences;

    public SyncProviderStore(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public SyncProviderStore(@NonNull SharedPreferences sharedPreferences) {
        mSharedPreferences = Preconditions.checkNotNull(sharedPreferences);
    }

    @NonNull
    public SyncProvider getProvider() {
        final String syncProviderName = mSharedPreferences.getString(KEY_SYNC_PROVIDER, "");
        try {
            return SyncProvider.valueOf(syncProviderName);
        } catch (IllegalArgumentException e) {
            return SyncProvider.None;
        }
    }

    public void setSyncProvider(@NonNull SyncProvider syncProvider) {
        mSharedPreferences.edit().putString(KEY_SYNC_PROVIDER, syncProvider.name()).apply();
    }

}
