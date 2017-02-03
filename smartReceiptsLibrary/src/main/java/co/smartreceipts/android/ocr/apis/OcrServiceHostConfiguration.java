package co.smartreceipts.android.ocr.apis;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.smartreceipts.android.R;
import co.smartreceipts.android.apis.hosts.HostConfiguration;
import co.smartreceipts.android.utils.log.Logger;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OcrServiceHostConfiguration implements HostConfiguration {

    private final String mApiKey;

    public OcrServiceHostConfiguration(@NonNull Context context) {
        mApiKey = Preconditions.checkNotNull(context.getString(R.string.ocr_api_key));
    }

    public OcrServiceHostConfiguration(@NonNull String apiKey) {
        mApiKey = Preconditions.checkNotNull(apiKey);
    }

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://api-au.taggun.io";
    }

    @NonNull
    @Override
    public OkHttpClient getClient() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(new ApiKeyHeaderInterceptor(mApiKey))
                .build();
    }

    @NonNull
    @Override
    public Gson getGson() {
        return new GsonBuilder().create();
    }

    private static class ApiKeyHeaderInterceptor implements Interceptor {

        private final String mApiKey;

        public ApiKeyHeaderInterceptor(@NonNull String apiKey) {
            mApiKey = Preconditions.checkNotNull(apiKey);
        }

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            final Request original = chain.request();
            final Request apiKeyedRequest = original.newBuilder().header("apikey", mApiKey).build();
            return chain.proceed(apiKeyedRequest);
        }
    }
}
