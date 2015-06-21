package co.smartreceipts.android.apis;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Safely holds a desired retrofit service in order that it will persist across configuration changes (so long as the parent activity
 * or fragment is not destroyed). Use the {@link #getService()} method to retrieve a service from this class
 *
 * @param <T> the service class type
 */
public class NetworkRequestManager<T> {

    private final RetrofitHeadlessFragment mHeadlessFragment;

    public NetworkRequestManager(@NonNull FragmentManager fragmentManager, @NonNull String endpoint, @NonNull Class<T> serviceClass) {
        RetrofitHeadlessFragment headlessFragment = (RetrofitHeadlessFragment) fragmentManager.findFragmentByTag(RetrofitHeadlessFragment.TAG);
        if (headlessFragment == null) {
            headlessFragment = new RetrofitHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, RetrofitHeadlessFragment.TAG).commit();
            final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            final RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setConverter(new GsonConverter(gson)).setEndpoint(endpoint).build();
            headlessFragment.mServiceClass = restAdapter.create(serviceClass);
        }
        mHeadlessFragment = headlessFragment;
    }

    @SuppressWarnings("unchecked")
    public final T getService() {
        return (T) mHeadlessFragment.mServiceClass;
    }

    public static final class RetrofitHeadlessFragment extends Fragment {

        private static final String TAG = RetrofitHeadlessFragment.class.getName();

        private Object mServiceClass;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
