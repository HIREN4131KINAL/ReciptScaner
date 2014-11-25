package co.smartreceipts.android.sync.user.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.id.Identifier;
import co.smartreceipts.android.sync.user.SyncUser;

/**
 * Encapsulates an immutable implementation of the {@link co.smartreceipts.android.sync.user.SyncUser} interface
 *
 * @author williambaumann
 */
public class ImmutableSyncUser implements SyncUser, android.os.Parcelable {

    private final Identifier mId;
    private final String mUsername, mEmail;

    public ImmutableSyncUser(@NonNull Identifier id, @NonNull String username, String email) {
        mId = id;
        mUsername = username;
        mEmail = email;
    }

    @NonNull
    @Override
    public Identifier getId() {
        return mId;
    }

    @NonNull
    @Override
    public String getUsername() {
        return mUsername;
    }

    @Override
    public String getEmail() {
        return mEmail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mId, 0);
        dest.writeString(this.mUsername);
        dest.writeString(this.mEmail);
    }

    private ImmutableSyncUser(Parcel in) {
        this.mId = in.readParcelable(Identifier.class.getClassLoader());
        this.mUsername = in.readString();
        this.mEmail = in.readString();
    }

    public static final Creator<ImmutableSyncUser> CREATOR = new Creator<ImmutableSyncUser>() {
        public ImmutableSyncUser createFromParcel(Parcel source) {
            return new ImmutableSyncUser(source);
        }

        public ImmutableSyncUser[] newArray(int size) {
            return new ImmutableSyncUser[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableSyncUser that = (ImmutableSyncUser) o;

        if (mEmail != null ? !mEmail.equals(that.mEmail) : that.mEmail != null) return false;
        if (!mId.equals(that.mId)) return false;
        if (!mUsername.equals(that.mUsername)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + mUsername.hashCode();
        result = 31 * result + (mEmail != null ? mEmail.hashCode() : 0);
        return result;
    }
}
