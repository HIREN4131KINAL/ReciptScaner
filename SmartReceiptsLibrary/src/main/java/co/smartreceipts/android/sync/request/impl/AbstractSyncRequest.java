package co.smartreceipts.android.sync.request.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.sync.request.SyncRequest;
import co.smartreceipts.android.sync.request.SyncRequestType;

/**
 * An abstract implementation of the {@link co.smartreceipts.android.sync.request.SyncRequest} interface
 * in order to provide a base implementation for common data types.
 *
 * @author williambaumann
 */
abstract class AbstractSyncRequest<T extends Parcelable> implements SyncRequest<T> {

    private final SyncRequestType mSyncRequestType;
    private final T mRequestData;
    private final Class<T> mClass;

    protected AbstractSyncRequest(@NonNull T requestData, @NonNull SyncRequestType requestType, @NonNull Class<T> klass) {
        mRequestData = requestData;
        mSyncRequestType = requestType;
        mClass = klass;
    }

    protected AbstractSyncRequest(@NonNull Parcel in) {
        this(in, (Class<T>) in.readSerializable());
    }

    /**
     * Stepping stone constructor to ensure we're using an efficient class loader (i.e. framework vs APK) for re-loading the parcel
     *
     * @param in - the {@link android.os.Parcel} to load
     * @param klass - the {@link java.lang.Class} from which we'll generate a {@link java.lang.ClassLoader}
     */
    private AbstractSyncRequest(@NonNull Parcel in, @NonNull Class<T> klass) {
        this((T) in.readParcelable(klass.getClassLoader()), (SyncRequestType) in.readParcelable(SyncRequestType.class.getClassLoader()), klass);
    }

    @Override
    @NonNull
    public final SyncRequestType getSyncRequestType() {
        return mSyncRequestType;
    }

    @Override
    @NonNull
    public final T getRequestData() {
        return mRequestData;
    }

    @Override
    @NonNull
    public final Class<T> getRequestDataClass() {
        return mClass;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mClass);
        dest.writeParcelable(mRequestData, flags);
        dest.writeParcelable(mSyncRequestType, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractSyncRequest that = (AbstractSyncRequest) o;

        if (!mClass.equals(that.mClass)) return false;
        if (!mRequestData.equals(that.mRequestData)) return false;
        if (!mSyncRequestType.equals(that.mSyncRequestType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mSyncRequestType.hashCode();
        result = 31 * result + mRequestData.hashCode();
        result = 31 * result + mClass.hashCode();
        return result;
    }
}
