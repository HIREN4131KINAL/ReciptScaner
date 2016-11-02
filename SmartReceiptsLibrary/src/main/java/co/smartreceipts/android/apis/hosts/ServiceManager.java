package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides a standardized manner in which we can define host configurations and their association to a specific
 * service endpoint for network requests within the app
 */
public class ServiceManager {

    private final ConcurrentHashMap<Object, HostConfiguration> mHostConfigurationMap = new ConcurrentHashMap<>();
    private final HostConfiguration mDefaultHostConfiguration;

    public ServiceManager() {
        this(new SmartReceiptsHostConfiguration());
    }

    public ServiceManager(@NonNull HostConfiguration defaultHostConfiguration) {
        mDefaultHostConfiguration = defaultHostConfiguration;
    }

    /**
     * Registers an endpoint to use a host configuration that does not align with our default
     *
     * @param hostConfiguration the custom {@link HostConfiguration} to use for this request
     * @param service the service class that will be used for this request
     */
    public <T> void registerEndpoint(@NonNull HostConfiguration hostConfiguration, final Class<T> service) {
        mHostConfigurationMap.put(service, hostConfiguration);
    }

    /**
     * Generates an appropriate service that can be used for network requests
     *
     * @param service the service class type
     * @return an instance of the service class, which can be used for the actual request
     */
    @NonNull
    @SuppressWarnings("SuspiciousMethodCalls")
    public <T> T getService(final Class<T> service) {
        final HostConfiguration hostConfiguration;
        if (mHostConfigurationMap.contains(service)) {
            hostConfiguration = mHostConfigurationMap.get(service);
        } else {
            hostConfiguration = mDefaultHostConfiguration;
        }


        final Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(hostConfiguration.getBaseUrl());
        if (hostConfiguration.getClient() != null) {
            builder.client(hostConfiguration.getClient());
        }
        builder.addConverterFactory(GsonConverterFactory.create());
        final Retrofit retrofit = builder.build();

        return retrofit.create(service);
    }
}
