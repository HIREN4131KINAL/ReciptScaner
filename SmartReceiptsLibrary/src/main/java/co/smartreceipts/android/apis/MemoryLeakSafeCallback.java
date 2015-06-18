package co.smartreceipts.android.apis;

import java.lang.ref.WeakReference;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Extends the {@link retrofit.Callback} interface to protect against memory leaks
 *
 * @param <Data> the return data type
 * @param <Leakable> the object type that might lead to memory leaks
 */
public abstract class MemoryLeakSafeCallback<Data, Leakable> implements Callback<Data> {

    private final WeakReference<Leakable> mWeakReference;

    public MemoryLeakSafeCallback(final Leakable leakable) {
        mWeakReference = new WeakReference<Leakable>(leakable);
    }

    @Override
    public final void success(Data data, Response response) {
        final Leakable leakable = mWeakReference.get();
        if (leakable != null) {
            success(leakable, data, response);
        }
    }

    @Override
    public void failure(RetrofitError error) {
        final Leakable leakable = mWeakReference.get();
        if (leakable != null) {
            failure(leakable, error);
        }
    }

    /**
     * Successful HTTP response callback that is only called if the {@link Leakable} parameter still has a strong reference
     */
    public abstract void success(Leakable leakable, Data data, Response response);

    /**
     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
     * exception. Is only called if the {@link Leakable} parameter still has a strong reference
     */
    public abstract void failure(Leakable leakable, RetrofitError error);
}
