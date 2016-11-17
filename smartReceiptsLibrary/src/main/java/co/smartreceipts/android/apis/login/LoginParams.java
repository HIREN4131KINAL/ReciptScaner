package co.smartreceipts.android.apis.login;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

public abstract class LoginParams implements Serializable, Parcelable {

    private final String type;
    private final String email;
    private final String password;
    private final String token;

    protected LoginParams(@NonNull String type, @Nullable String email, @Nullable String password, @Nullable String token) {
        this.type = type;
        this.email = email;
        this.password = password;
        this.token = token;
    }

    protected LoginParams(Parcel in) {
        this(in.readString(), in.readString(), in.readString(), in.readString());
    }

    @Nullable
    public String getType() {
        return type;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginParams)) return false;

        LoginParams that = (LoginParams) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null)
            return false;
        return token != null ? token.equals(that.token) : that.token == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        return result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.email);
        dest.writeString(this.password);
        dest.writeString(this.token);
    }

}
