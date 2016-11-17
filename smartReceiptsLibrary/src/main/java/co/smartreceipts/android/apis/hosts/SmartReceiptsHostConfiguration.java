package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.OkHttpClient;

public class SmartReceiptsHostConfiguration implements HostConfiguration {

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://smartreceipts.co";
    }

    @Nullable
    @Override
    public OkHttpClient getClient() {
        return null;
    }
}
