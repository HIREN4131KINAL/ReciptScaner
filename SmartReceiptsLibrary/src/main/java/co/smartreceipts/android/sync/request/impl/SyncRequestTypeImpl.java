package co.smartreceipts.android.sync.request.impl;

import android.os.Parcel;

import co.smartreceipts.android.sync.request.SyncRequestType;

/**
 * Provides an {@link java.lang.Enum} implementation of the {@link SyncRequestTypeImpl}
 * interface
 *
 * @author williambaumann
 */
public enum SyncRequestTypeImpl implements SyncRequestType {
    Get("Get"), Insert("Insert"), Update("Update"), Delete("Delete");

    private final String mType;

    private SyncRequestTypeImpl(String type) {
        mType = type;
    }

    @Override
    public String getType() {
        return mType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mType);
    }

    public static final Creator<SyncRequestTypeImpl> CREATOR = new Creator<SyncRequestTypeImpl>() {
        public SyncRequestTypeImpl createFromParcel(Parcel source) {
            return getEnumFromType(source.readString());
        }

        public SyncRequestTypeImpl[] newArray(int size) {
            return new SyncRequestTypeImpl[size];
        }
    };


    public static SyncRequestTypeImpl getEnumFromType(String requestType) {
        for (SyncRequestTypeImpl syncRequestTypeEnum : SyncRequestTypeImpl.values()) {
            if (syncRequestTypeEnum.getType().equals(requestType)) {
                return syncRequestTypeEnum;
            }
        }
        throw new IllegalArgumentException("Invalud Request Type was provided: " + requestType);
    }
}
