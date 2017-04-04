package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;

public interface HostConfiguration {

    /**
     * @return a string pointing to the web host (e.g. "https://smartreceipts.co")
     */
    @NonNull
    String getBaseUrl();

    /**
     * @return the desired {@link OkHttpClient}
     */
    @NonNull
    OkHttpClient getClient();

    /**
     * @return the desired {@link Gson} for response parsing
     */
    @NonNull
    Gson getGson();
}
