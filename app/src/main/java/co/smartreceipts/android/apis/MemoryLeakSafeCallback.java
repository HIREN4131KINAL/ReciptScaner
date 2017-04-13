package co.smartreceipts.android.apis;

import java.lang.ref.WeakReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Extends the {@link Callback} interface to protect against memory leaks
 *
 * @param <Data> the return data type
 * @param <Leakable> the object type that might lead to memory leaks
 */
public abstract class MemoryLeakSafeCallback<Data, Leakable> implements Callback<Data> {

    private final WeakReference<Leakable> mWeakReference;
    private boolean mIgnoreResult = false;

    public MemoryLeakSafeCallback(final Leakable leakable) {
        mWeakReference = new WeakReference<>(leakable);
    }

    @Override
    public final synchronized void onResponse(Call<Data> call, Response<Data> response) {
        final Leakable leakable = mWeakReference.get();
        if (!mIgnoreResult && leakable != null) {
            success(leakable, call, response);
        }
    }

    @Override
    public final synchronized void onFailure(Call<Data> call, Throwable t) {
        final Leakable leakable = mWeakReference.get();
        if (!mIgnoreResult && leakable != null) {
            failure(leakable, call, t);
        }
    }

    /**
     * Indicates that the result should be ignored (akin to cancel but the request may still complete)
     */
    public final synchronized void ignoreResult() {
        mIgnoreResult = true;
    }

    /**
     * Successful HTTP response callback that is only called if the {@link Leakable} parameter still has a strong reference
     */
    public abstract void success(Leakable leakable, Call<Data> call, Response<Data> response);

    /**
     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
     * exception. Is only called if the {@link Leakable} parameter still has a strong reference
     */
    public abstract void failure(Leakable leakable, Call<Data> call, Throwable t);

}