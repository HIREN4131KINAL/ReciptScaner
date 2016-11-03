package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

/**
 * Provides a standardized manner in which we can define host configurations and their association to a specific
 * service endpoint for network requests within the app
 */
public class ServiceManager {

    private final Retrofit mRetrofit;
    private final Map<Class<?>, Object> mCachedServiceMap = new HashMap<>();

    public ServiceManager() {
        this(new SmartReceiptsHostConfiguration());
    }

    public ServiceManager(@NonNull HostConfiguration defaultHostConfiguration) {
        Preconditions.checkNotNull(defaultHostConfiguration);

        final Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(defaultHostConfiguration.getBaseUrl());
        if (defaultHostConfiguration.getClient() != null) {
            builder.client(defaultHostConfiguration.getClient());
        }
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()));

        mRetrofit = builder.build();
    }

    /**
     * Generates an appropriate service that can be used for network requests
     *
     * @param serviceClass the service class type
     * @return an instance of the service class, which can be used for the actual request
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public synchronized  <T> T getService(final Class<T> serviceClass) {
        if (mCachedServiceMap.containsKey(serviceClass)) {
            return (T) mCachedServiceMap.get(serviceClass);
        }

        final T service = mRetrofit.create(serviceClass);
        mCachedServiceMap.put(serviceClass, service);
        return service;
    }
}
