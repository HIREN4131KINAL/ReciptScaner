package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.OkHttpClient;

public interface HostConfiguration {

    /**
     * @return a string pointing to the web host (e.g. "https://smartreceipts.co")
     */
    @NonNull
    String getBaseUrl();

    /**
     * @return a custom {@link OkHttpClient} or {@code null} if the default one should be used
     */
    @Nullable
    OkHttpClient getClient();
}
