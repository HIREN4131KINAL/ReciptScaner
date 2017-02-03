package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import okhttp3.OkHttpClient;

public class SmartReceiptsHostConfiguration implements HostConfiguration {

    private final SmartReceiptsGsonBuilder mSmartReceiptsGsonBuilder;

    public SmartReceiptsHostConfiguration(@NonNull SmartReceiptsGsonBuilder smartReceiptsGsonBuilder) {
        mSmartReceiptsGsonBuilder = Preconditions.checkNotNull(smartReceiptsGsonBuilder);
    }

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://smartreceipts.co";
    }

    @NonNull
    @Override
    public OkHttpClient getClient() {
        return new OkHttpClient();
    }

    @NonNull
    @Override
    public Gson getGson() {
        return mSmartReceiptsGsonBuilder.create();
    }
}
