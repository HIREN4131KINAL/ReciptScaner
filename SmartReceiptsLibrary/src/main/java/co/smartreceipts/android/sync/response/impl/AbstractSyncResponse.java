package co.smartreceipts.android.sync.response.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.request.SyncRequest;
import co.smartreceipts.android.sync.response.SyncResponse;

/**
 * An abstract implementation of the {@link co.smartreceipts.android.sync.response.SyncResponse} interface
 * in order to provide a base implementation for common data types.
 *
 * @author williambaumann
 */
class AbstractSyncResponse<T extends Parcelable> implements SyncResponse<T> {

    private final SyncRequest<T> mSyncRequest;
    private final T mResponse;

    protected AbstractSyncResponse(@NonNull T response, @NonNull SyncRequest<T> syncRequest) {
        mResponse = response;
        mSyncRequest = syncRequest;
    }

    protected AbstractSyncResponse(@NonNull Parcel in) {
        this(in, (Class<T>) in.readSerializable());
    }

    /**
     * Stepping stone constructor to ensure we're using an efficient class loader (i.e. framework vs APK) for re-loading the parcel
     *
     * @param in    - the {@link android.os.Parcel} to load
     * @param klass - the {@link java.lang.Class} from which we'll generate a {@link java.lang.ClassLoader}
     */
    private AbstractSyncResponse(@NonNull Parcel in, @NonNull Class<T> klass) {
        this((T) in.readParcelable(klass.getClassLoader()), (SyncRequest<T>) in.readParcelable(SyncRequest.class.getClassLoader()));
    }

    @NonNull
    @Override
    public final SyncRequest<T> getRequest() {
        return mSyncRequest;
    }

    @NonNull
    @Override
    public final T getResponse() {
        return mResponse;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mResponse.getClass());
        dest.writeParcelable(mResponse, flags);
        dest.writeParcelable(mSyncRequest, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractSyncResponse that = (AbstractSyncResponse) o;

        if (!mResponse.equals(that.mResponse)) return false;
        if (!mSyncRequest.equals(that.mSyncRequest)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mSyncRequest.hashCode();
        result = 31 * result + mResponse.hashCode();
        return result;
    }
}
